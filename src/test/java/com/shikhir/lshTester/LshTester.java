package com.shikhir.lshTester;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

import com.shikhir.lsh.str.Lsh4Text;
import com.shikhir.util.stringops.Stopwords;

public class LshTester {

	@Test
	public void testLevenshteinSimilarity() {
		String document1 = "abcd";
		String document2 = "bbcd";
		
		int similarity = (int) (Lsh4Text.levenshteinSimilarity(document1, document2) * 100);
		assertEquals(75, similarity); // document1 and document2 are 75% the same
	}
	
	
	@Test
	public void wordsTokensTest() {

		final int KGRAM_MIN = 1;
		final int KGRAM_MAX = 1;
		final int MAX_NUMBER_OF_BUCKETS = 2;
		final boolean wordTokens = true;
		final boolean removeStopWords = true;

		
		System.out.println("Word tokens test");

		Lsh4Text lshText = new Lsh4Text(removeStopWords, true);

		try {
			lshText.loadFile("src/test/resources/test_data_movie_plots.txt", "UTF-8", wordTokens, KGRAM_MIN, KGRAM_MAX);
		} catch (IOException e) {
			fail("could not find test file");
		}
		System.out.println("Untrimmed Forest size:"+lshText.untrimmedForestSize());
		lshText.printTopShingleAndCount(10);
		System.out.println("Number of tokens used more than once = "+lshText.findCountofIndexInUntrimmedForest(1));
		assertEquals(lshText.untrimmedForestSize(), 2272);
		lshText.buildForest();
		
		int buckets[] = lshText.getBuckets("This movie stinks. It's boring. I've never been so disgusted in my life.", 
											true, 
											KGRAM_MIN, 
											KGRAM_MAX, 
											MAX_NUMBER_OF_BUCKETS);
		assertEquals(buckets.length, 2);
	}	
	
	@Test
	public void charecterTokensTest() {

		final int KGRAM_MIN = 6;
		final int KGRAM_MAX = 20;
		final int MAX_NUMBER_OF_BUCKETS = 2;
		final boolean wordTokens = false;
		final boolean removeStopWords = false;
		final boolean removeStopCharacters = true;
		
		Lsh4Text lshText = new Lsh4Text(removeStopWords, removeStopCharacters);

		System.out.println("Character tokens test");
		try {
			System.out.println("Loading Data file");
			lshText.loadFile("src/test/resources/phishing_website_dataset_test.txt", "UTF-8", wordTokens, KGRAM_MIN, KGRAM_MAX);
			System.out.println("File Loaded");
		} catch (IOException e) {
			fail("could not find test file");
		}
		System.out.println("Untrimmed Forest size:"+lshText.untrimmedForestSize());
		System.out.println("Removing frequency less than 2");		
		lshText.removeLessThanFrequency(15);
		System.out.println("New trimmed Forest size:"+lshText.untrimmedForestSize());

		lshText.clearnUntrimmedForest(40);
		System.out.println("New Cleaned Forest size:"+lshText.untrimmedForestSize());
		
		lshText.printTopShingleAndCount(20);
		assertEquals(lshText.untrimmedForestSize(), 54);
		lshText.buildForest();
		
		int buckets[] = lshText.getBuckets("This movie stinks. It's boring. I've never been so disgusted in my life.", 
											true, 
											KGRAM_MIN, 
											KGRAM_MAX, 
											MAX_NUMBER_OF_BUCKETS);
		assert(buckets.length <= 2);
	}		
	
	@Test
	public void stopwords() {
		Lsh4Text lsh4Text = new Lsh4Text(true, false);
		
		String sentence = "Hello my name is Shikhir. This is a test to see if the stopwords function actually remove all the stopwords.";
		String removedStopWords = Stopwords.removeStopWords(sentence);
		
		assertEquals(removedStopWords, "Shikhir. test stopwords function remove stopwords.");
	}

	
}
