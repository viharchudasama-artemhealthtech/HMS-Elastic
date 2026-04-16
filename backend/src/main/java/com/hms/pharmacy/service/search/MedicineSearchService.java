package com.hms.pharmacy.service.search;

import co.elastic.clients.elasticsearch._types.FieldValue;
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
import org.apache.commons.codec.language.DoubleMetaphone;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class MedicineSearchService {

    private final MedicineSearchRepository medicineSearchRepository;
    private final MedicineRepository medicineRepository;
    private final MedicineMapper medicineMapper;
    private final ElasticsearchOperations elasticsearchOperations;
    private final DoubleMetaphone doubleMetaphone = new DoubleMetaphone();

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
        String query = keyword == null ? "" : keyword.trim();
        if (query.isBlank()) {
            throw new BadRequestException("Search keyword is required");
        }

        long startTime = System.nanoTime();
        try {
            List<String> phoneticCodes = buildPhoneticCodes(query);
            NativeQuery searchQuery = NativeQuery.builder()
                    .withQuery(q -> q.bool(b -> b
                            .must(m -> m.bool(should -> should
                                    .should(s -> s.match(match -> match
                                            .field("name")
                                            .query(query)
                                            .boost(3.0f)))
                                    .should(s -> s.match(match -> match
                                            .field("name")
                                            .query(query)
                                            .fuzziness("AUTO")
                                            .boost(2.0f)))
                                    .should(s -> s.terms(terms -> terms
                                            .field("phoneticCodes")
                                            .terms(values -> values.value(phoneticCodes.stream()
                                                    .map(FieldValue::of)
                                                    .toList()))))
                                    .minimumShouldMatch("1")))
                            .filter(f -> f.term(t -> t
                                    .field("isActive")
                                    .value(true)))))
                    .withPageable(PageRequest.of(0, 10))
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
            List<MedicineSuggestionDTO> results = medicineRepository.searchActiveMedicines(query, PageRequest.of(0, 10)).stream()
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
            List<Medicine> medicines = medicineRepository.findAll();
            List<MedicineSearch> docs = medicines.stream()
                                                 .map(this::toSearchDocument)
                                                 .toList();
            medicineSearchRepository.saveAll(docs);
            log.info("Reindex {} medicines into Elasticsearch", docs.size());
        } catch (Exception ex) {
            log.warn("Elasticsearch reindex skipped because search service is unavailable: {}", ex.getMessage());
            throw new IllegalStateException("Elasticsearch is unavailable. Start Elasticsearch and try reindex again.", ex);
        }
    }

    private MedicineSearch toSearchDocument(Medicine medicine) {
        return MedicineSearch.builder()
                .id(medicine.getId())
                .name(medicine.getName())
                .phoneticCodes(buildPhoneticCodes(medicine.getName()))
                .brand(medicine.getManufacturer())
                .stock(medicine.getQuantityInStock())
                .isActive(medicine.getIsActive())
                .build();
    }

    private List<String> buildPhoneticCodes(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }

        String lowerValue = value.toLowerCase(Locale.ROOT);
        return Stream.concat(
                Arrays.stream(lowerValue.split("[^a-z0-9]+"))
                        .filter(token -> !token.isBlank()),
                Stream.of(lowerValue.trim())
        )
                .distinct()
                .flatMap(token -> Arrays.stream(new String[]{
                        doubleMetaphone.doubleMetaphone(token),
                        doubleMetaphone.doubleMetaphone(token, true)
                }))
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(code -> !code.isBlank())
                .distinct()
                .toList();
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
