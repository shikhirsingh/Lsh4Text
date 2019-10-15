package com.shikhir.lshTester;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import com.shikhir.lsh.str.Lsh4Text;
import com.shikhir.util.stringops.Stopwords;
import com.shikhir.util.stringops.StringOperations;

public class LshTester {
	final private String[] spam_ham_messages_history= {"Congrats! 1 year special cinema pass for 2 is yours. call 09061209465 now! C Suprman V, Matrix3, StarWars3, etc all 4 FREE! bx420-ip4-5we. 150pm. Dont miss out!",
			  "Your free ringtone is waiting to be collected. Simply text the password \\MIX\\\" to 85069 to verify. Get Usher and Britney. FML",
			  "I'm still looking for a car to buy. And have not gone 4the driving test yet.	",
			  "Aight, I'll hit you up when I get some cash	",
			  "Your code is 2230",
			  "Hey Chcy! Your EazyDiner referral invite is expiring on 15-11-18. Complete a booking above Rs. 500 to earn Rs. 400 in your Eazywallet. Book now! https://lkmdg.com/XB8Eom3322q40",
			  "Tap to reset your Instagram password: https://ig.me/dafde Std data rates may apply",
			  "Hi Murae, Your One Time Password(OTP) to signup for a DCM account is 79283",
			  "Your Yada sign-in code is: 750615",
			  "验证码35906618用于QQ8******7更换密保手机,泄露有风险.防盗能力提升百倍 aq.qq.com/t -QQ安全中心",
			  "How would my ip address test that considering my computer isn't a minecraft server	"};
	final private String [] testMessages = {
			"askrfjk sksdkjgj skksjeeeje kgguerifkf",
			"Bob, code for Acme Store is: 2124",
			"Robin, code for Acme Store is: 5787",
			"Hey Maddy! Your EazyDiner referral invite is expiring on 15-10-18. Complete a booking above Rs. 500 to earn Rs. 400 in your Eazywallet. Book now! https://ngcrt.com/sYtUGcmBx2",
			"Hey Sinjy Deora! Your EazyDiner referral invite is expiring on 15-02-19. Complete a booking above Rs. 500 to earn Rs. 400 in your Eazywallet. Book now! https://feywy.com/gZOpSgx8OT",
			"Hello World",
			"验证码3234334用于QQ8******7更换密保手机,泄露有风险.防盗能力提升百倍 qq.com/yavadoo -QQ安全中心"
	};

	@Test
	public void testLevenshteinSimilarity() {
		String document1 = "abcd";
		String document2 = "bbcd";
		
		int similarity = (int) (Lsh4Text.levenshteinSimilarity(document1, document2) * 100);
		assertEquals(75, similarity); // document1 and document2 are 75% the same
	}
	
	@Test
	public void testMessage() {
		
		
		assertEquals(true, true); 
	}
	
	@Test
	public void testForestSerialization() {
		final int KGRAM_MIN = 1;
		final int KGRAM_MAX = 1;
		final boolean wordTokens = true;
		final boolean removeStopWords = true;
		final boolean removeStopChar = true;


		Lsh4Text lshText1 = new Lsh4Text(removeStopWords, removeStopChar);

		try {
			lshText1.loadFile("src/test/resources/test_data_movie_plots.txt", "UTF-8", wordTokens, KGRAM_MIN, KGRAM_MAX);
		} catch (IOException e) {
			fail("could not find test file");
		}
		try {
			File tmpFile = File.createTempFile("UntrimmedForest", ".tsv");
			lshText1.exportUntrimmedForest(tmpFile);
			System.out.println("Exporting Untrimmed Forest to Temp File: "+tmpFile.getAbsoluteFile());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		lshText1.buildForest();
		
		String base64Test = lshText1.encodeForestAsBase64();
		
		Lsh4Text lshText2 = new Lsh4Text(removeStopWords, removeStopChar);
		lshText2.decodeForestFromBase64(base64Test);
		
		Integer[] forest1 = lshText1.getForest();
		Integer[] forest2 = lshText2.getForest();
		
		for(int i=0; i<forest1.length; i++) {
				assertEquals(forest1[i], forest2[i]);
		}		
	}
	
	@Test
	public void testBase64Vector() {
						
		final int KGRAM_MIN = 1;
		final int KGRAM_MAX = 1;
		final boolean removeStopChar = true;
		final boolean removeStopWords = false;
		final boolean wordTokens = true;

		Lsh4Text lshText = new Lsh4Text(removeStopWords, removeStopChar);
		for(String msg: spam_ham_messages_history){	
			if(StringOperations.countCJKCharecters(msg)>0) { // this tests to see if this is a Chinese, Japanese, Korean, or Vietnamese message
				lshText.addDocumentToUntrimmedForest(msg, false, KGRAM_MIN, KGRAM_MAX); // chinese japanese or vietnamese text are encoded by charecters
			}
			else {
				lshText.addDocumentToUntrimmedForest(msg, wordTokens, KGRAM_MIN, KGRAM_MAX);
			}
		}
		lshText.buildForest();
		

		
		String base64ValueMsg1 = StringOperations.countCJKCharecters(testMessages[1])>0 ? lshText.getVectorAsBase64(testMessages[1], false, KGRAM_MIN, KGRAM_MAX)
																						:lshText.getVectorAsBase64(testMessages[1], wordTokens, KGRAM_MIN, KGRAM_MAX);

		String base64ValueMsg2 = StringOperations.countCJKCharecters(testMessages[2])>0 ? lshText.getVectorAsBase64(testMessages[2], false, KGRAM_MIN, KGRAM_MAX)
				:lshText.getVectorAsBase64(testMessages[2], wordTokens, KGRAM_MIN, KGRAM_MAX);

		assertEquals(base64ValueMsg1, base64ValueMsg2); // are test messages at index 1 and index 2 producing the same signature?
		
		String base64ValueMsg3 = StringOperations.countCJKCharecters(testMessages[3])>0 ? lshText.getVectorAsBase64(testMessages[3], false, KGRAM_MIN, KGRAM_MAX)
				:lshText.getVectorAsBase64(testMessages[3], wordTokens, KGRAM_MIN, KGRAM_MAX);

		assertNotEquals(base64ValueMsg1, base64ValueMsg3);
		
		String base64ValueMsg4 = StringOperations.countCJKCharecters(testMessages[4])>0 ? lshText.getVectorAsBase64(testMessages[4], false, KGRAM_MIN, KGRAM_MAX)
				:lshText.getVectorAsBase64(testMessages[4], wordTokens, KGRAM_MIN, KGRAM_MAX);

		assertEquals(base64ValueMsg3, base64ValueMsg4); // are test messages at index 3 and index 4 producing the same signature?
		
	}
	
	@Test
	public void wordsTokensTest() {

		final int KGRAM_MIN = 1;
		final int KGRAM_MAX = 1;
		final int MAX_NUMBER_OF_BUCKETS = 2;
		final boolean wordTokens = true;
		final boolean removeStopWords = true;
		final boolean removeStopChar = true;


		
		System.out.println("Word tokens test");

		Lsh4Text lshText = new Lsh4Text(removeStopWords, removeStopChar);

		try {
			lshText.loadFile("src/test/resources/test_data_movie_plots.txt", "UTF-8", wordTokens, KGRAM_MIN, KGRAM_MAX);
		} catch (IOException e) {
			fail("could not find test file");
		}
		System.out.println("Untrimmed Forest size:"+lshText.untrimmedForestSize());
		lshText.printTopShingleAndCount(10);
		System.out.println("Number of tokens used more than once = "+lshText.findCountofIndexInUntrimmedForest(1));
		int fs = lshText.untrimmedForestSize();
		assertEquals(lshText.untrimmedForestSize(), 2270);
		lshText.buildForest();
		
		int buckets[] = lshText.getBuckets("This movie stinks. It's boring. I've never been so disgusted in my life.", 
											true, 
											KGRAM_MIN, 
											KGRAM_MAX, 
											MAX_NUMBER_OF_BUCKETS);
		assertEquals(buckets.length, 2);
		
		boolean[] bVector = lshText.getVector("This movie stinks. It's boring. I've never been so disgusted in my life.", true, KGRAM_MIN, KGRAM_MAX);
		System.out.println("boolean vector size: "+bVector.length);		
		
		String vector64a = lshText.getVectorAsBase64("This movie stinks. It's boring. I've never been so disgusted in my life.",
									true,
									KGRAM_MIN, 
									KGRAM_MAX);

		assertEquals("eAA=", vector64a);

		
		String vector64b = lshText.getVectorAsBase64("Paul is invited to Libbets' apartment in Manhattan, though upon arriving, is disappointed to learn that Francis was also invited.",
									true,
									KGRAM_MIN, 
									KGRAM_MAX);
		
		
		assertEquals("bQBwAL8AzQEyApsC", vector64b);

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
		int fs = lshText.untrimmedForestSize();
		assertEquals(fs, 64);
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
		String sentence = "Hello my name is Shikhir. This is a test to see if the stopwords function actually remove all the stopwords.";
		String removedStopWords = Stopwords.removeStopWords(sentence);
		
		assertEquals(removedStopWords, "Shikhir. test stopwords function remove stopwords.");
	}

	
}
