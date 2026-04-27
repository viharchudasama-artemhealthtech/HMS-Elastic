# Basic Elasticsearch

## Goal

This guide explains Elasticsearch in a simple but correct way.

It answers:

- what Elasticsearch is
- what problem it solves
- what the main words mean
- how a document becomes searchable
- how simple queries work

---

## 1. What Elasticsearch Is

Elasticsearch is a search engine built on Apache Lucene.

Simple meaning:

- it stores JSON documents
- it prepares them for search
- it returns relevant results fast

A database is often strongest at:

- exact record lookup
- joins
- transactions

Elasticsearch is strongest at:

- full-text search
- autocomplete
- typo tolerance
- ranking
- filtering plus search together

So Elasticsearch is best thought of as:

- a search engine with a JSON API

---

## 2. Main Words Explained

### Document

One JSON object.

Example:

```json
{
  "id": 1,
  "title": "Paracetamol",
  "brand": "SunFarma",
  "price": 10
}
```

### Field

One property inside a document.

Examples:

- `title`
- `brand`
- `price`

### Index

A collection of similar documents.

Example:

- `medicines`

### Token

A piece of text created by analysis.

Example:

```text
"Paracetamol 500 test" -> ["Paracetamol", "500" , "test"]
```

### Term

The final indexed value that search uses.

Simple idea:

- terms are what Elasticsearch actually looks up

### Mapping

Rules that tell Elasticsearch how each field should behave.

### Analyzer

The text-processing pipeline that converts raw text into searchable terms.

---

## 3. What Problem Elasticsearch Solves

Elasticsearch is useful when users do not type perfect exact values.

Examples:

- user types part of a title
- user makes a typo
- user wants fast suggestions while typing
- user wants search plus filters together

Example:

If your document has:

```text
Paracetamol test
```

the user may search:

- `para`
- `paracitomol`
- `test`

A good search system should still help the user find the right result.

---

## 4. Mapping: How Fields Behave

### `text`

Used for full-text search.

Example:

```json
"title": "Paracetamol 500"
```

This is analyzed into tokens like:

```text
paracetamol
500
```

### `keyword`

Used for exact values.

Example:

```json
"brand": "SunFarma"
```

This is stored as one exact value, not broken into tokens.

### Numeric Fields

Examples:

- `integer`
- `double`

Used for:

- sorting
- ranges
- aggregations

### `boolean`

Used for:

- true/false filters

Example:

```json
"inStock": true
```

---

## 5. Analyzer: How Text Becomes Searchable

An analyzer works in this order:

```text
raw text
-> character filters
-> tokenizer
-> token filters
-> final terms
```

### Character Filters

Change raw text before splitting.

Example:

```text
"Men's-Shoes" -> "Mens Shoes"
```

### Tokenizer

Splits text into tokens.

Example:

```text
"Wireless Mouse" -> ["Wireless", "Mouse"]
```

### Token Filters

Change tokens after splitting.

Example:

```text
["Wireless", "Mouse"] -> ["wireless", "mouse"]
```

Common token filters:

- lowercase
- stopword
- stemming
- synonym

---

## 6. Inverted Index: Why Search Is Fast

This is the most important concept.

Normal human thinking:

```text
doc1 = wireless mouse
doc2 = gaming keyboard
doc3 = wireless charger
```

Elasticsearch stores a search-friendly version like:

```text
wireless -> [doc1, doc3]
mouse -> [doc1]
gaming -> [doc2]
keyboard -> [doc2]
charger -> [doc3]
```

This is called an inverted index.

Why it matters:

- Elasticsearch does not scan every document for every search
- it jumps directly from a term to matching document IDs

---

## 7. What Happens When You Index a Document

Suppose you send:

```json
{
  "id": 1,
  "title": "Wireless Mouse",
  "brand": "LogiTech",
  "price": 1299,
  "inStock": true
}
```

High-level flow:

1. Elasticsearch receives the JSON
2. mapping decides how each field should be handled
3. `title` is analyzed into terms
4. exact fields are stored exactly
5. terms are written into index structures
6. document becomes searchable after refresh

---

## 8. What Happens When You Search

Suppose the user searches:

```text
wireless mouse
```

High-level flow:

1. query is analyzed
2. Elasticsearch looks up terms
3. matching documents are found
4. scores are calculated
5. top results are returned

This is why search can be fast even when there are many documents.

---

## 9. Common Query Types

### `match`

Used for full-text fields.

Example:

```json
{
  "match": {
    "title": "wireless mouse"
  }
}
```

### `term`

Used for exact fields.

Example:

```json
{
  "term": {
    "brand": "LogiTech"
  }
}
```

### `range`

Used for numbers and dates.

Example:

```json
{
  "range": {
    "price": {
      "lte": 1500
    }
  }
}
```

### `bool`

Combines query parts.

Example:

```json
{
  "bool": {
    "must": [
      { "match": { "title": "wireless mouse" } }
    ],
    "filter": [
      { "term": { "inStock": true } }
    ]
  }
}
```

---

## 10. Simple End-to-End Example

Document:

```json
{
  "id": 1,
  "title": "Wireless Mouse",
  "brand": "LogiTech",
  "price": 1299,
  "inStock": true
}
```

Mapping idea:

- `title` -> `text`
- `brand` -> `keyword`
- `price` -> `integer`
- `inStock` -> `boolean`

Search:

```json
{
  "bool": {
    "must": [
      { "match": { "title": "wireless" } }
    ],
    "filter": [
      { "term": { "inStock": true } }
    ]
  }
}
```

Meaning:

- find documents whose title matches `wireless`
- only keep documents where `inStock = true`

---

## 11. Beginner Mistakes to Avoid

- mapping every field as `text`
- using `term` query on analyzed text fields by accident
- forgetting that exact fields should usually be `keyword`
- expecting raw `_source` to behave like the search structure

---

## 12. Final Summary

If you remember only the basics, remember this:

- documents contain fields
- mappings decide field behavior
- analyzers convert text into terms
- the inverted index connects terms to documents
- search is fast because Elasticsearch looks up terms directly
