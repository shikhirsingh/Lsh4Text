package com.shikhir.lsh.untrimmed.forest;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Logger;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.StringUtils;

import com.shikhir.lsh.trimmed.TForest;
import com.shikhir.lsh.untrimmed.forest.shingling.ForestShingle;
import com.shikhir.lsh.untrimmed.forest.shingling.Shingle;
import com.shikhir.lsh.untrimmed.forest.shingling.ShinglingSet;
import com.shikhir.util.stringops.Stopwords;
import com.shikhir.util.stringops.StringOperations;
import com.shikhir.util.stringops.normalize.Normalize;

public class UntrimmedForest {

	private TreeMap<Integer, ForestShingle> untrimmedForestMap = new TreeMap<Integer, ForestShingle>();

	private int documentInsertionCount=0;
	private boolean removeStopWords=false;
	private boolean removeStopCharacters=false;
	private boolean caseSensitive=false;
	private boolean normalize=false;

	
	private static final Logger log = Logger.getLogger(UntrimmedForest.class.getName());
	private static final int RECOMMENDED_VECTOR_SIZE = 1000;
	
	
	public UntrimmedForest() {
		
	}
	
	public int size() {
		return untrimmedForestMap.size();
	}

	public TreeMap<Integer, ForestShingle> getUntrimmedForestMap() {
		return untrimmedForestMap;
	}

	public void setUntrimmedForestMap(TreeMap<Integer, ForestShingle> untrimmedForestMap) {
		this.untrimmedForestMap = untrimmedForestMap;
	}

	public boolean isRemoveStopWords() {
		return removeStopWords;
	}

	public void setRemoveStopWords(boolean removeStopWords) {
		this.removeStopWords = removeStopWords;
	}

	public boolean isRemoveStopCharacters() {
		return removeStopCharacters;
	}

	public void setRemoveStopCharacters(boolean removeStopCharacters) {
		this.removeStopCharacters = removeStopCharacters;
	}


	public int getDocumentSize() {
		return documentInsertionCount;
	}

	public void addDocument(String document, boolean wordTokens, int minKGram, int maxKGram) {
	    Objects.requireNonNull(document, "document parameter must not be null");
	    if(StringUtils.isBlank(document)) throw new IllegalArgumentException("document parameter cannot be empty");
	    
		document = this.removeStopCharacters?StringOperations.removeStopChar(document):document;
		document = normalize?Normalize.all(document):document;
		document = isCaseSensitive()?document:document.toLowerCase();
		if(this.removeStopWords) {
			document=Stopwords.removeStopWords(document);
		};
	    if(document.trim().length()==0) {
	    	return;
	    }

		Shingle[] documentShingles = ShinglingSet.getTokensForMessage(document, wordTokens, minKGram, maxKGram);
		if(documentShingles==null || documentShingles.length==0 ) return;
		documentInsertionCount++;
		for (Shingle s : documentShingles) {
			ForestShingle fs = untrimmedForestMap.get(s.getId());
			if (fs == null) fs = new ForestShingle(s.getToken(), 0);
			fs.increment();
			untrimmedForestMap.put(fs.getId(), fs);
		}

	}
	
	
	/**
	 * By default, the digits are normalized to increase the the chances of collision for signature.
	 * You can turn this off.
	 * 
	 * @param normalize The text of the document for which the boolean vector is being created
	 */                 
	public void setNormalize(boolean normalize) {
		this.normalize = normalize;
	}
	
	/**
	 * This will print all the shingles and their count from the unTrimmed forest in descending order of frequency count.
	 * This could be used to identify the vector size needed to build a forest.
	 * 
	 * @param head the count of top shingles to be returned
	 */
	public void printTopShingleAndCount(int head) {
		int size=untrimmedForestMap.size();
		if(size<head) throw new IllegalArgumentException();
		
		ArrayList<ForestShingle> forest = getUntrimmedForest(true);

		for(int i=0; i<head; i++) {
			System.out.println(forest.get(i).getToken()+ " → "+ forest.get(i).getShingleCountInForest());
		}
	}
	
	/**
	 * Returns untrimmed forest as an ArrayList
	 * 
	 * @return Returns the full untrimmed forest.
	 */
	public ArrayList<ForestShingle> getUntrimmedForest(boolean decending) {
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
	 * An untrimmed forest is sorted by frequency of shinglings found in all
	 * documents. This method will find the index in the array where the frequency
	 * count is less than or equal to the value of the parameter. This should be
	 * used to determine the size of the vector.
	 * 
 	 * @param countNumber The frequency count of the token
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
	 * Removes duplicates from untrimmed forest. This function is useful when the encoding is by characters instead of words
	 * 
	 * @param percentage The percentage of frequency count a token must be in the range of in order to remove
	 */

	public void cleanUntrimmedForest(int percentage) {
		
	    ForestShingle[] values = untrimmedForestMap.values().toArray(new ForestShingle[untrimmedForestMap.size()]);
	    
	    Arrays.sort(values, Collections.reverseOrder());

	    for(int i=0; i< values.length; i++) {
	    	int iCount = values[i].getShingleCountInForest();
	    	String iToken = values[i].getToken().replace("[","").replace("]", "");

    		if(i%1000==0) {
    			float outputPercent = (float) (100.0*i/values.length);
    			String formattedString = String.format("%.02f", outputPercent);
    			if(values.length>10000) {
    				log.info(formattedString+"% done ");
    			}
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
	 * An untrimmed forest is sorted by frequency of shinglings found in all
	 * documents and all shinglings less than or equal to the count number are removed
	 * 
 	 * @param countNumber The frequency count of the token
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
	 * Gets the default vector size if none is provided
	 * 
	 * @return The default vector size 
	 */
	
	public int getDefaultVector() {
		if (this.untrimmedForestMap.size() < 800) {
			return this.untrimmedForestMap.size() - 1;
		}
		int duplicateIndex = findCountofIndexInUntrimmedForest(1);
		if (duplicateIndex < 1200)
			return duplicateIndex;
		return RECOMMENDED_VECTOR_SIZE;

	}


	
	/**
	 * Builds a trimmed forest of vectorSize from using a untrimmed forest by
	 * removing all the leafs that had the lowest frequency of use
	 * 
	 * @param vectorSize The size of the vector used to build the forest
	 */
	
	public TForest buildForest(int vectorSize) {

		if (vectorSize > untrimmedForestMap.size()) {
			throw new IllegalArgumentException();
		}

		TForest trimmedForest = new TForest(removeStopCharacters, normalize, removeStopWords, caseSensitive);
		trimmedForest.setRemoveStopCharacters(removeStopCharacters);
		trimmedForest.setNormalize(normalize);
		trimmedForest.setRemoveStopWords(removeStopWords);
		trimmedForest.setCaseSensitive(caseSensitive);
		
		ArrayList<ForestShingle> unTrimmedForestAL = getUntrimmedForest(true);
		
		for (int j = 0; j < vectorSize; j++) {
			int id = unTrimmedForestAL.get(j).getId();
			int shingleCount = unTrimmedForestAL.get(j).getShingleCountInForest();
			String token = unTrimmedForestAL.get(j).getToken();
			float percentage = (float) (1.0*shingleCount/documentInsertionCount);
			trimmedForest.add(id, percentage);
		}
		trimmedForest.finalize();
		untrimmedForestMap = null; // releasing to free up memory
		
		System.gc();
		
		return trimmedForest;
	}
	
	
	
	/**
	 * Builds a trimmed forest from using a untrimmed forest by removing all the
	 * leafs that had the lowest frequency of use. The a default vector size of less
	 * than 1200 is used.
	 * 
	 */
	
	public TForest buildForest() {
		return buildForest(getDefaultVector());
	}

	/**
	 * This will build a forest using all the shinglings. This should only be used
	 * if the size of the forest is small (less than 1000) or there is very little
	 * redunancy.
	 */
	
	public TForest buildFullForest() {
		return buildForest(untrimmedForestMap.size());
	}


	public boolean isCaseSensitive() {
		return caseSensitive;
	}
	
	public void setCaseSensitive(boolean caseSensitive) {
		this.caseSensitive=caseSensitive;
	}
	
}
