package com.shikhir.lsh.trimmed;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import com.shikhir.lsh.str.Lsh4Text;
import com.shikhir.util.stringops.StringOperations;

public class TForestTest {

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
	public void testTrimmedForestSerialization() throws IOException {

		final int KGRAM_MIN = 1;
		final int KGRAM_MAX = 1;
		final boolean removeStopChar = true;
		final boolean removeStopWords = false;
		final boolean wordTokens = true;

		Lsh4Text lshText = new Lsh4Text(removeStopWords, removeStopChar);
		for (String msg : testMessages) {
			if (StringOperations.countCJKCharecters(msg) > 0) { // this tests to see if this is a Chinese, Japanese,
																// Korean, or Vietnamese message
				lshText.addDocument(msg, false, 3, 3); // chinese japanese or vietnamese text are encoded by charecters
			} else {
				lshText.addDocument(msg, wordTokens, KGRAM_MIN, KGRAM_MAX);
			}
		}
		lshText.buildForest(lshText.getUntrimmedForest().findCountofIndexInUntrimmedForest(1));
		Sentence msg10Sentence1 = StringOperations.countCJKCharecters(testMessages[10]) > 0
				? lshText.getSentence(testMessages[10], false, 3, 3)
				: lshText.getSentence(testMessages[10], wordTokens, KGRAM_MIN, KGRAM_MAX);
		long msg10Base64One = msg10Sentence1.getSignature();

		
		File utforestFile = File.createTempFile("trimmedForestTest", ".txt");
		lshText.exportTrimmedForest(utforestFile);

		Lsh4Text lshText2 = new Lsh4Text(removeStopWords, removeStopChar);
		lshText2.importTrimmedForest(utforestFile);
		System.out.println(utforestFile.getAbsolutePath());
		int lsh2Size = lshText2.getTrimmedForest().size();
		Sentence msg10Sentence2 = StringOperations.countCJKCharecters(testMessages[10]) > 0
				? lshText2.getSentence(testMessages[10], false, 3, 3)
				: lshText2.getSentence(testMessages[10], wordTokens, KGRAM_MIN, KGRAM_MAX);
		long msg10Base64Two = msg10Sentence2.getSignature();

		utforestFile.delete();
		assertEquals(msg10Base64Two, msg10Base64One);
	}

	@Test
	public void testSentenceForCollisions() {

		final int KGRAM_MIN = 1;
		final int KGRAM_MAX = 1;
		final boolean removeStopChar = true;
		final boolean removeStopWords = false;
		final boolean wordTokens = true;

		Lsh4Text lshText = new Lsh4Text(removeStopWords, removeStopChar);
		lshText.setNormalize(true);
		for (String msg : testMessages) {
			if (StringOperations.countCJKCharecters(msg) > 0) { // this tests to see if this is a Chinese, Japanese,
																// Korean, or Vietnamese message
				lshText.addDocument(msg, false, 3, 3); // chinese japanese or vietnamese text are encoded by charecters
			} else {
				lshText.addDocument(msg, wordTokens, KGRAM_MIN, KGRAM_MAX);
			}
		}
		System.out.println("Size of Untrimmed Forest = " + lshText.getUntrimmedForest().size());
		System.out.println("Cutoff=" + lshText.getUntrimmedForest().findCountofIndexInUntrimmedForest(1));
		lshText.buildForest(lshText.getUntrimmedForest().findCountofIndexInUntrimmedForest(1));

		Sentence msg1Sentence = StringOperations.countCJKCharecters(testMessages[1]) > 0
				? lshText.getSentence(testMessages[1], false, KGRAM_MIN, KGRAM_MAX)
				: lshText.getSentence(testMessages[1], wordTokens, KGRAM_MIN, KGRAM_MAX);

		System.out.printf("Probablity of collision = %.6f", msg1Sentence.getCollisionProbability() * 100);
		System.out.println("%");

		long msg1Sig = msg1Sentence.getSignature();

		long msg2Sig = StringOperations.countCJKCharecters(testMessages[2]) > 0
				? lshText.getSentenceSignature(testMessages[2], false, KGRAM_MIN, KGRAM_MAX)
				: lshText.getSentenceSignature(testMessages[2], wordTokens, KGRAM_MIN, KGRAM_MAX);

		assertEquals(msg1Sig, msg2Sig); // are test messages at index 1 and index 2 producing the same signature?

		long msg3Sig = StringOperations.countCJKCharecters(testMessages[3]) > 0
				? lshText.getSentenceSignature(testMessages[3], false, KGRAM_MIN, KGRAM_MAX)
				: lshText.getSentenceSignature(testMessages[3], wordTokens, KGRAM_MIN, KGRAM_MAX);

		Sentence msg3Sentence = StringOperations.countCJKCharecters(testMessages[3]) > 0
				? lshText.getSentence(testMessages[3], false, KGRAM_MIN, KGRAM_MAX)
				: lshText.getSentence(testMessages[3], wordTokens, KGRAM_MIN, KGRAM_MAX);

		assertNotEquals(msg2Sig, msg3Sig);

		System.out.println("Sentence 4: dictionaryWordCount=" + msg3Sentence.getDictionaryLocation().size());
		System.out.println("Sentence 4: totalWordCount=" + msg3Sentence.getOriginalSentence().size());

		Sentence msg4Sentence = StringOperations.countCJKCharecters(testMessages[4]) > 0
				? lshText.getSentence(testMessages[4], false, KGRAM_MIN, KGRAM_MAX)
				: lshText.getSentence(testMessages[4], wordTokens, KGRAM_MIN, KGRAM_MAX);

		assertEquals(msg3Sentence.getDictionaryLocationInBase64(), msg4Sentence.getDictionaryLocationInBase64()); 
		assertNotEquals(msg1Sentence, msg4Sentence);

		Sentence msg6Sentence = StringOperations.countCJKCharecters(testMessages[3]) > 0
				? lshText.getSentence(testMessages[6], false, KGRAM_MIN, KGRAM_MAX)
				: lshText.getSentence(testMessages[3], wordTokens, KGRAM_MIN, KGRAM_MAX);
		Sentence msg7Sentence = StringOperations.countCJKCharecters(testMessages[3]) > 0
				? lshText.getSentence(testMessages[7], false, KGRAM_MIN, KGRAM_MAX)
				: lshText.getSentence(testMessages[3], wordTokens, KGRAM_MIN, KGRAM_MAX);

		assertEquals(msg6Sentence.getDictionaryLocationInBase64(), msg7Sentence.getDictionaryLocationInBase64()); 
		assertNotEquals(msg1Sentence, msg6Sentence);

		Sentence msg8Sentence = StringOperations.countCJKCharecters(testMessages[8]) > 0
				? lshText.getSentence(testMessages[8], false, 3, 3)
				: lshText.getSentence(testMessages[8], wordTokens, KGRAM_MIN, KGRAM_MAX);
		String msg8Base64 = msg8Sentence.getDictionaryLocationInBase64();

		Sentence msg9Sentence = StringOperations.countCJKCharecters(testMessages[9]) > 0
				? lshText.getSentence(testMessages[9], false, 3, 3)
				: lshText.getSentence(testMessages[9], wordTokens, KGRAM_MIN, KGRAM_MAX);
		long msg9Base64 = msg9Sentence.getSignature();

		Sentence msg10Sentence = StringOperations.countCJKCharecters(testMessages[10]) > 0
				? lshText.getSentence(testMessages[10], false, 3, 3)
				: lshText.getSentence(testMessages[10], wordTokens, KGRAM_MIN, KGRAM_MAX);
		long msg10Base64 = msg10Sentence.getSignature();


		assertEquals(msg8Sentence.getDictionaryLocationInBase64(), msg9Sentence.getDictionaryLocationInBase64()); 
		assertEquals(msg9Sentence.getDictionaryLocationInBase64(), msg10Sentence.getDictionaryLocationInBase64()); 	
	}	
}
