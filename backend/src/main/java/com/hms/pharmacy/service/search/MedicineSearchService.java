package com.hms.pharmacy.service.search;

import co.elastic.clients.elasticsearch._types.SortOrder;
import com.hms.common.exception.BadRequestException;
import com.hms.pharmacy.dto.response.MedicineResponseDTO;
import com.hms.pharmacy.dto.response.MedicineSuggestionDTO;
import com.hms.pharmacy.entity.Medicine;
import com.hms.pharmacy.entity.MedicineSearch;
import com.hms.pharmacy.mapper.MedicineMapper;
import com.hms.pharmacy.repository.MedicineRepository;
import com.hms.pharmacy.repository.elasticsearch.MedicineSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.IndexQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MedicineSearchService {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 10;
    private static final int MAX_SEARCH_SIZE = 50;
    private static final int REINDEX_BATCH_SIZE = 1_000;

    private static final float EXACT_BOOST = 4.0f;
    private static final float AUTOCOMPLETE_BOOST = 3.0f;
    private static final float FUZZY_BOOST = 2.0f;
    private static final float PHONETIC_BOOST = 1.5f;

    private final MedicineSearchRepository medicineSearchRepository;
    private final MedicineRepository medicineRepository;
    private final MedicineMapper medicineMapper;
    private final ElasticsearchOperations elasticsearchOperations;

    public List<MedicineResponseDTO> getActiveMedicines() {
        try {
            List<MedicineSearch> docs = medicineSearchRepository.findByIsActiveTrue();
            if (docs.isEmpty()) {
                return medicineMapper.toDtoList(medicineRepository.findByIsActiveTrue());
            }

            // It takes a list of MedicineSearch objects → extracts their IDs → looks up each medicine in the repository → ignores IDs that don’t exist → converts found medicines into DTOs → returns them as a list.
            return docs.stream()
                    .map(MedicineSearch::getId)
                    .map(medicineRepository::findById)
                    .flatMap(Optional::stream)
                    .map(medicineMapper::toDto)
                    .toList();
        } catch (Exception ex) {
            log.error("Elasticsearch active medicines query failed, using DB fallback: {}", ex.getMessage());
            return medicineMapper.toDtoList(medicineRepository.findByIsActiveTrue());
        }
    }

    public List<MedicineSuggestionDTO> searchMedicines(String keyword) {
        return searchMedicines(keyword, DEFAULT_PAGE, DEFAULT_SIZE);
    }

    public List<MedicineSuggestionDTO> searchMedicines(String keyword, int page, int size) {
        String query = keyword == null ? "" : keyword.trim();
        if (query.isBlank()) {
            throw new BadRequestException("Search keyword is required");
        }

        PageRequest pageRequest = PageRequest.of(
                Math.max(page, DEFAULT_PAGE),
                Math.min(Math.max(size, 1), MAX_SEARCH_SIZE));

        long startTime = System.nanoTime();
        try {
            NativeQuery searchQuery = NativeQuery.builder()
                    .withQuery(q -> q.bool(b -> b
                            .should(s -> s.matchPhrase(match -> match
                                    .field("name")
                                    .query(query)
                                    .boost(EXACT_BOOST)))
                            .should(s -> s.match(match -> match
                                    .field("name.auto")
                                    .query(query)
                                    .boost(AUTOCOMPLETE_BOOST)))
                            .should(s -> s.match(match -> match
                                    .field("name")
                                    .query(query)
                                    .fuzziness("AUTO")
                                    .boost(FUZZY_BOOST)))
                            .should(s -> s.match(match -> match
                                    .field("name.phonetic")
                                    .query(query)
                                    .boost(PHONETIC_BOOST)))
                            .minimumShouldMatch("1")
                            .filter(f -> f.term(t -> t
                                    .field("isActive")
                                    .value(true)))))
                    .withPageable(pageRequest)
                    .withSort(s -> s.score(score -> score.order(SortOrder.Desc)))
                    .build();

            List<MedicineSuggestionDTO> results = elasticsearchOperations.search(searchQuery, MedicineSearch.class)
                    .stream()
                    .map(SearchHit::getContent)
                    .map(this::toSuggestionDto)
                    .toList();
            long endTime = System.nanoTime();
            log.info("Elasticsearch search for '{}' took {} ms", query, (endTime - startTime) / 1_000_000);
            return results;
        } catch (Exception ex) {
            long esFailTime = System.nanoTime();
            log.warn("Elasticsearch autocomplete failed after {} ms, using DB fallback: {}", (esFailTime - startTime) / 1_000_000, ex.getMessage());
            List<MedicineSuggestionDTO> results = medicineRepository.searchActiveMedicines(query, pageRequest).stream()
                    .map(this::toSuggestionDto)
                    .toList();
            long dbEndTime = System.nanoTime();
            log.info("DB fallback search for '{}' took {} ms", query, (dbEndTime - esFailTime) / 1_000_000);
            return results;
        }
    }

    public void indexMedicine(Medicine medicine) {
        try {
            if (Boolean.TRUE.equals(medicine.getIsActive())) {
                medicineSearchRepository.save(toSearchDocument(medicine));
            } else {
                medicineSearchRepository.deleteById(medicine.getId());
            }
        } catch (Exception ex) {
            log.error("Failed to index medicine {}: {}", medicine.getId(), ex.getMessage());
        }
    }

    public void deleteMedicineFromIndex(Long medicineId) {
        try {
            medicineSearchRepository.deleteById(medicineId);
        } catch (Exception ex) {
            log.error("Failed to delete medicine from index {}: {}", medicineId, ex.getMessage());
        }
    }

    public void reindexAllMedicines() {
        try {
            int page = 0;
            long totalIndexed = 0;
            Page<Medicine> medicines;

            do {
                medicines = medicineRepository.findAll(PageRequest.of(page, REINDEX_BATCH_SIZE));
                List<MedicineSearch> docs = medicines.getContent().stream()
                        .map(this::toSearchDocument)
                        .toList();
                bulkIndex(docs);
                totalIndexed += docs.size();
                page++;
            } while (medicines.hasNext());

            log.info("Reindex {} medicines into Elasticsearch", totalIndexed);
        } catch (Exception ex) {
            log.warn("Elasticsearch reindex skipped because search service is unavailable: {}", ex.getMessage());
            throw new IllegalStateException("Elasticsearch is unavailable. Start Elasticsearch and try reindex again.", ex);
        }
    }

    private void bulkIndex(List<MedicineSearch> docs) {
        if (docs.isEmpty()) {
            return;
        }

        List<IndexQuery> queries = docs.stream()
                .map(doc -> new IndexQueryBuilder()
                        .withId(String.valueOf(doc.getId()))
                        .withObject(doc)
                        .build())
                .toList();

        elasticsearchOperations.bulkIndex(queries, MedicineSearch.class);
    }

    private MedicineSearch toSearchDocument(Medicine medicine) {
        return MedicineSearch.builder()
                .id(medicine.getId())
                .name(medicine.getName())
                .brand(medicine.getManufacturer())
                .stock(medicine.getQuantityInStock())
                .isActive(medicine.getIsActive())
                .build();
    }

    private MedicineSuggestionDTO toSuggestionDto(MedicineSearch document) {
        int stock = document.getStock() == null ? 0 : document.getStock();
        return MedicineSuggestionDTO.builder()
                .id(document.getId())
                .name(document.getName())
                .brand(document.getBrand())
                .stock(stock)
                .inStock(stock > 0)
                .build();
    }

    private MedicineSuggestionDTO toSuggestionDto(Medicine medicine) {
        int stock = medicine.getQuantityInStock() == null ? 0 : medicine.getQuantityInStock();
        return MedicineSuggestionDTO.builder()
                .id(medicine.getId())
                .name(medicine.getName())
                .brand(medicine.getManufacturer())
                .stock(stock)
                .inStock(stock > 0)
                .build();
    }
}
