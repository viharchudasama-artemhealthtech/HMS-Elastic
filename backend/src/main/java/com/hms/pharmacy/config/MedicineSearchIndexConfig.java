package com.hms.pharmacy.config;

import com.hms.pharmacy.entity.MedicineSearch;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.index.Settings;

import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
@Slf4j

// This package is used for configuration classes , Means this class is NOT business logic, it's setup code
public class MedicineSearchIndexConfig {

    // Core Interface to interact with Elasticsearch like  create index , search , mapping and save data
    private final ElasticsearchOperations elasticsearchOperations;

    // ApplicationRunner runs automatically when springboot starts => use for initialization , seeding data and index creation
    @Bean
    ApplicationRunner medicineIndexInitializer() {
        return args -> {
            try {
                IndexOperations indexOps = elasticsearchOperations.indexOps(MedicineSearch.class);

                if (indexOps.exists()) {
                    indexOps.delete();
                    log.info("Deleted existing Elasticsearch index medicines for reconfig");
                }

                // Edge End gram user for auto complete
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

                // Combine filter and char filter
                Map<String, Object> analysis = new LinkedHashMap<>();
                analysis.put("filter", Map.of(
                        "autocomplete_filter", edgeNgramFilter,
                        "word_delimiter", wordDelimiterFilter
                ));
                analysis.put("analyzer", Map.of(
                        "autocomplete_index", autocompleteIndexAnalyzer,
                        "autocomplete_search", autocompleteSearchAnalyzer
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
                log.info("Created Elasticsearch index medicines with autocomplete analyzers");
            } catch (Exception ex) {
                log.warn("Elasticsearch is unavailable during startup. Continuing with DB-backed search fallback: {}", ex.getMessage());
            }
        };
    }
}
