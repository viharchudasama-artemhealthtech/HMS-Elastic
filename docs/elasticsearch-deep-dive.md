# Elasticsearch Deep Dive

## Purpose

This folder now contains Elasticsearch documentation split into three focused guides so it is easier to read step by step.

## Reading Order

1. [basic-elasticsearch.md](./basic-elasticsearch.md)
2. [advanced-elasticsearch-internals.md](./advanced-elasticsearch-internals.md)
3. [elasticsearch-implementation-patterns.md](./elasticsearch-implementation-patterns.md)

## What Each File Covers

### `basic-elasticsearch.md`

Best for:

- beginners
- onboarding
- understanding Elasticsearch vocabulary
- learning how a document becomes searchable

Main topics:

- document, field, index, token, term
- mappings
- analyzers
- inverted index
- simple query types
- basic end-to-end examples

### `advanced-elasticsearch-internals.md`

Best for:

- deep internals
- technical interviews
- performance and debugging
- understanding Lucene behavior under Elasticsearch

Main topics:

- segments
- refresh, flush, translog
- postings lists
- doc values
- search execution flow
- scoring
- merge behavior
- why Elasticsearch is fast

### `elasticsearch-implementation-patterns.md`

Best for:

- real-world application design
- autocomplete
- phonetic search
- fuzzy matching
- async indexing
- DB plus Elasticsearch architecture

Main topics:

- when to use `text` vs `keyword`
- `edge_ngram` vs `ngram`
- phonetic design choices
- sync vs async indexing
- event-driven indexing flow
- relevance tuning patterns

## Short Summary

If you want one sentence only:

Elasticsearch is fast because it converts text into terms and then looks up those terms directly instead of scanning documents one by one.

## References

- Elastic analyzer anatomy: https://www.elastic.co/guide/en/elasticsearch/reference/current/analyzer-anatomy.html
- Elastic near real-time search: https://www.elastic.co/docs/manage-data/data-store/near-real-time-search
- Elastic `_source`: https://www.elastic.co/docs/reference/elasticsearch/mapping-reference/mapping-source-field
- Elastic `doc_values`: https://www.elastic.co/guide/en/elasticsearch/reference/current/doc-values.html/
- Elastic `search_analyzer`: https://www.elastic.co/docs/reference/elasticsearch/mapping-reference/search-analyzer
- Elastic multi-fields: https://www.elastic.co/guide/en/elasticsearch/reference/current/_multi_fields.html
- Elastic phonetic plugin: https://www.elastic.co/docs/reference/elasticsearch/plugins/analysis-phonetic
- Elastic `edge_ngram` tokenizer: https://www.elastic.co/guide/en/elasticsearch/reference/current/analysis-edgengram-tokenizer.html
- Elastic `ngram` token filter: https://www.elastic.co/docs/reference/text-analysis/analysis-ngram-tokenfilter
- Elastic similarity settings: https://www.elastic.co/docs/reference/elasticsearch/index-settings/similarity
- Elastic translog settings: https://www.elastic.co/docs/reference/elasticsearch/index-settings/translog
- Elastic routing field: https://www.elastic.co/guide/en/elasticsearch/reference/current/mapping-routing-field.html
- Elastic indexing buffer: https://www.elastic.co/guide/en/elasticsearch/reference/current/indexing-buffer.html
- Lucene file formats: https://lucene.apache.org/core/3_5_0/fileformats.html
- Lucene postings format: https://lucene.apache.org/core/10_1_0/core/org/apache/lucene/codecs/lucene101/Lucene101PostingsFormat.html
