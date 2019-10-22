package com.shikhir.lsh.trimmed;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import com.shikhir.lsh.untrimmed.forest.shingling.Shingle;
import com.shikhir.lsh.untrimmed.forest.shingling.ShinglingSet;
import com.shikhir.util.stringops.Stopwords;
import com.shikhir.util.stringops.StringOperations;
import com.shikhir.util.stringops.normalize.Normalize;

import info.debatty.java.lsh.LSHMinHash;
import info.debatty.java.lsh.MinHash;

public class TForest {

	TreeMap<Integer, TShingleProperties> trimmedForest = new TreeMap<Integer, TShingleProperties>();

	private boolean removeStopCharacters;
	private boolean normalize;
	private boolean removeStopWords;
	private boolean caseSensitive;
	public static final int LSH_SEED = 1234567890; // Seed chosen

	public TForest() {
		
	}
	
	public TForest(boolean removeStopCharacters, boolean normalize, boolean removeStopWords, boolean caseSensitive) {
		this.removeStopCharacters = removeStopCharacters;
		this.normalize = normalize;
		this.removeStopWords = removeStopWords;
		this.setCaseSensitive(caseSensitive);
	}

	public Sentence getSentence(String document, boolean wordTokens, int minKGrams, int maxKGrams) {
		Shingle[] shinglingArr = getShingleArr(document, wordTokens, minKGrams, maxKGrams, removeStopCharacters, normalize, removeStopWords, caseSensitive);
		float percentage = 1.0f;
		ArrayList<Integer> locationAL = new ArrayList<Integer>();
		ArrayList<Integer> idAL = new ArrayList<Integer>();

		for(Shingle iTkn: shinglingArr) {
			idAL.add(iTkn.getId());
			TShingleProperties prop = trimmedForest.get(iTkn.getId());
			if(prop==null) {
				continue;
			}
			else {
				int location = trimmedForest.get(iTkn.getId()).getLocation();
				locationAL.add(location);
				float tknPercentage = trimmedForest.get(iTkn.getId()).getPercentage();
				percentage = percentage*tknPercentage;
			}
		}
		Sentence retVal = new Sentence(document, percentage, locationAL, idAL );
		return retVal;
	}

	private Shingle[] getShingleArr(String document, boolean wordTokens, int minKGrams, int maxKGrams, 
										boolean removeStopCharacters, boolean normalize, boolean removeStopWords, boolean caseSensitive) {
	    if(StringUtils.isBlank(document)) throw new IllegalArgumentException("document parameter cannot be empty");
		if (this.trimmedForest == null) {
			throw new NullPointerException();
		}

		document = removeStopCharacters?StringOperations.removeStopChar(document):document;
		document = normalize?Normalize.all(document):document;
		document = isCaseSensitive()?document:document.toLowerCase();
		if(removeStopWords) document = Stopwords.removeStopWords(document);

		return ShinglingSet.getTokensForMessage(document, wordTokens, minKGrams, minKGrams);	
	}	
	
	/**
	 * This will get you the vector for a string from the forest. The forest must be
	 * built in order to create a vector. The size of the vector must be defined
	 * during the buildForest process. The forest is build by hashing the shinglings
	 * into an integer and then putting them into a sorted array.
	 * 
	 * @param document The text of the document for which the boolean vector is
	 *                 being created
	 * @param wordTokens if true, tokens of words are assumed, otherwise characters
 	 * @param minKGrams minimum size of shingling
	 * @param maxKGrams maximum size of shingling
	 *                 
	 * @return The vector for the string
	 */

	public boolean[] getVector(String document, boolean wordTokens, int minKGrams, int maxKGrams, 
							   boolean removeStopCharacters, boolean normalize, boolean removeStopWords) {

		Shingle[] shinglingArr = getShingleArr(document, wordTokens, minKGrams, maxKGrams, removeStopCharacters, normalize, removeStopWords, caseSensitive);
		
		int forestSize = this.trimmedForest.size();
		boolean[] vector = new boolean[forestSize];

		for(Shingle s: shinglingArr) {
			TShingleProperties index = trimmedForest.get(s.getId());
			if(index!=null) {
				vector[index.getLocation()]=true;
			}
		}		
		return vector;
	}
	
	
	public void add(Integer id, Float percentage) {
		TShingleProperties property = new TShingleProperties(percentage);
		trimmedForest.put(id, property);
	}
	
	
	public void finalize() {
		int i=0;
        for (Map.Entry<Integer,TShingleProperties> entry : trimmedForest.entrySet()) {
        	Integer key = entry.getKey();
        	TShingleProperties prop = entry.getValue();
        	prop.setLocation(i);
        	trimmedForest.put(key, prop);
        	i++;
        }

	}
	
	/**
	 * This will get you the vector for a string from the forest. The forest must be
	 * built in order to create a vector. The size of the vector must be defined
	 * during the buildForest process. The forest is build by hashing the shinglings
	 * into an integer and then putting them into a sorted array.
	 * 
	 * @param document The text of the document for which the boolean vector is
	 *                 being created
	 * @param wordTokens if true, tokens of words are assumed, otherwise characters
 	 * @param minKGrams minimum size of shingling
	 * @param maxKGrams maximum size of shingling
	 *                 
	 * @return The vector for the string
	 */
	public boolean[] getVector(String document, boolean wordTokens, int minKGrams, int maxKGrams) {
	    if(StringUtils.isBlank(document)) throw new IllegalArgumentException("document parameter cannot be empty");

		document = removeStopCharacters?StringOperations.removeStopChar(document):document;
		document = normalize?Normalize.all(document):document;
		
		if(removeStopWords) {
			document = Stopwords.removeStopWords(document);
		}
		document = isCaseSensitive()?document: document.toLowerCase();

		ShinglingSet set = new ShinglingSet();
		set.addShingling(document, wordTokens, minKGrams, maxKGrams);

		Integer[] idsArry = set.getAllId();

		int forestSize = this.trimmedForest.size();
		boolean[] vector = new boolean[forestSize];

		for(Integer id: idsArry) {
			TShingleProperties prop = trimmedForest.get(id);
			if(prop==null) continue;
			Integer location = trimmedForest.get(id).getLocation();
			if(location!=null) {
				vector[location]=true;
			}
		}
		return vector;
	}
	

	/**
	 * The a bucket size is automatically estimated if not provided in the
	 * getBuckets() function. This function estimates a bucket size.
	 * 
	 * @return returns the default bucket size based on the size of the forest()
	 */
	
	public int defaultBucketSize() {
		final int BUCKET_MULTIPLIER = 3;
		int bucketSize = (int) (Math.sqrt(trimmedForest.size()) * BUCKET_MULTIPLIER);
		return bucketSize;
	}
	
	/**
	 * The MinHash signature of the text created by using the forest vector
	 * 
	 * @param document The text of the document for which the signature is being created
	 * @param wordTokens if true, tokens of words are assumed, otherwise characters
 	 * @param minKGram minimum size of shingling
	 * @param maxKGram maximum size of shingling
	 * @param similartyError The similarity error 
	 * @return Gets the MinHash signature of the documents
	 */
	
	public int[] getMinHashSignature(String document, boolean wordTokens, int minKGram, int maxKGram, double similartyError) {
	    if(StringUtils.isBlank(document)) throw new IllegalArgumentException("document parameter cannot be empty");

		MinHash minhash = new MinHash(similartyError, trimmedForest.size(), TForest.LSH_SEED);
		return minhash.signature(getVector(document, wordTokens, minKGram, maxKGram));
	}

	/**
	 * Count the number of shinglings in the forest
	 * 
	 * @param document The text of the document for which the signature is being created
	 * @param wordTokens if true, tokens of words are assumed, otherwise characters
 	 * @param minKGram minimum size of shingling
	 * @param maxKGram maximum size of shingling
	 * @return Gets the MinHash signature of the documents
	 */
	
	public int countDocumentShinglingsInForest(String document, boolean wordTokens, int minKGram, int maxKGram) {
	    if(StringUtils.isBlank(document)) throw new IllegalArgumentException("document parameter cannot be empty");

		document = removeStopCharacters?StringOperations.removeStopChar(document):document;
		document = normalize?Normalize.all(document):document;
		
		if(removeStopWords) document = Stopwords.removeStopWords(document);

		ShinglingSet set = new ShinglingSet();
		set.addShingling(document, wordTokens, minKGram, maxKGram);

		Integer[] idsArry = set.getAllId();

		int found=0;
		for(Integer id: idsArry) {
			Integer location = trimmedForest.get(id).getLocation();
			if(location!=null) {
				found++;
			}
		}
		return found;
	}
	

	
	/**
	 * Using two MinHash signatures, you can compute the similarity of the
	 * signatures. Looking at the similarity of the signatures can be a faster
	 * alternative to looking at the entire corpus of document and comparing their
	 * similarity. Although it's not as accurate
	 * 
	 * @param sig1            The MinHash signature of the first document vector
	 * @param sig2            The MinHash signature of the second document vector
	 * @param similarityError The similarity error
	 * @return The similarity of the two signatures
	 * 
	 */

	public double signatureSimilarity(int[] sig1, int[] sig2, double similarityError) {
		MinHash minhash = new MinHash(similarityError, this.trimmedForest.size(), LSH_SEED);
		return minhash.similarity(sig1, sig2);
	}

	
	private static int[] removeDuplicates(int[] iArr) {
		// remove duplicates buckets
		for (int i = 0; i < iArr.length - 1; i++) {
			for (int j = i + 1; j < iArr.length; j++) {
				if (iArr[i] == iArr[j]) {
					iArr = ArrayUtils.remove(iArr, j);
				}
			}
		}
		return iArr;
	}
	
	/**
	 * Returns all the possible buckets which may contain this text. This function
	 * must be used with same stages and bucketSize parameters for all searches in
	 * order to get a valid result. You will need to store the signature / content
	 * key/value in the bucket and then do a signature similarity. If the signatures
	 * are similar, you should then do a stronger test of similarity such as
	 * Levenshtein distance or cosine similarity on the actual body of the document.
	 * A default bucket is estimate for convenience.
	 * 
	 * @param document The document which needs to be analyzed
	 * @param wordTokens if true, tokens of words are assumed, otherwise characters
 	 * @param minKGrams minimum size of shingling
	 * @param maxKGrams maximum size of shingling
	 * @param stages   determines the number of possible buckets
	 * @return returns list of possible buckets which may contain this text.
	 */
	public int[] getBuckets(String document, boolean wordTokens, int minKGrams, int maxKGrams, int stages) {
		return getBuckets(document, wordTokens, minKGrams, maxKGrams, stages, defaultBucketSize());
	}

	/**
	 * Returns all the possible buckets which may contain this text. This function
	 * must be used with same stages and bucketSize parameters for all searches in
	 * order to get a valid result. You will need to store the signature / content
	 * key/value in the bucket and then do a signature similarity. If the signatures
	 * are similar, you should then do a stronger test of similarity such as
	 * Levenshtein distance or cosine similarity on the actual body of the document.
	 * 
	 * @param document   The document which needs to be analyzed
	 * @param wordTokens if true, tokens of words are assumed, otherwise characters
 	 * @param minKGrams minimum size of shingling
	 * @param maxKGrams maximum size of shingling
	 * @param stages     determines the number of possible buckets
	 * @param bucketSize The document for which the boolean vector is being created
	 * @return returns list of possible buckets which may contain this text.
	 */
	public int[] getBuckets(String document, boolean wordTokens, int minKGrams, int maxKGrams, int stages, int bucketSize) {
	    Objects.requireNonNull(document, "document must not be null");
	    if(StringUtils.isBlank(document)) throw new IllegalArgumentException("document parameter cannot be empty");
	    
		boolean vector[] = getVector(document, wordTokens, minKGrams, maxKGrams);

		// Create and configure LSH algorithm
		LSHMinHash lsh = new LSHMinHash(stages, bucketSize, vector.length, TForest.LSH_SEED);
		int[] buckets = lsh.hash(vector);
		Arrays.sort(buckets);

		// remove duplicates buckets
		return removeDuplicates(buckets);
	}

	
	public void printTrimmedForest() {
        for (Map.Entry<Integer,TShingleProperties> entry : trimmedForest.entrySet()) {
        	System.out.println(entry.getKey()+" -> "+entry.getValue().getPercentage()+" -> "+ entry.getValue().getLocation());
        }
	}

	public int size() {
		return trimmedForest.size();
	}
	
	public boolean isRemoveStopCharacters() {
		return removeStopCharacters;
	}

	public void setRemoveStopCharacters(boolean removeStopCharacters) {
		this.removeStopCharacters = removeStopCharacters;
	}

	public boolean isRemoveStopWords() {
		return removeStopWords;
	}

	public void setRemoveStopWords(boolean removeStopWords) {
		this.removeStopWords = removeStopWords;
	}

	public boolean isNormalize() {
		return normalize;
	}

	public void setNormalize(boolean normalize) {
		this.normalize = normalize;
	}


	public boolean isCaseSensitive() {
		return caseSensitive;
	}


	public void setCaseSensitive(boolean caseSensitive) {
		this.caseSensitive = caseSensitive;
	}

	public TreeMap<Integer, TShingleProperties> getTrimmedForest() {
		return trimmedForest;
	}

	public void setTrimmedForest(TreeMap<Integer, TShingleProperties> trimmedForest) {
		this.trimmedForest = trimmedForest;
	}

}
