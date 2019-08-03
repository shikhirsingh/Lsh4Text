 Lsh4Text - A Java Implementation of the Locality sensitive hashing algorithm

This library makes is simple to use [LSH](https://medium.com/engineering-brainly/locality-sensitive-hashing-explained-304eb39291e4) (Locality sensitive hashing) for text documents. [Locality Sensitive Hashing](https://medium.com/engineering-brainly/locality-sensitive-hashing-explained-304eb39291e4) is a probabilistic algorithm to find similar documents without scanning each documents one by one to determine if they are similar.


**Author**

* Shikhir Singh

**Dependencies to compile**

* Java 8+ 

**How to Install**

Maven - be sure to check for latest version in Maven:

```
<dependency>
  <groupId>com.shikhir</groupId>
  <artifactId>Lsh4Text</artifactId>
  <version>2.0.4</version>
</dependency>
```

**How LSH Works**

LSH belongs to a class of probabilistic algorithms. In it's simplest form, LSH works by grouping all of your documents (or the document's signature) into n buckets. Each document (or it's signature) needs to be stored in multiple buckets. When you are looking for a document that could be similar, you check each of the buckets LSH asks you to check. The advantage of using LSH is that you don't need to search the entire dataset one by one, just the buckets. Because you only need to search a small number of buckets, it can significantly reduces the size and dimension of the problem. For a more detailed explanation please see [this chapter](http://infolab.stanford.edu/~ullman/mmds/ch3a.pdf) from [Mining Massive Datasets](http://infolab.stanford.edu/~ullman/mmds.html) book. Another explanation is provided in [this blog](https://medium.com/engineering-brainly/locality-sensitive-hashing-explained-304eb39291e4). 

**Parameters You Will Want to Change**
There are a number of parameters that go into a text LSH algorithm. In this implementation of LSH, some of these are auto computed for you. You will want to try different numbers based on your dataset. I encourage you to try different numbers. 

* Bucket Size - How many different buckets that exist
* Number of Possible Buckets for each document (i.e Bands)
* Vector Size - Number of unique kgrams - 
* K-Shingles or kGram size - the minimum and maximum number of k-grams

**Get me started**

* First, you will need to create a forest which contains shinglings (i.e. words) of all your documents. Do do this, you can either load a file containing the document or add a document one by one. Here is how you load of file to create an untrimmed forest. 

```
	Lsh4Text lshText = new Lsh4Text();
	try {
		Lsh4Text lshText = new Lsh4Text(true, true);
		lshText.loadFile("test_data_movie_plots.txt", "UTF-8", true, 1, 1);
	} catch (IOException e) {
		fail("could not find test file");
	}
```
This command above creates an untrimmed forest from a file. The loadFile assumes that each line of the text file is it's own document. 

You can also load the documents one by one 

```
	final int kGramMin = 3; // three is a good value to get started. Lower this if you aren't getting good results
	final int kGramMax = 3;
	String document = loadStringFromSomewhere();
	lshText.addDocumentToUntrimmedForest(document, true, kGramMin, kGramMax) {

```

* After the untrimmed forest is built, you will need to trim the forest. An untrimmed forest contains all the shinglings(words) from all the documents, which is too huge. In order to trim a forest, you will need to look at the frequency counts of the shinglings. You can do this using the Lsh4Text.findCountofIndexInUntrimmedForest(FREQUENCY_COUNT) function. Alternatively, you can just use the default values. The Lsh4Text.buildForest method without any params will guess at default values. WARNING: the default values will not be sufficient if you have a huge number of documents. Do your homework here. 

```
	lshText.buildForest()
```

* After your forest is built, you will need to put all your documents(and/or their signatures and vectors) into buckets. Because a document signature is typically smaller than the full document, it's often faster put the signatures into a bucket and check for signature similarity. Sometimes, signatures of the documents are too big. Can you control the size of the signature by adjusting the similarityError parameter. If you store the vectors of each document in the bucket, you could also check the vectors for similarity. If your documents are huge, this could be much quicker. 

```
	final int NUMBER_OF_BUCKETS=2;
	
	for(String document: allDocuments){
		int buckets[] = lshText.getBuckets(document, true, kGramMin, kGramMax, NUMBER_OF_BUCKETS);
		for(int eachBucket: buckets){
			int[] signature = lshText.getMinHashSignature(document, true, kGramMin, kGramMax, similarityError);
			
			// Add all document and/or document signature into each of the buckets
			// The same document will go into multiple buckets
			// You will probably be doing this is a key/value database
			
			// mydatabase.insert(eachBucket, signature, document, documentVector);
		}
	}
```

* In order to find a document, you will need get all of it's possible buckets (via the Lsh4Text.getBuckets method) and then search each bucket one by one for a signature similarity, vector similarity, or document similarity. Good ways to check for document similarity are Jaccard Similarity, Cosine Similarity, or Levenshtein Distance. 

```
	String documentToSearch = "This movie is super slow and boring.";
	int[] docToSearchSignature = lshText.getMinHashSignature(documentToSearch, true, kGramMin, kGramMax, similarityError);
	
	int possible_buckets[] = lshText.getBuckets(documentToSearch, true, kGramMin, kGramMax, NUMBER_OF_BUCKETS););

	for(int searchBucket: possible_buckets){
		// get a list of signatures each representing a document in the bucket
		// do a jacarrd similarity for each signature against docToSearchSignature
		// if there is a possible match, do a jacarrd similarity for the vectors
		// if there is a match, do a similarity test on the documents
	}
```
* LSH4Text takes up a lot of memory due to the size of the forest. Close it when you are done to avoid out of memory errors. 

```
	lsh.close()
```

**LICENSE**
* Apache 2.0

**Version History**

* 1.0.0 - Initial Release
* 1.0.1 - Small fixes
* 2.0.0 - Major Updates to support multiple LSH instances
* 2.0.1 - Lsh4Text.clearnUntrimmedForest() added
* 2.0.3 - Bug fixes
* 2.0.4 - Made remove stop characters optional

**Roadmap Features**

* New optional methods to shrink size of untrimmed forest by removing leaves/tokens that have the same jaccard similarity of documents ids
