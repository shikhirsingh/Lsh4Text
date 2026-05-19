# Lsh4Text - A Java implementation of Locality Sensitive Hashing (LSH) for text

Lsh4Text is a Java library for **fast approximate text similarity search** using LSH + MinHash.
This README is written for people who are **not** LSH experts and want a practical way to get started.

If you are new to LSH, these references are helpful:

- Brainly engineering blog: [Locality Sensitive Hashing explained](https://medium.com/engineering-brainly/locality-sensitive-hashing-explained-304eb39291e4)
- Stanford MMDS chapter: [Finding Similar Items (Chapter 3)](http://infolab.stanford.edu/~ullman/mmds/ch3a.pdf)
- MMDS book homepage: [Mining of Massive Datasets](http://infolab.stanford.edu/~ullman/mmds.html)

## Author

- Shikhir Singh

## Dependencies

- Java 8+
- Maven

## Installation

```xml
<dependency>
  <groupId>com.shikhir</groupId>
  <artifactId>Lsh4Text</artifactId>
  <version>3.0.7</version>
</dependency>
```

---

## What problem does LSH solve?

A naive similarity system compares every document against every other document.
That quickly becomes too expensive as your corpus grows.

**LSH belongs to a class of probabilistic algorithms** (you trade some accuracy for speed).
In simple terms:

1. Convert each document into a representation (vector/signature).
2. Hash that representation into multiple buckets.
3. At query time, only inspect documents in matching buckets.

This drastically reduces candidate comparisons because you search a much smaller subset of the corpus first.

In short:

- Without LSH: compare query with almost everything.
- With LSH: compare query with a much smaller candidate set first.

---

## How Lsh4Text is structured

Lsh4Text uses a two-phase approach:

1. **Untrimmed forest**: ingest documents and collect token frequencies.
2. **Trimmed forest**: keep a selected subset of tokens and assign fixed positions in a boolean vector.

Typical production flow:

1. Ingest your training corpus.
2. Build a trimmed forest (dictionary/vector space).
3. For each stored document, compute buckets + signature and index them.
4. For each query, compute query buckets + signature, fetch candidates, then run stronger similarity checks.

### Vocabulary used in this project (plain language)

- **Document**: one input text.
- **Token / shingle**: a small piece of text (word-based or character-based).
- **Vector**: a boolean array; each position represents whether a token from the trimmed forest is present.
- **Signature**: a compact MinHash representation of the vector.
- **Bucket**: a hash-based partition used to quickly find likely candidates.

---

## Parameters you should tune

Text LSH quality depends on dataset-specific tuning. Important knobs:

- **Bucket size**: number of distinct bucket IDs in your LSH space.
- **Number of stages/bands**: how many bucket keys each document receives.
- **Vector size**: number of shingles retained in the trimmed forest.
- **k-shingles / k-grams (`minKGram`, `maxKGram`)**: shingle lengths.
- **`wordTokens`**:
  - `true` for word shingles (common for natural language).
  - `false` for character shingles (helpful for noisy/short text).
- **`similarityError`**: MinHash approximation tradeoff.

There is no universal best setting—test with your own corpus and quality targets.

Good starting points for many text datasets:

- `wordTokens = true`
- `minKGram = 2`, `maxKGram = 3` (or `3,3` for stricter matching)
- `stages = 2` to `4`
- `similarityError = 0.05`

---

## Quick start (step-by-step)

## 1) Create and configure

```java
boolean removeStopWords = true;         // removes words like "the", "and", etc.
boolean removeStopCharacters = true;    // removes punctuation-like stop characters
boolean caseSensitive = false;

Lsh4Text lshText = new Lsh4Text(removeStopWords, removeStopCharacters, caseSensitive);

// Optional: normalize text (e.g., digit-like patterns) before shingling
lshText.setNormalize(true);
```

## 2) Build the untrimmed forest from your corpus

### Option A: load from file

Each line is treated as one document:

```java
lshText.loadFile("test_data_movie_plots.txt", "UTF-8", true, 1, 1);
```

### Option B: add documents incrementally

```java
final int kGramMin = 3;
final int kGramMax = 3;
boolean wordTokens = true;

for (String document : allDocuments) {
    lshText.addDocument(document, wordTokens, kGramMin, kGramMax);
}
```

## 3) Build the trimmed forest

The untrimmed forest may be very large. Build a trimmed forest to define vector space:

```java
int inferredVectorSize = lshText.buildForest();
```

Or explicitly choose vector size:

```java
lshText.buildForest(1500);
```

Tip: Larger vector sizes generally improve discrimination but increase memory usage.
If you are unsure, start small and increase gradually while evaluating precision/recall.

## 4) Index your stored documents into LSH buckets

```java
final int STAGES = 2;
final double similarityError = 0.05;

for (String document : allDocuments) {
    int[] buckets = lshText.getBuckets(document, true, 3, 3, STAGES);
    int[] signature = lshText.getMinHashSignature(document, true, 3, 3, similarityError);
    boolean[] vector = lshText.getVector(document, true, 3, 3);

    for (int bucket : buckets) {
        // Store in your DB/index:
        // key: bucket
        // value: document ID + signature (+ optional vector/text)
    }
}
```

Why signature first? Signatures are usually much smaller than full text and often faster for candidate filtering.
This helps keep query-time candidate scoring cheap.

## 5) Query for similar documents

```java
String query = "This movie is super slow and boring.";

int[] queryBuckets = lshText.getBuckets(query, true, 3, 3, 2);
int[] querySignature = lshText.getMinHashSignature(query, true, 3, 3, 0.05);

for (int bucket : queryBuckets) {
    // Fetch candidate docs/signatures from this bucket
    // 1) Compute signature similarity first
    // 2) Optionally compare vectors
    // 3) Run stronger text-level similarity on finalists
}
```

Recommended stronger checks for re-ranking/verification:

- Jaccard similarity
- Cosine similarity
- Levenshtein similarity/distance

### End-to-end process summary (copy/paste checklist)

1. Create `Lsh4Text` with preprocessing settings.
2. Add training documents (`loadFile` or `addDocument` loop).
3. Build trimmed forest (`buildForest`).
4. For each stored document: compute buckets + signature, store with doc ID.
5. For each query: compute query buckets + signature, fetch candidates.
6. Use `signatureSimilarity` first, then stronger similarity for finalists.

---

## Persistence (save/load forests)

You can export/import forests as JSON:

- `exportUntrimmedForest(File)` / `importUntrimmedForest(...)`
- `exportTrimmedForest(File)` / `importTrimmedForest(...)`

This is useful when you want to train once and reuse forests across service restarts.
It also helps separate offline training from online query serving.

---

## Memory notes

Large corpora can consume significant memory because forest structures keep many shingles.
When done, release resources:

```java
lshText.close();
```

---

## License

- Apache 2.0

## Version history

- 1.0.0 - Initial Release
- 1.0.1 - Small fixes
- 2.0.0 - Major updates to support multiple LSH instances
- 2.0.1 - `Lsh4Text.cleanUntrimmedForest()` added
- 2.0.3 - Bug fixes
- 2.0.4 - Made remove-stop-characters optional via constructor
- 2.0.5 - Added normalization to increase hash collision for similar text
- 2.0.6 - Small bug fixes; better README; Java 8 dependency
- 3.0.7 - Documentation and JavaDoc improvements; README onboarding updates

## Roadmap features

- Optional methods to shrink untrimmed forest by removing leaves/tokens that have similar document-id overlap behavior.
