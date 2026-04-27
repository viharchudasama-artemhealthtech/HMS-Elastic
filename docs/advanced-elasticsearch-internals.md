# Advanced Elasticsearch Internals

## Goal

This guide explains what happens inside Elasticsearch and Lucene after a document is indexed and when a query runs.

Focus:

- internal data structures
- indexing lifecycle
- search lifecycle
- segment behavior
- translog and durability
- relevance and performance

---

## 1. Lucene Under Elasticsearch

Elasticsearch is built on Apache Lucene.

Simple meaning:

- Elasticsearch gives distributed APIs, mappings, cluster features, and JSON access
- Lucene does the low-level indexing and searching work

When you index into Elasticsearch, the actual searchable structures are Lucene structures.

---

## 2. The Main Internal Structures

### Inverted Index

The inverted index maps:

```text
term -> documents
```

Example:

```text
wireless -> [doc1, doc3]
mouse -> [doc1]
```

### Term Dictionary

Stores the unique indexed terms in sorted order.

Why it matters:

- fast term lookup

### Postings Lists

For each term, Lucene stores a postings list.

A postings list contains:

- document IDs
- sometimes frequency
- sometimes positions

Example:

```text
mouse -> [doc1, doc9, doc22]
```

### Positions

Positions tell Lucene where a term appeared inside the field.

Why it matters:

- phrase queries
- proximity search

### Skip Data

Skip data helps Lucene move quickly through long postings lists.

Why it matters:

- better performance on large result sets

### Doc Values

Doc values are columnar field storage.

Use cases:

- sorting
- aggregations
- scripting

Mental contrast:

- inverted index = `term -> docs`
- doc values = `doc -> values`

---

## 3. `_source`, Indexed Terms, and Doc Values Are Different

A single field may exist in multiple internal forms.

Example document:

```json
{
  "title": "Wireless Mouse",
  "brand": "LogiTech",
  "price": 1299
}
```

### `_source`

Stores original JSON.

Used for:

- returning documents
- debugging
- reindexing support

### Indexed Terms

For `title`, terms may be:

```text
wireless
mouse
```

Used for:

- full-text search

### Doc Values

For `brand` and `price`, doc values help:

- sorting by `price`
- aggregating by `brand`

This is why one logical field can consume disk in more than one way.

---

## 4. Segment Model

Each shard contains one or more Lucene segments.

A segment is:

- immutable
- searchable
- later mergeable

### Why Segments Are Immutable

Benefits:

- simpler concurrent reads
- efficient compression
- fast opening for search

Tradeoff:

- updates are not in-place
- deletes are logical first, physical later

---

## 5. Indexing Lifecycle

When a document is indexed, the simplified flow is:

1. request reaches coordinating node
2. routing decides target primary shard
3. primary shard parses document
4. mappings are applied
5. text fields are analyzed
6. Lucene fields and term streams are built
7. operation is written into indexing buffers
8. operation is appended to translog
9. replicas receive the operation
10. refresh later makes the write searchable

### Important Distinction

`indexed` does not always mean:

- immediately visible to search

Visibility usually comes after refresh.

---

## 6. Refresh, Flush, and Translog

These are commonly confused.

### Refresh

Refresh makes recent changes searchable.

Simple meaning:

- open new segment data to search

### Flush

Flush creates a Lucene commit and rolls the translog generation.

Simple meaning:

- stronger persistence step

### Translog

Translog protects acknowledged writes between full Lucene commits.

Simple meaning:

- recovery safety log

### Why This Design Exists

If Lucene committed every document immediately:

- indexing would be much slower

So Elasticsearch uses:

- lightweight searchable refresh
- separate durability protection via translog

---

## 7. Updates and Deletes

Because segments are immutable:

- update = add new version + mark old version deleted
- delete = mark deleted now + reclaim space later

### Practical Consequence

Heavy update workloads create:

- deleted-doc overhead
- more merge pressure

This is a normal part of Lucene behavior.

---

## 8. Merge Lifecycle

Over time, many small segments are created.

Lucene merges them into larger segments.

Why:

- fewer segments to search
- better compression
- reclaim deleted-doc space

Merge cost:

- CPU
- disk IO
- temporary disk usage

Aggressive analyzers like n-grams can increase merge pressure because they produce more indexed terms.

---

## 9. Search Execution Lifecycle

When a query runs:

1. coordinating node receives the request
2. request fans out to relevant shards
3. each shard executes the query locally
4. each shard returns top hits and scores
5. coordinator merges shard results
6. fetch phase loads `_source` or fields for final documents

Two important phases:

- query phase
- fetch phase

### Query Phase

Find matching docs and compute scores.

### Fetch Phase

Retrieve the actual returned document data.

This explains why:

- filtering/counting can be cheaper than returning many large documents

---

## 10. Why Elasticsearch Is Fast

Search speed comes from a combination of:

- sorted term dictionary
- compressed postings
- skip lists
- immutable segments
- filesystem cache
- top-N retrieval instead of full result materialization

### Example

If the query is:

```text
wireless
```

Elasticsearch does not scan every title.

Instead it conceptually does:

```text
lookup "wireless" -> get postings -> score candidates -> fetch top hits
```

---

## 11. BM25 and Relevance

BM25 is the default similarity for text search.

You do not need the full formula to understand the ideas.

BM25 rewards:

- term presence
- rare terms
- useful term repetition
- shorter, focused fields

### Intuition Example

Query:

```text
wireless mouse
```

Document A:

```text
Wireless Mouse
```

Document B:

```text
Everything About Wireless Devices Including Mouse Accessories
```

Document A often scores higher because:

- both terms are present
- field is shorter and more focused

---

## 12. Phonetic Codes and N-Grams Internally

Phonetic codes and n-grams are not stored in a special magical structure.

They also become terms.

### Phonetic Example

If:

```text
smith -> SM0
```

then internally:

```text
SM0 -> [matching docs]
```

### Edge N-Gram Example

If:

```text
mouse -> m, mo, mou, mous, mouse
```

then each prefix becomes a separate term.

This is important because it explains both:

- why these features are fast at query time
- why they increase index size and indexing cost

---

## 13. Performance Cost Drivers

Search speed depends on:

- number of shards
- number of segments
- term selectivity
- query type
- cache warmth
- disk speed
- amount of fetched data

Indexing speed depends on:

- analyzer complexity
- token expansion
- refresh frequency
- merge pressure
- replica count

### Example

A rare exact term query can be cheap.

A fuzzy query on a short common word can be much more expensive.

---

## 14. Filesystem Cache

Filesystem cache matters a lot.

Why:

- Lucene segment files are immutable
- repeated access to hot term/postings files becomes cheaper
- operating system caching can dramatically improve real-world latency

This is why Elasticsearch nodes should not use all RAM only for heap.

They also need room for file cache.

---

## 15. Common Internal Misunderstandings

### "`_source` is what Elasticsearch searches"

Not exactly.

Search mainly runs on indexed terms, not raw document scans.

### "Update rewrites the document in place"

No.

Lucene segments are immutable.

### "More shards means faster"

Not always.

Too many shards increase coordination overhead.

### "N-grams are cheap because search is fast"

Search may be fast, but indexing and storage can become expensive.

---

## 16. Final Summary

Internally, Elasticsearch works because:

- text becomes terms
- terms go into Lucene structures
- terms point to document IDs
- segments keep data searchable and immutable
- refresh exposes new data
- translog protects recent writes
- merges keep the shard efficient over time

If you understand:

- inverted index
- segments
- refresh
- translog
- merge

then you understand most of Elasticsearch internals.
