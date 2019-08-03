package com.shikhir.lsh.str;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.ArrayUtils;

import com.shikhir.lsh.forest.ForestShingle;
import com.shikhir.lsh.shingling.Shingle;
import com.shikhir.lsh.shingling.ShinglingSet;
import com.shikhir.util.stringops.Stopwords;

import info.debatty.java.lsh.LSHMinHash;
import info.debatty.java.lsh.MinHash;
import me.lemire.integercompression.differential.IntegratedIntCompressor;

public class Lsh4Text {

	private  TreeMap<Integer, ForestShingle> untrimmedForestMap = new TreeMap<Integer, ForestShingle>();

	private  Integer[] forest = null;
	private static final int RECOMMENDED_VECTOR_SIZE = 1000;

	private boolean removeStopWords=false;
	private static final Logger log = Logger.getLogger(Lsh4Text.class.getName());

	private static final int LSH_SEED = 1234567890; // Seed chosen

	public Lsh4Text() {
	}

	/**
	 * Returns the Jaccard Similarity of two different vectors
	 * 
	 * @param removeStopwords Removes stop words while creating a vector or inputting into forest
	 */
	public Lsh4Text(boolean removeStopwords) {
		this.removeStopWords = removeStopwords;
	}
	
	private static String removeStopChar(String text) {
		return text.replaceAll("[.,:*;!()'-]", "").replaceAll("\\s+"," ");
	}
	
	/**
	 * The a bucket size is automatically estimated if not provided in the
	 * getBuckets() function. This function estimates a bucket size.
	 * 
	 * @return returns the default bucket size based on the size of the forest()
	 */
	public int defaultBucketSize() {
		final int BUCKET_MULTIPLIER = 3;
		int bucketSize = (int) (Math.sqrt(forest.length) * BUCKET_MULTIPLIER);
		// if(bucketSize > 10) bucketSize = (int) Math.round((bucketSize)/10.0) * 10; //
		// rounds to the nearest 10
		return bucketSize;
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
	 * Returns the Jaccard Similarity of two different vectors
	 * 
	 * @param vector1 The document which needs to be analyzed
	 * @param vector2   determines the number of possible buckets
	 * @return Jaccard Similarity of the vectors.
	 */
	public static double jaccardSimilarity4Vectors(boolean[] vector1, boolean[] vector2) {
        return MinHash.jaccardIndex(vector1, vector2);
	}
	
	
	/**
	 * decompresses, decodes and loads a forest from base64 encoded string
	 * 
	 * @param encoded a base64 encoded string which was created by the
	 *                encodeForestAsBase64()
	 */
	public void decodeForestFromBase64(String encoded) {
		byte[] decoded = Base64.getDecoder().decode(encoded);

		IntBuffer intBuf = ByteBuffer.wrap(decoded).order(ByteOrder.LITTLE_ENDIAN).asIntBuffer();
		int[] array = new int[intBuf.remaining()];
		intBuf.get(array);

		IntegratedIntCompressor iic = new IntegratedIntCompressor();
		int[] uncompressed = iic.uncompress(array); // equals to data

		this.forest = new Integer[uncompressed.length];
		for (int i = 0; i < uncompressed.length; i++) {
			this.forest[i] = uncompressed[i];
		}
	}

	/**
	 * compresses encodes the entire Forest as base64 for easy storage.
	 * 
	 * @return returns an base64 encoded value of forest
	 */
	public String encodeForestAsBase64() {

		int ints[] = new int[forest.length];

		for (int i = 0; i < forest.length; i++) {
			ints[i] = forest[i];
		}

		System.out.println("uncompressed Int - " + ints.length);
		IntegratedIntCompressor iic = new IntegratedIntCompressor();
		int[] compressed = iic.compress(ints); // compressed array
		System.out.println("Compressed Int - " + compressed.length);

		java.nio.ByteBuffer bb = java.nio.ByteBuffer.allocate(compressed.length * 4);
		bb.order(ByteOrder.LITTLE_ENDIAN).asIntBuffer().put(compressed);
		return Base64.getEncoder().encodeToString(bb.array());
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

		boolean vector[] = getVector(document, wordTokens, minKGrams, maxKGrams);

		// Create and configure LSH algorithm
		LSHMinHash lsh = new LSHMinHash(stages, bucketSize, vector.length, LSH_SEED);
		int[] buckets = lsh.hash(vector);
		Arrays.sort(buckets);

		// remove duplicates buckets
		return removeDuplicates(buckets);
	}

	/**
	 * Returns untrimmed forest as an ArrayList
	 * 
	 * @return Returns the full untrimmed forest.
	 */
	private ArrayList<ForestShingle> getUntrimmedForest(boolean decending) {
		if (untrimmedForestMap == null)
			throw new NullPointerException();

		ArrayList<ForestShingle> forestMapValues = new ArrayList<ForestShingle>(untrimmedForestMap.values());

		if (decending) {
			Collections.sort(forestMapValues, Collections.reverseOrder());
		} else {
			Collections.sort(forestMapValues);
		}
		return forestMapValues;
	}

	/**
	 * Returns the number of unique shinglings in an untrimmed forest
	 * 
	 * @return count of unique shinglings in untrimmed forest.
	 */
	public int untrimmedForestSize() {
		if (untrimmedForestMap == null)
			throw new NullPointerException();
		else
			return untrimmedForestMap.size();
	}

	/**
	 * An untrimmed forest is sorted by frequency of shinglings found in all
	 * documents. This method will find the index in the array where the frequency
	 * count is less than or equal to the value of the parameter. This should be
	 * used to determine the size of the vector.
	 * 
 	 * countNumber The frequency count of the token
	 * 
	 * @return The frequency could to find in an untrimmed forest.
	 */
	public int findCountofIndexInUntrimmedForest(int countNumber) {
		ArrayList<ForestShingle> forest = getUntrimmedForest(true);

		for (int i = 0; i < forest.size(); i++) {
			if (forest.get(i).getShingleCountInForest() <= countNumber) {
				return i;
			}
		}
		return forest.size();
	}
	
	/**
	 * An untrimmed forest is sorted by frequency of shinglings found in all
	 * documents and all shinglings less than or equal to the count number are removed
	 * 
 	 * countNumber The frequency count of the token
 	 */	
	public void removeLessThanFrequency(int countNumber) {
		ArrayList<ForestShingle> forest = getUntrimmedForest(true);
		TreeSet<Integer> ts = new TreeSet<Integer>();
		
		for(ForestShingle sh: forest) {
			if(sh.getShingleCountInForest()<= countNumber) {
				ts.add(sh.getId());
			}
		}

		for(int i: ts) {
			untrimmedForestMap.remove(i);
		}
	}

	/**
	 * This will print all the shingles from a forest.
	 */
	public void printTrimmedForest() {
		for (int i : this.forest) {
			System.out.println(i);
		}
	}

	/**
	 * This will print all the shingles and their count from the unTrimmed forest in descending order of frequency count.
	 * This could be used to identify the vector size needed to build a forest.
	 * 
	 * @param head the count of top shingles to be returned
	 */
	public void printTopShingleAndCount(int head) {
		if(untrimmedForestMap.size()<head) throw new IllegalArgumentException();
		
		ArrayList<ForestShingle> forest = getUntrimmedForest(true);

		for(int i=0; i<head; i++) {
			System.out.println(forest.get(i).getToken()+ " → "+ forest.get(i).getShingleCountInForest());
		}
	}

	/**
	 * This will build a forest using all the shinglings. This should only be used
	 * if the size of the forest is small (less than 1000) or there is very little
	 * redundancy.
	 * 
	 * @return The forest is returned as an array of integers
	 */
	public Integer[] getForest() {
		if (this.forest == null)
			throw new NullPointerException();
		return this.forest;
	}

	/**
	 * This will build a forest using all the shinglings. This should only be used
	 * if the size of the forest is small (less than 1000) or there is very little
	 * redunancy.
	 */
	public void buildFullForest() {
		buildForest(untrimmedForestMap.size());
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
		if (this.forest == null)
			throw new NullPointerException();

		document = removeStopChar(document);
		if(removeStopWords) Stopwords.removeStopWords(document);


		ShinglingSet set = new ShinglingSet();
		set.addShingling(document, wordTokens, minKGrams, maxKGrams);

		boolean[] vector = new boolean[this.forest.length];

		for (int i = 0; i < this.forest.length; i++) {
			vector[i] = set.contains(forest[i]);
		}

		return vector;
	}

	/**
	 * The MinHash signature of the text created by using the forest vector
	 * 
	 * @param document The text of the document for which the signature is being
	 *                 created
	 * @param wordTokens if true, tokens of words are assumed, otherwise characters
 	 * @param minKGram minimum size of shingling
	 * @param maxKGram maximum size of shingling
	 * @param similartyError The similarity error 
	 * @return Gets the MinHash signature of the documents
	 */
	public int[] getMinHashSignature(String document, boolean wordTokens, int minKGram, int maxKGram, double similartyError) {
		if (this.forest == null)
			throw new NullPointerException();

		MinHash minhash = new MinHash(similartyError, forest.length, LSH_SEED);
		return minhash.signature(getVector(document, wordTokens, minKGram, maxKGram));
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
		MinHash minhash = new MinHash(similarityError, this.forest.length, LSH_SEED);
		return minhash.similarity(sig1, sig2);
	}

	private int getDefaultVector() {
		if (this.untrimmedForestMap.size() < 800) {
			return this.untrimmedForestMap.size() - 1;
		}
		int duplicateIndex = findCountofIndexInUntrimmedForest(1);
		if (duplicateIndex < 1200)
			return duplicateIndex;
		return RECOMMENDED_VECTOR_SIZE;

	}

	/**
	 * Builds a trimmed forest from using a untrimmed forest by removing all the
	 * leafs that had the lowest frequency of use. The a default vector size of less
	 * than 1200 is used.
	 * 
	 */
	public void buildForest() {
		buildForest(getDefaultVector());
	}

	/**
	 * Builds a trimmed forest of vectorSize from using a untrimmed forest by
	 * removing all the leafs that had the lowest frequency of use
	 * 
	 * @param vectorSize The size of the vector used to build the forest
	 */
	public void buildForest(int vectorSize) {

		if (vectorSize > untrimmedForestMap.size()) {
			throw new IllegalArgumentException();
		}

		ArrayList<ForestShingle> unTrimmedForest = getUntrimmedForest(true);
		forest = new Integer[vectorSize];

		for (int j = 0; j < vectorSize; j++) {
			forest[j] = unTrimmedForest.get(j).getId();
		}

		Arrays.sort(forest);
		unTrimmedForest = null;
		untrimmedForestMap = null; // releasing to free up memory
		System.gc();
	}

	/**
	 * Load the forest with provided integers
	 * 
	 * @param forestInt An array of integers that contain the forest
	 */
	public void loadForest(Integer[] forestInt) {
		this.forest = forestInt;
		Arrays.sort(this.forest);
	}

	/**
	 * Adds the document to the forest by creating shingles of the documents and
	 * then adding it to the forest. If the machine does not have enough memory, you
	 * may see an out of memory error if your forest gets too big. This method does not
	 * look for any separator, which can result in a longer list of shinglings
	 * 
	 * @param document The text of the document
	 * @param wordTokens The text is tokenized by charecters instead of words
 	 * @param minKGram minimum size of shingling
	 * @param maxKGram maximum size of shingling
	 */
	public void addDocumentToUntrimmedForest(String document, boolean wordTokens, int minKGram, int maxKGram) {
		document = removeStopChar(document);
		if(this.removeStopWords) {
			document=Stopwords.removeStopWords(document);
		};
		forest = null;
		Shingle[] documentShingles = ShinglingSet.getTokensForMessage(document, wordTokens, minKGram, maxKGram);

		for (Shingle s : documentShingles) {
			ForestShingle fs = untrimmedForestMap.get(s.getId());
			if (fs == null) fs = new ForestShingle(s.toString(), 0);

			fs.increment();
			untrimmedForestMap.put(fs.getId(), fs);
		}
	}


	/**
	 * Creates forest from a text file by tokenizing the text file. Each line is
	 * assumed to be its own document.
	 * 
	 * @param fileName  The name of the file which will be read to create the forest
	 * @param encoding The encoded file type (ie. UTF-8)
	 * @param wordTokens If true, the documents will be tokenized using words, otherwise characters
	 * @param kGramsMin The minimum number of k-Grams used
	 * @param kGramsMax The maximum number of k-Grams used
	 * @return The size of the forest
	 */
	public int loadFile(String fileName, String encoding, boolean wordTokens, int kGramsMin, int kGramsMax) throws IOException {
		File tldlist = FileUtils.getFile(fileName);

		try {
			LineIterator it = FileUtils.lineIterator(tldlist, encoding);

			while (it.hasNext()) {
				String text = it.nextLine().replace("\"", "");
				addDocumentToUntrimmedForest(text, wordTokens, kGramsMin, kGramsMax);
			}
			it.close();
		} finally {
		}

		return untrimmedForestMap.size();
	}


	/**
	 * Give you the levenshtein similarity of two documents as a percentage. Levenshtein Similarty is a
	 * measure of similarity of two strings. 
	 * See wikipedia an explanation of Levenshtein Distance.
	 * 
	 * @param document1 The text of the first document to compare
	 * @param document2 The text of the second document to compare
	 * 
	 * @return The distance as a percentage
	 */

	public static double levenshteinSimilarity(String document1, String document2) {
		int a[][] = opennlp.tools.util.StringUtil.levenshteinDistance(document1, document2);
		double percentage_difference = (double) 1
				- (double) a[document1.length()][document2.length()] / Math.max(document1.length(), 
						document2.length());
		return percentage_difference;
	}
	
	
	
	/**
	 * Removes duplicates from untrimmed forest. This function is useful when the encoding is by characters instead of words
	 * 
	 * @param percentage The percentage of frequency count a token must be in the range of in order to remove
	 */

	public void clearnUntrimmedForest(int percentage) {
		
	    ForestShingle[] values = untrimmedForestMap.values().toArray(new ForestShingle[untrimmedForestMap.size()]);
	    
	    Arrays.sort(values, Collections.reverseOrder());

	    for(int i=0; i< values.length; i++) {
	    	int iCount = values[i].getShingleCountInForest();
	    	String iToken = values[i].getToken().replace("[","").replace("]", "");

    		if(i%1000==0) {
    			float outputPercent = (float) (100.0*i/values.length);
    			String formattedString = String.format("%.02f", outputPercent);
    			if(values.length>10000) log.info(formattedString+"% done ");
    		}

	    	
		    for(int j=i; j<values.length; j++) {
		    	String jToken = values[j].getToken().replace("[","").replace("]", "");
		    	
		    	if(!iToken.equals(jToken)){
			    	int jCount = values[j].getShingleCountInForest();
			    	int p = iCount-jCount==0?0:(int) Math.abs((100 * (((double)iCount - (double)jCount) / (double)jCount)));
		    	
			    	if(p > percentage) {
			    		break;
			    	};
			    	
		    		if(jToken.contains(iToken) && p <= percentage) {
		    	    	untrimmedForestMap.remove(values[i].getId());
		    			break;
		    		}
		    	}
		    }

	    }	    
	}

	/**
	 * Releases resources
	 */
	public void close() {
		this.forest=null;
		this.untrimmedForestMap=null;
		System.gc();
	}
}
