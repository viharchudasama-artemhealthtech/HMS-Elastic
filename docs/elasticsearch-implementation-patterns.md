# Elasticsearch Implementation Patterns

## Goal

This guide focuses on practical design patterns for real applications.

It covers:

- mapping patterns
- autocomplete patterns
- phonetic and fuzzy patterns
- sync vs async indexing
- event-driven indexing
- common production tradeoffs

---

## 1. Most Common High-Level Architecture

In many production systems:

- a relational database is the source of truth
- Elasticsearch is the search projection

Simple meaning:

- write the main business data to the database
- copy a search-friendly version into Elasticsearch

Why this is common:

- databases handle transactions and business integrity well
- Elasticsearch handles search behavior well

This is usually better than trying to make Elasticsearch the only primary system for everything.

---

## 2. Mapping Pattern: Exact + Full-Text Together

One of the most common patterns is to store the same logical field in two ways.

Example:

```json
"name": {
  "type": "text",
  "fields": {
    "raw": {
      "type": "keyword"
    }
  }
}
```

Meaning:

- `name` is used for full-text search
- `name.raw` is used for exact filters, sorting, and aggregations

Why it helps:

- one field can support both flexible search and exact operations

---

## 3. Pattern: Autocomplete with `edge_ngram`

This is one of the most common Elasticsearch patterns.

### Goal

Support search-as-you-type.

### Index-Time Strategy

Generate prefixes:

```text
mouse -> mo, mou, mous, mouse
```

### Search-Time Strategy

Do not expand prefixes again.

Use a simpler search analyzer.

### Why This Pattern Works

Index time:

- store prefixes ahead of time

Search time:

- query only the actual typed prefix

### Typical Design

- index analyzer includes `edge_ngram`
- search analyzer does not include `edge_ngram`

### Benefit

- fast autocomplete

### Cost

- larger index
- more indexing CPU
- more merge pressure

### Warning

Very small grams like `1` character often create very broad low-precision matches.

---

## 4. Pattern: `edge_ngram` vs `ngram`

Use `edge_ngram` when:

- you need prefixes
- user types from the start of a word
- autocomplete is the goal

Use `ngram` when:

- you truly need substring matching
- internal fragments matter

Avoid `ngram` unless needed because:

- it expands token count much more
- it increases storage and indexing cost sharply

---

## 5. Pattern: Fuzzy Search for Typos

Use fuzzy search when users often type mistakes.

Examples:

- `wirless` for `wireless`
- `mose` for `mouse`

Best use cases:

- product names
- medicine names
- people-entered search text

Tradeoff:

- more flexible matching
- potentially more expensive queries
- possible false positives

Good practice:

- use fuzzy search selectively
- avoid using it as the only strategy for every field

---

## 6. Pattern: Phonetic Search

Use phonetics when sound similarity matters more than spelling similarity.

Examples:

- `John` and `Jon`
- `Smith` and `Smyth`

### Two Main Implementations

#### Application-Side Phonetic Field

Generate phonetic codes before indexing.

Example:

```json
{
  "name": "John Smith",
  "namePhonetic": ["JN", "SM0"]
}
```

Benefits:

- easy to debug
- easy to log
- full application control

#### Analyzer-Side Phonetics

Generate phonetics in Elasticsearch analysis.

Benefits:

- cleaner search-engine-centered configuration

### Best Practice

Use phonetics as one signal, not the only signal.

Combine with:

- exact match
- normal text match
- fuzzy match where useful

---

## 7. Pattern: Layered Retrieval

One of the strongest real-world designs is layered retrieval.

Example query strategy:

- exact match gets the highest boost
- normal full-text match gets a high boost
- fuzzy match gets a medium boost
- phonetic match gets a lower fallback boost

Why this works:

- exact and strong lexical matches stay at the top
- typo and sound-like matches still get a chance

This often produces better search quality than relying on one matching method alone.

---

## 8. Pattern: Filter Separately from Scoring

Use `filter` for structured rules.

Examples:

- `inStock = true`
- `status = active`
- `price <= 2000`

Use `must` and `should` for scoring logic.

Why this matters:

- filters do not affect score
- filters are often more efficient
- relevance stays cleaner

Example:

```json
{
  "bool": {
    "must": [
      { "match": { "title": "wireless mouse" } }
    ],
    "filter": [
      { "term": { "inStock": true } },
      { "range": { "price": { "lte": 2000 } } }
    ]
  }
}
```

---

## 9. Pattern: Synchronous Indexing

Flow:

```text
Controller
-> Service
-> Save DB
-> Index Elasticsearch in same request
-> Return response
```

### Benefits

- easy to understand
- immediate search sync
- simpler flow

### Drawbacks

- request becomes slower
- Elasticsearch failure affects main request
- DB and search concerns become tightly coupled

Use when:

- scale is small
- simplicity matters more than decoupling

---

## 10. Pattern: Event-Driven Async Indexing

This is often a better production pattern.

Flow:

```text
Controller
-> Service
-> Save DB
-> Publish internal event
-> Async listener
-> Transform to search document
-> Index Elasticsearch
```

### Why This Pattern Is Strong

- main business write stays focused on DB success
- request latency is lower
- Elasticsearch indexing is decoupled
- retries and error handling become easier

### Important Detail

The event should usually run after DB commit.

Why:

- if the DB transaction rolls back, you do not want Elasticsearch to index data that never actually committed

### Typical Safe Pattern

```text
save DB in transaction
publish internal event
listen AFTER_COMMIT
process indexing asynchronously
```

### Tradeoff

Search becomes eventually consistent for a short time.

Meaning:

- the latest write may not appear in search immediately

This is often acceptable and worth the tradeoff.

---

## 11. Pattern: Bulk Reindexing

Sometimes you need to rebuild the search index.

Typical reasons:

- mapping changed
- analyzer changed
- search document structure changed
- historical data needs repair

Common flow:

```text
read from source DB
transform documents
bulk index into Elasticsearch
switch traffic or reuse index
```

Good practice:

- use batching
- avoid very frequent refresh during bulk loads
- monitor failures

---

## 12. Pattern: Search Document as a Projection

The search document should be designed for search, not as a perfect copy of the DB entity.

Example:

Source DB entity:

```json
{
  "id": 1,
  "firstName": "John",
  "lastName": "Smith",
  "isActive": true
}
```

Search projection:

```json
{
  "id": 1,
  "fullName": "John Smith",
  "fullNamePhonetic": ["JN", "SM0"],
  "isActive": true
}
```

Why this is better:

- search queries become simpler
- relevance can be tuned better
- search-specific data can be added without polluting the source model

---

## 13. Pattern: Choosing the Right Matching Method

### Exact Match

Use for:

- IDs
- codes
- categories
- statuses

### Full-Text Match

Use for:

- titles
- descriptions
- long text

### Fuzzy Match

Use for:

- typo-prone user input

### Phonetic Match

Use for:

- names
- sound-like text

### `edge_ngram`

Use for:

- autocomplete

### `ngram`

Use for:

- special substring use cases only

---

## 14. Pattern: Performance-Safe Defaults

- use `keyword` for exact fields
- use `text` only where needed
- keep `edge_ngram` focused
- avoid `ngram` unless required
- put structured conditions in `filter`
- avoid oversharding
- leave memory for filesystem cache
- measure before adding more matching strategies

---

## 15. Common Real-World Mistakes

- using one field type for everything
- using autocomplete analyzers at both index and search time
- using fuzzy, phonetic, and n-gram logic everywhere without tuning
- indexing full DB entities without shaping a search projection
- updating Elasticsearch before DB commit
- making Elasticsearch failure break the main write path unnecessarily

---

## 16. Final Summary

Strong Elasticsearch implementations usually follow these ideas:

- database is the source of truth
- Elasticsearch is the search-optimized projection
- exact, lexical, fuzzy, and phonetic strategies are combined carefully
- filters are separated from scoring
- autocomplete uses `edge_ngram`
- async event-driven indexing is often better than direct synchronous indexing

If you want one practical sentence:

Design the search document for search, and design the indexing flow so search stays fast without making business writes fragile.
