package com.shikhir.lsh.str;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.StringUtils;

import com.shikhir.lsh.trimmed.Sentence;
import com.shikhir.lsh.trimmed.TForest;
import com.shikhir.lsh.untrimmed.forest.UntrimmedForest;
import info.debatty.java.lsh.MinHash;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;


public class Lsh4Text {

	private UntrimmedForest untrimmedForest = new UntrimmedForest();
	
	private TForest trimmedForest=null;


	private boolean removeStopWords=false;
	private boolean removeStopCharacters=false;
	private boolean caseSensitive = false;
	private boolean normalize=false;
	private int cutoff=0;
	
	
	private static final Logger log = Logger.getLogger(Lsh4Text.class.getName());


	public Lsh4Text() {
	}

	/**
	 * Returns the Jaccard Similarity of two different vectors
	 * 
	 * @param removeStopWords Removes stop words while creating a vector or inputting into forest
 	 * @param removeStopCharacters Removes stop characters (like quotations, colons, etc) while creating a vector or inputting into forest
	 */
	public Lsh4Text(boolean removeStopWords, boolean removeStopCharacters) {
		this.removeStopWords = removeStopWords;
		this.removeStopCharacters = removeStopCharacters;
		
		untrimmedForest.setRemoveStopWords(removeStopWords);
		untrimmedForest.setRemoveStopCharacters(removeStopCharacters);
		untrimmedForest.setCaseSensitive(caseSensitive);

	}

	/**
	 * Returns the Jaccard Similarity of two different vectors
	 * 
	 * @param removeStopWords Removes stop words while creating a vector or inputting into forest
 	 * @param removeStopCharacters Removes stop characters (like quotations, colons, etc) while creating a vector or inputting into forest
 	 * @param caseSensitive should the forest be case sensitive
	 */
	public Lsh4Text(boolean removeStopWords, boolean removeStopCharacters, boolean caseSensitive) {
		this.removeStopWords = removeStopWords;
		this.removeStopCharacters = removeStopCharacters;
		this.caseSensitive = caseSensitive;
		
		untrimmedForest.setRemoveStopWords(removeStopWords);
		untrimmedForest.setRemoveStopCharacters(removeStopCharacters);
		untrimmedForest.setCaseSensitive(caseSensitive);
		
	}

	
	/**
	 * Returns the Jaccard Similarity of two different vectors
	 * 
	 * @param vector1 The document which needs to be analyzed
	 * @param vector2   determines the number of possible buckets
	 * @return Jaccard Similarity of the vectors.
	 */
	public static double jaccardSimilarity4Vectors(boolean[] vector1, boolean[] vector2) {
	    Objects.requireNonNull(vector1, "vector1 must not be null");
	    Objects.requireNonNull(vector2, "vector2 must not be null");

		return MinHash.jaccardIndex(vector1, vector2);
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
		return trimmedForest.getBuckets(document, wordTokens, minKGrams, maxKGrams, stages);
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
		return trimmedForest.getBuckets(document, wordTokens, minKGrams, maxKGrams, stages, bucketSize);
	}

	/**
	 * Returns untrimmed forest 
	 * 
	 * @return The full untrimmed forest.
	 */
	public UntrimmedForest getUntrimmedForest() {
		if (untrimmedForest == null) {
			throw new NullPointerException();
		}
		return untrimmedForest;
	}
	public TForest getTrimmedForest() {
		return trimmedForest;
	}

	/**
	 * Returns the number of unique shinglings in an untrimmed forest
	 * 
	 * @return count of unique shinglings in untrimmed forest.
	 */
	
	public int untrimmedForestSize() {
		return untrimmedForest.size();
	}

	/**
	 * Builds a trimmed forest of vectorSize from using a untrimmed forest by
	 * removing all the leafs that had the lowest frequency of use
	 * 
 	 * @return size of forest built
	 */

	public int buildForest() {
		if(cutoff>0) {
			int cutoffIndex = getUntrimmedForest().findCountofIndexInUntrimmedForest(cutoff);
			buildForest(cutoffIndex);
			return cutoffIndex;
		}
		else {
			int size= untrimmedForest.getDefaultVector();
			trimmedForest = untrimmedForest.buildForest();
			return size;
		}
	}

	
	/**
	 * Builds a trimmed forest of vectorSize from using a untrimmed forest by
	 * removing all the leafs that had the lowest frequency of use
	 * 
	 * @param vectorSize The size of the vector used to build the forest
	 */

	public void buildForest(int vectorSize) {
		trimmedForest = untrimmedForest.buildForest(vectorSize);
	}
	
	/**
	 * This will print all the shingles and their count from the unTrimmed forest in descending order of frequency count.
	 * This could be used to identify the vector size needed to build a forest.
	 * 
	 * @param head the count of top shingles to be returned
	 */
	public void printTopShingleAndCount(int head) {
		untrimmedForest.printTopShingleAndCount(head);
	}

	/**
	 * Add a document to an untrimmed forest
	 * 
	 * @param document The document which needs to be analyzed
	 * @param wordTokens if true, tokens of words are assumed, otherwise characters
 	 * @param minKGram minimum size of shingling
	 * @param maxKGram maximum size of shingling
	 */
	
	public void addDocument(String document, boolean wordTokens, int minKGram, int maxKGram) {
		untrimmedForest.addDocument(document, wordTokens, minKGram, maxKGram);
	}
	
	/**
	 * By default, the digits are normalized to increase the the chances of collision for signature.
	 * You can turn this off.
	 * 
	 * @param normalize The text of the document for which the boolean vector is
	 *                 being created
	 *                 
	 */
	public void setNormalize(boolean normalize) {
		this.normalize = normalize;
		untrimmedForest.setNormalize(normalize);
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
	 * @return The vector for the string
	 */
	public boolean[] getVector(String document, boolean wordTokens, int minKGrams, int maxKGrams) {
		return trimmedForest.getVector(document, wordTokens, minKGrams, maxKGrams);
	}


	/**
	 * Counts the number of shinglings in the document
	 * 
	 * @param document The text of the document for which the boolean vector is
	 *                 being created
	 * @param wordTokens if true, tokens of words are assumed, otherwise characters
 	 * @param minKGrams minimum size of shingling
	 * @param maxKGrams maximum size of shingling
	 * @return the count of number of shinglings
	 */

	public int countDocumentShinglingsInForest(String document, boolean wordTokens, int minKGrams, int maxKGrams) {
		return trimmedForest.countDocumentShinglingsInForest(document, wordTokens, minKGrams, maxKGrams);
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
		return trimmedForest.getMinHashSignature(document, wordTokens, minKGram, maxKGram, similartyError);
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
	    if(StringUtils.isBlank(fileName)) throw new IllegalArgumentException("fileName parameter cannot be empty or null");

		File tldlist = FileUtils.getFile(fileName);

		try {
			LineIterator it = FileUtils.lineIterator(tldlist, encoding);

			while (it.hasNext()) {
				String text = it.nextLine().replace("\"", "").trim();

				if(text.length()<3) continue;
				untrimmedForest.addDocument(text, wordTokens, kGramsMin, kGramsMax);
			}
			it.close();
		} finally {
		}

		return untrimmedForest.size();
	}
	
	
	/**
	 * Export the Trimmed forest to a file in JSON format
	 * 
	 * @param file The file to which the object will be exported 
	 */

	public void exportTrimmedForest(File file) {
        ObjectMapper mapper = new ObjectMapper();
        String json=null;
        try {
        	json = mapper.writeValueAsString(trimmedForest);
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(json);
            writer.close();

        }
        catch(JsonProcessingException e) {
        	System.err.println("ERROR: Could not create json needed for export");
        	e.printStackTrace();
        }
        catch(IOException e) {
        	e.printStackTrace();
        }
	}
	
	public void importTrimmedForest(InputStream inStream) throws IOException {
		InputStreamReader inR = null;
		BufferedReader buf = null;
		inR = new InputStreamReader(inStream);
		buf = new BufferedReader(inR);

		String line = buf.readLine(); 
		StringBuilder sb = new StringBuilder(); 
		while(line != null){ 
			sb.append(line).append("\n"); 
			line = buf.readLine(); 
		} 
		String json = sb.toString(); 
		
		trimmedForest = new ObjectMapper().readValue(json, TForest.class);
	}
	
	public void importTrimmedForest(File file) throws IOException {
		FileInputStream fis = null;
		fis = new FileInputStream(file);
		importTrimmedForest(fis);
	}
	
	public void importUntrimmedForest(InputStream inStream) throws IOException {
		InputStreamReader inR = null;
		BufferedReader buf = null;
		inR = new InputStreamReader(inStream);
		buf = new BufferedReader(inR);

		String line = buf.readLine(); 
		StringBuilder sb = new StringBuilder(); 
		while(line != null){ 
			sb.append(line).append("\n"); 
			line = buf.readLine(); 
		} 
		String json = sb.toString(); 
		
		untrimmedForest = new ObjectMapper().readValue(json, UntrimmedForest.class);
	}

	public void importUntrimmedForest(File file) throws IOException {
		FileInputStream fis = null;
		fis = new FileInputStream(file);
		importUntrimmedForest(fis);
	}

	
	public Sentence getSentence(String document, boolean wordTokens, int minKGrams, int maxKGrams) {
		return trimmedForest.getSentence(document, wordTokens, minKGrams, maxKGrams);		
	}
	
	public long getSentenceSignature(String document, boolean wordTokens, int minKGrams, int maxKGrams) {
		return trimmedForest.getSentence(document, wordTokens, minKGrams, maxKGrams).getSignature();
	}
	
	/**
	 * Export the Untrimmed forest to a file in JSON format
	 * 
	 * @param file The file to which the object will be exported 
	 */

	public void exportUntrimmedForest(File file) {
        ObjectMapper mapper = new ObjectMapper();
        String json=null;
        try {
        	json = mapper.writeValueAsString(untrimmedForest);
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(json);
            writer.close();

        }
        catch(JsonProcessingException e) {
        	System.err.println("ERROR: Could not create json needed for export");
        	e.printStackTrace();
        }
        catch(IOException e) {
        	e.printStackTrace();
        }
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
		MinHash minhash = new MinHash(similarityError, this.trimmedForest.size(), TForest.LSH_SEED);
		return minhash.similarity(sig1, sig2);
	}
	
	/**
	 * Give you the levenshtein similarity of two documents as a percentage. Levenshtein Similarty is a
	 * measure of similarity of two strings. 
	 * See wikipedia an explanation of Levenshtein Distance.
	 * 
	 * @param document1 The text of the first document to compare
	 * @param document2 The text of the second document to compare
	 * @return The distance as a percentage
	 */

	public static double levenshteinSimilarity(String document1, String document2) {
		return com.shikhir.StrWrangler4j.nlp.NlpOperations.levenshteinSimilarity(document1, document2);
	}

	/**
	 * Releases resources
	 */
	
	public void close() {
		this.trimmedForest=null;
		this.untrimmedForest=null;
		System.gc();
	}

	public boolean isCaseSensitive() {
		return caseSensitive;
	}

	private void setCaseSensitive(boolean caseSensitive) {
		this.caseSensitive = caseSensitive;
	}

	public int getCutoff() {
		return cutoff;
	}

	public void setCutoff(int cutoff) {
		this.cutoff = cutoff;
	}
}
