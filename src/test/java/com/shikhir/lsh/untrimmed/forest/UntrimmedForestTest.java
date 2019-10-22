package com.shikhir.lsh.untrimmed.forest;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;

import com.shikhir.lsh.str.Lsh4Text;
import com.shikhir.lsh.trimmed.TForest;

import org.junit.Before;

public class UntrimmedForestTest {
	UntrimmedForest untrimmedForest = new UntrimmedForest();

	private String[] demolitionManQuotes = {
		"You are fined one credit for a violation of the Verbal Morality Statute",
		"Your repeated violation of the Verbal Morality Statute has caused me to notify the San Angeles Police Department. Please remain where you are for your reprimand",
		"John Spartan you are fined one credit for a violation of the verbal morality code!",
		"Lenina Huxley, you are fined one half credit for a sotto voce violation of the verbal morality statute",
		"Enhance your calm",
		"I've had it with enhance your calm"
	};
	
	@Before
	public void testAddDocument() {
		untrimmedForest.setCaseSensitive(false);
		for(String quote: demolitionManQuotes) {
			untrimmedForest.addDocument(quote, true, 1, 1);
		}
		
		assertEquals(demolitionManQuotes.length, untrimmedForest.getDocumentSize());
		assertTrue(untrimmedForest.size()>0);
	}


	@Test
	public void testBuild() {
		untrimmedForest.printTopShingleAndCount(untrimmedForest.size());
		TForest tforest = untrimmedForest.buildForest(9);
		assertEquals(9, tforest.size());
	}
	
	@Test
	public void testUntrimmedForestSerialization() throws IOException {
		final boolean removeStopWords = false;
		final boolean removeStopChar = false;
		final boolean caseSensitive = false;

		Lsh4Text lshText = new Lsh4Text(removeStopWords, removeStopChar, caseSensitive);
		String[] demolitionManQuotes = { 
				"You are fined one credit for a violation of the Verbal Morality Statute",
				"Your repeated violation of the Verbal Morality Statute has caused me to notify the San Angeles Police Department. Please remain where you are for your reprimand",
				"John Spartan you are fined one credit for a violation of the verbal morality code!",
				"Lenina Huxley, you are fined one half credit for a sotto voce violation of the verbal morality statute",
				"Enhance your calm", 
				"I’m gonna find that psycho Phoenix and enhance his calm" };
		
		for (String quote : demolitionManQuotes) {
			lshText.addDocument(quote, true, 1, 1);
		}
		
		File tmpFile = File.createTempFile("temp", ".txt");
		lshText.exportUntrimmedForest(tmpFile);
		
		Lsh4Text lshTextImport =  new Lsh4Text(removeStopWords, removeStopChar, caseSensitive);
		lshTextImport.importUntrimmedForest(tmpFile);

		assertEquals(lshText.untrimmedForestSize(), lshTextImport.untrimmedForestSize());
		
		tmpFile.delete();
	}
	
}
