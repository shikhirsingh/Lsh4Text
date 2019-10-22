package com.shikhir.lshTester;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

import com.shikhir.lsh.str.Lsh4Text;
import com.shikhir.util.stringops.Stopwords;

public class LshTester {
	final private String[] spam_ham_messages_history = {
			"Congrats! 1 year special cinema pass for 2 is yours. call 09061209465 now! C Suprman V, Matrix3, StarWars3, etc all 4 FREE! bx420-ip4-5we. 150pm. Dont miss out!",
			"Your free ringtone is waiting to be collected. Simply text the password \\MIX\\\" to 85069 to verify. Get Usher and Britney. FML",
			"I'm still looking for a car to buy. And have not gone 4the driving test yet.	",
			"Aight, I'll hit you up when I get some cash	", "Your code is 2230",
			"Hey Chcy! Your EazyDiner referral invite is expiring on 15-11-18. Complete a booking above Rs. 500 to earn Rs. 400 in your Eazywallet. Book now! https://lkmdg.com/XB8Eom3322q40",
			"Tap to reset your Instagram password: https://ig.me/dafde Std data rates may apply",
			"Hi Murae, Your One Time Password(OTP) to signup for a DCM account is 79283",
			"Your Yada sign-in code is: 750615", "验证码35906618用于QQ8******7更换密保手机,泄露有风险.防盗能力提升百倍 aq.qq.com/t -QQ安全中心",
			"How would my ip address test that considering my computer isn't a minecraft server	" };

	final private String[] testMessages = { "askrfjk sksdkjgj skksjeeeje kgguerifkf",
			"Bob, code for Acme Store is: 2124", "Robin, code for Acme Store is: 5787",
			"Hey Maddy! Your EazyDiner referral invite is expiring on 15-10-18. Complete a booking above Rs. 500 to earn Rs. 400 in your Eazywallet. Book now! https://ngcrt.com/sYtUGcmBx2",
			"Hey Sinjy Deora! Your EazyDiner referral invite is expiring on 15-02-19. Complete a booking above Rs. 500 to earn Rs. 400 in your Eazywallet. Book now! https://feywy.com/gZOpSgx8OT",
			"Hello World", "验证码35906618用于QQ4******7更换密保手机,泄露能风险.防盗能力提升百倍 aq.qq.com/t -QQ安全中心",
			"验证码3234334用于QQ8******8更换密保手机,泄露有风险.防盗能力提升百倍 qq.com/yavadoo -QQ安全中心",
			"帐号（qfiies）     的密保手机 1989*****66解绑成，同时不能再用此手机号进行登录。如需重新绑定，请用电脑登录pa",
			"帐号（dfgdfg）的密保手机 1289*****66解绑能，同时不能再用此手机号进行登录。如需重新绑定，请用电脑登录pa",
			"帐号（邯郸私人伴游） 的密保手机 1972*****79解需用，同时不能再用此手机号进行登录。如需重新绑定，请用电脑登录pa" };

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
		final boolean removeStopChar = true;
		final boolean caseSensitive = false;
		
		System.out.println("Word tokens test");

		Lsh4Text lshText = new Lsh4Text(removeStopWords, removeStopChar, caseSensitive);

		try {
			lshText.loadFile("src/test/resources/test_data_movie_plots.txt", "UTF-8", wordTokens, KGRAM_MIN, KGRAM_MAX);
		} catch (IOException e) {
			fail("could not find test file");
		}
		int forestSize = lshText.untrimmedForestSize();
		assertEquals(forestSize, 2269);
		lshText.buildForest();
		
		int buckets[] = lshText.getBuckets("This movie stinks. It's boring. I've never been so disgusted in my life.", 
											true, 
											KGRAM_MIN, 
											KGRAM_MAX, 
											MAX_NUMBER_OF_BUCKETS);
		assertEquals(buckets.length, 2);
		
		boolean[] bVector = lshText.getVector("This movie stinks. It's boring. I've never been so disgusted in my life.", true, KGRAM_MIN, KGRAM_MAX);
		System.out.println("boolean vector size: "+bVector.length);		
		
		long sig1 = lshText.getSentenceSignature("This movie stinks. It's boring. I've never been so disgusted in my life.",
									true,
									KGRAM_MIN, 
									KGRAM_MAX);
	
		assertEquals(1576857129949774653L, sig1);

		
		long sig2 = lshText.getSentenceSignature("Paul is invited to Libbets' apartment in Manhattan, though upon arriving, is disappointed to learn that Francis was also invited.",
									true,
									KGRAM_MIN, 
									KGRAM_MAX);
		System.out.println(sig2);
		assertEquals(3537752637760471811L, sig2);
		
	}
	



	@Test
	public void characterTokensTest() {

		final int KGRAM_MIN = 5;
		final int KGRAM_MAX = 20;
		final int MAX_NUMBER_OF_BUCKETS = 2;
		final boolean wordTokens = false;
		final boolean removeStopWords = false;
		final boolean removeStopCharacters = false;

		Lsh4Text lshText = new Lsh4Text(removeStopWords, removeStopCharacters);

		System.out.println("Character tokens test");
		try {
			System.out.println("Loading Data file");
			int forestCount = lshText.loadFile("src/test/resources/phishing_website_dataset_test.txt", "UTF-8",
					wordTokens, KGRAM_MIN, KGRAM_MAX);
			System.out.println("File Loaded");
		} catch (IOException e) {
			fail("could not find test file");
		}
		System.out.println("Untrimmed Forest size:" + lshText.untrimmedForestSize());
		lshText.printTopShingleAndCount(10);
		System.out.println("Removing frequency less than 2");
		lshText.getUntrimmedForest().removeLessThanFrequency(15);
		System.out.println("New trimmed Forest size:" + lshText.untrimmedForestSize());

		lshText.getUntrimmedForest().cleanUntrimmedForest(40);
		System.out.println("New Cleaned Forest size:" + lshText.untrimmedForestSize());

		lshText.printTopShingleAndCount(20);
		int fs = lshText.untrimmedForestSize();
		assertEquals(fs, 58);
		lshText.buildForest();

		int buckets[] = lshText.getBuckets("login.secure.apple-com.gr", wordTokens, KGRAM_MIN, KGRAM_MAX,
				MAX_NUMBER_OF_BUCKETS);
		assert (buckets.length <= 2);
	}


	@Test
	public void stopwords() {
		String sentence = "Hello my name is Shikhir. This is a test to see if the stopwords function actually remove all the stopwords.";
		String removedStopWords = Stopwords.removeStopWords(sentence);

		assertEquals(removedStopWords, "Shikhir. test stopwords function remove stopwords.");
	}


}
