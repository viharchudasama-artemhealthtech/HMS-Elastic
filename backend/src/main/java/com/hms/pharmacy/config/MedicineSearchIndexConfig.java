package com.hms.pharmacy.config;

import com.hms.pharmacy.entity.MedicineSearch;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.index.AliasAction;
import org.springframework.data.elasticsearch.core.index.AliasActionParameters;
import org.springframework.data.elasticsearch.core.index.AliasActions;
import org.springframework.data.elasticsearch.core.index.Settings;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Configuration
@RequiredArgsConstructor
@Slf4j

// This package is used for configuration classes , Means this class is NOT business logic, it's setup code
public class MedicineSearchIndexConfig {

    private static final String MEDICINES_ALIAS = "medicines";
    private static final String MEDICINES_INDEX = "medicines_v1";

    // Core Interface to interact with Elasticsearch like  create index , search , mapping and save data
    private final ElasticsearchOperations elasticsearchOperations;

    // ApplicationRunner runs automatically when springboot starts => use for initialization , seeding data and index creation
    @Bean
    ApplicationRunner medicineIndexInitializer() {
        return args -> {
            try {
                IndexOperations indexOps = elasticsearchOperations.indexOps(IndexCoordinates.of(MEDICINES_INDEX));

                if (indexOps.exists()) {
                    ensureAlias(indexOps);
                    log.info("Elasticsearch index {} already exists; ensured alias {}", MEDICINES_INDEX, MEDICINES_ALIAS);
                    return;
                }

                // Edge ngram filter for autocomplete.
                Map<String, Object> edgeNgramFilter = new LinkedHashMap<>();
                edgeNgramFilter.put("type", "edge_ngram");
                edgeNgramFilter.put("min_gram", 1);
                edgeNgramFilter.put("max_gram", 20);

                // Word delimiter filter splits punctuation and numeric boundaries
                Map<String, Object> wordDelimiterFilter = new LinkedHashMap<>();
                wordDelimiterFilter.put("type", "word_delimiter");
                wordDelimiterFilter.put("preserve_original", true);
                wordDelimiterFilter.put("split_on_case_change", true);
                wordDelimiterFilter.put("split_on_numerics", true);
                wordDelimiterFilter.put("generate_word_parts", true);
                wordDelimiterFilter.put("generate_number_parts", true);

                // Index Analyzer splits on standard tokenizer, lowercases, and generates autocomplete ngrams
                Map<String, Object> autocompleteIndexAnalyzer = new LinkedHashMap<>();
                autocompleteIndexAnalyzer.put("type", "custom");
                autocompleteIndexAnalyzer.put("tokenizer", "standard");
                autocompleteIndexAnalyzer.put("filter", new String[]{"lowercase", "word_delimiter", "autocomplete_filter"});

                // Search Analyzer uses standard tokenizer and lowercase normalization
                Map<String, Object> autocompleteSearchAnalyzer = new LinkedHashMap<>();
                autocompleteSearchAnalyzer.put("type", "custom");
                autocompleteSearchAnalyzer.put("tokenizer", "standard");
                autocompleteSearchAnalyzer.put("filter", new String[]{"lowercase"});

                Map<String, Object> phoneticFilter = new LinkedHashMap<>();
                phoneticFilter.put("type", "phonetic");
                phoneticFilter.put("encoder", "double_metaphone");
                phoneticFilter.put("replace", false);

                Map<String, Object> phoneticAnalyzer = new LinkedHashMap<>();
                phoneticAnalyzer.put("type", "custom");
                phoneticAnalyzer.put("tokenizer", "standard");
                phoneticAnalyzer.put("filter", new String[]{"lowercase", "phonetic_filter"});

                // Combine filter and char filter
                Map<String, Object> analysis = new LinkedHashMap<>();
                analysis.put("filter", Map.of(
                        "autocomplete_filter", edgeNgramFilter,
                        "word_delimiter", wordDelimiterFilter,
                        "phonetic_filter", phoneticFilter
                ));
                analysis.put("analyzer", Map.of(
                        "autocomplete_index", autocompleteIndexAnalyzer,
                        "autocomplete_search", autocompleteSearchAnalyzer,
                        "phonetic_analyzer", phoneticAnalyzer
                ));

                Settings settings = new Settings(Map.of(
                                "index", Map.of(
                                "max_ngram_diff", 19,
                                "analysis", analysis
                        )
                ));

                boolean created = indexOps.create(settings);
                if (!created) {
                    log.warn("Elasticsearch index medicines could not be created");
                    return;
                }

                indexOps.putMapping(indexOps.createMapping(MedicineSearch.class));
                ensureAlias(indexOps);
                log.info("Created Elasticsearch index {} with alias {}", MEDICINES_INDEX, MEDICINES_ALIAS);
            } catch (Exception ex) {
                log.warn("Elasticsearch is unavailable during startup. Continuing with DB-backed search fallback: {}", ex.getMessage());
            }
        };
    }

    private void ensureAlias(IndexOperations indexOps) {
        List<AliasAction> actions = new ArrayList<>();

        findIndicesForAlias(indexOps).stream()
                .filter(index -> !MEDICINES_INDEX.equals(index))
                .map(index -> AliasActionParameters.builder()
                        .withIndices(index)
                        .withAliases(MEDICINES_ALIAS)
                        .build())
                .map(AliasAction.Remove::new)
                .forEach(actions::add);

        AliasActionParameters addAliasParameters = AliasActionParameters.builder()
                .withIndices(MEDICINES_INDEX)
                .withAliases(MEDICINES_ALIAS)
                .withIsWriteIndex(true)
                .build();
        actions.add(new AliasAction.Add(addAliasParameters));

        indexOps.alias(new AliasActions(actions.toArray(AliasAction[]::new)));
    }

    private Set<String> findIndicesForAlias(IndexOperations indexOps) {
        try {
            return indexOps.getAliases(MEDICINES_ALIAS).keySet();
        } catch (Exception ex) {
            log.debug("Alias {} is not assigned yet: {}", MEDICINES_ALIAS, ex.getMessage());
            return Set.of();
        }
    }
}
