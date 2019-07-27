package com.shikhir.lshTester;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

import com.shikhir.lsh.str.Lsh4Text;

public class LshTester {

	@Test
	public void testLevenshteinSimilarity() {
		String document1 = "abcd";
		String document2 = "bbcd";
		
		int similarity = (int) (Lsh4Text.levenshteinSimilarity(document1, document2) * 100);
		assertEquals(75, similarity); // document1 and document2 are 75% the same
	}
	
	@Test
	public void testForestCreation() {
		try {
			Lsh4Text.loadFile("test_data_movie_plots.txt", "UTF-8");
		} catch (IOException e) {
			fail("could not find test file");
		}
		System.out.println("Untrimmed Size =" + Lsh4Text.untrimmedForestSize());
		assertEquals(Lsh4Text.untrimmedForestSize(), 10320);
		Lsh4Text.buildForest();
		int buckets[] = Lsh4Text.getBuckets("This movie stinks. It's boring. I've never been so disgusted in my life.", 2);
		
		assertEquals(buckets.length, 2);
	}	
}
