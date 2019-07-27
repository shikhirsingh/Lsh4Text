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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.ArrayUtils;

import com.shikhir.lsh.forest.ForestShingle;
import com.shikhir.lsh.shingling.ShinglingSet;

import info.debatty.java.lsh.LSHMinHash;
import info.debatty.java.lsh.MinHash;
import me.lemire.integercompression.differential.IntegratedIntCompressor;

public class Lsh4Text {

	private static TreeMap<Integer, ForestShingle> untrimmedForestMap = new TreeMap<Integer, ForestShingle>();

	private static Integer[] forest = null;
	private static final int RECOMMENDED_VECTOR_SIZE = 1000;

	private static Lsh4Text single_instance = null;

	private static int minKgram = 3; // default Value set to three
	private static int maxKgram = 3; // default Value set to three
	private static final int LSH_SEED = 1234567890; // Seed chosen

	private Lsh4Text() {
	}

	public static Lsh4Text getInstance() {
		if (single_instance == null)
			single_instance = new Lsh4Text();

		return single_instance;
	}

	
	private static String removeStopChar(String text) {
		return text.replaceAll("[.,:*;!()]", "").replaceAll("s+"," ");
	}
	
	/**
	 * The a bucket size is automatically estimated if not provided in the
	 * getBuckets() function. This function estimates a bucket size.
	 * 
	 * @return returns the default bucket size based on the size of the forest()
	 */
	public static int defaultBucketSize() {
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
	 * Returns the Jaccard Similarity of the vectors
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
	public static void decodeForestFromBase64(String encoded) {
		byte[] decoded = Base64.getDecoder().decode(encoded);

		IntBuffer intBuf = ByteBuffer.wrap(decoded).order(ByteOrder.LITTLE_ENDIAN).asIntBuffer();
		int[] array = new int[intBuf.remaining()];
		intBuf.get(array);

		IntegratedIntCompressor iic = new IntegratedIntCompressor();
		int[] uncompressed = iic.uncompress(array); // equals to data

		forest = new Integer[uncompressed.length];
		for (int i = 0; i < uncompressed.length; i++) {
			forest[i] = uncompressed[i];
		}
	}

	/**
	 * compresses encodes the entire Forest as base64 for easy storage.
	 * 
	 * @return returns an base64 encoded value of forest
	 */
	public static String encodeForestAsBase64() {

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
	 * @param stages   determines the number of possible buckets
	 * @return returns list of possible buckets which may contain this text.
	 */
	public static int[] getBuckets(String document, int stages) {
		return getBuckets(document, stages, defaultBucketSize());
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
	 * @param stages     determines the number of possible buckets
	 * @param bucketSize The document for which the boolean vector is being created
	 * @return returns list of possible buckets which may contain this text.
	 */
	public static int[] getBuckets(String document, int stages, int bucketSize) {

		boolean vector[] = getVector(document);

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
	private static ArrayList<ForestShingle> getUntrimmedForest(boolean decending) {
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
	public static int untrimmedForestSize() {
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
	 * @return The frequency could to find in an untrimmed forest.
	 */
	public static int findCountofIndexInUntrimmedForest(int countNumber) {
		ArrayList<ForestShingle> forest = getUntrimmedForest(true);

		for (int i = 0; i < forest.size(); i++) {
			if (forest.get(i).getShingleCountInForest() <= countNumber) {
				return i;
			}
		}
		return forest.size();
	}

	/**
	 * This will print all the shingles from a forest.
	 */
	public static void printForest() {
		for (int i : forest) {
			System.out.println(i);
		}
	}

	/**
	 * This will print all the shingles and their count from the unTrimmed forest.
	 * This could be used to identify the vector size needed to build a forest.
	 */
	public static void printShingleAndCount(int head) {
		if(untrimmedForestMap.size()<head) throw new IllegalArgumentException();
		
		ArrayList<ForestShingle> forest = getUntrimmedForest(true);

		for(int i=0; i<head; i++) {
			System.out.println(forest.get(i).getId()+ " --> "+ forest.get(i).getShingleCountInForest());
		}
	}

	/**
	 * This will build a forest using all the shinglings. This should only be used
	 * if the size of the forest is small (less than 1000) or there is very little
	 * redundancy.
	 * 
	 * @return The forest is returned as an array of integers
	 */
	public static Integer[] getForest() {
		if (forest == null)
			throw new NullPointerException();
		return forest;
	}

	/**
	 * This will build a forest using all the shinglings. This should only be used
	 * if the size of the forest is small (less than 1000) or there is very little
	 * redunancy.
	 */
	public static void buildFullForest() {
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
	 * @return The vector for the string
	 */
	public static boolean[] getVector(String document) {
		if (forest == null)
			throw new NullPointerException();

		document = removeStopChar(document);

		ShinglingSet set = new ShinglingSet();
		set.addShingling(document, minKgram, maxKgram);

		boolean[] vector = new boolean[forest.length];

		for (int i = 0; i < forest.length; i++) {
			vector[i] = set.contains(forest[i]);
		}

		return vector;
	}

	/**
	 * The MinHash signature of the text created by using the forest vector
	 * 
	 * @param document The text of the document for which the signature is being
	 *                 created
	 * @return Gets the MinHash signature of the documents
	 */
	public static int[] getMinHashSignature(String document, double similartyError) {
		if (forest == null)
			throw new NullPointerException();

		document = removeStopChar(document);

		MinHash minhash = new MinHash(similartyError, forest.length, LSH_SEED);
		return minhash.signature(getVector(document));
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

	public static double signatureSimilarity(int[] sig1, int[] sig2, double similarityError) {
		MinHash minhash = new MinHash(similarityError, forest.length, LSH_SEED);
		return minhash.similarity(sig1, sig2);
	}

	private static int getDefaultVector() {
		if (untrimmedForestMap.size() < 800) {
			return untrimmedForestMap.size() - 1;
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
	public static void buildForest() {
		buildForest(getDefaultVector());
	}

	/**
	 * Builds a trimmed forest of vectorSize from using a untrimmed forest by
	 * removing all the leafs that had the lowest frequency of use
	 * 
	 * @param vectorSize The size of the vector used to build the forest
	 */
	public static void buildForest(int vectorSize) {

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
	public static void loadForest(Integer[] forestInt) {
		forest = forestInt;
		Arrays.sort(forest);
	}

	/**
	 * Adds the document to the forest by creating shingles of the documents and
	 * then adding it to the forest. If the machine does not have enough memory, you
	 * may see an out of memory error if your forest gets too big.
	 * 
	 * @param document The text of the document
	 */
	public static void addDocumentToUntrimmedForest(String document) {
		getInstance();
		document = removeStopChar(document);
		forest = null;
		Integer[] documentTokens = ShinglingSet.getTokensForMessage(document, minKgram, maxKgram);

		for (Integer tkn : documentTokens) {
			ForestShingle fs = untrimmedForestMap.get(tkn);
			if (fs == null)
				fs = new ForestShingle(tkn, 0);

			fs.increment();
			untrimmedForestMap.put(fs.getId(), fs);
		}
	}

	/**
	 * Sets the default k-grams used to build forest and find bucket
	 * 
	 * @param kGramsMinSize The minimum number of k-Grams used
	 * @param kGramsMaxSize The maximum number of k-Grams used
	 */
	public static void setKgrams(int kGramsMinSize, int kGramsMaxSize) {
		minKgram = kGramsMinSize;
		maxKgram = kGramsMaxSize;
	}

	/**
	 * Creates forest from a text file by tokenizing the text file. Each line is
	 * assumed to be its own document.
	 * 
	 * @param fileName  The name of the file which will be read to create the forest
	 * @param kGramsMin The minimum number of k-Grams used
	 * @param kGramsMax The maximum number of k-Grams used
	 * @return The size of the forest
	 */
	public static int loadFile(String fileName, String encoding, int kGramsMin, int kGramsMax) throws IOException {
		setKgrams(kGramsMin, kGramsMax);
		return loadFile(fileName, encoding);
	}

	/**
	 * Creates forest from a text file by tokenizing the text file. Each line is
	 * assumed to be its own document. It also assumes that the default min and max
	 * k-grams are three. Each line of the file must be it's own document. 
	 * 
	 * @param fileName The name of the file which will be read to create the forest
	 * @return The size of the forest
	 */
	public static int loadFile(String fileName, String encoding) throws IOException {
		getInstance();
		
		File tldlist = FileUtils.getFile(fileName);

		try {
			LineIterator it = FileUtils.lineIterator(tldlist, encoding);

			while (it.hasNext()) {
				String text = it.nextLine().replace("\"", "");
				addDocumentToUntrimmedForest(text);
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
				- (double) a[document1.length()][document2.length()] / Math.max(document1.length(), document2.length());
		return percentage_difference;
	}
	
	public static void close() {
		forest=null;
		untrimmedForestMap=null;
		System.gc();
	}
}
