package com.shikhir.lsh.trimmed;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Base64;

import org.junit.Test;

public class SentenceTest {

    @Test
    public void getDictionaryLocationInBase64ShouldEncodeAllIntBytes() {
        ArrayList<Integer> dictionary = new ArrayList<Integer>();
        dictionary.add(1);
        dictionary.add(300);

        ArrayList<Integer> original = new ArrayList<Integer>(dictionary);
        Sentence sentence = new Sentence("doc", 0.5f, dictionary, original);

        ByteBuffer expected = ByteBuffer.allocate(8);
        expected.putInt(1);
        expected.putInt(300);

        assertEquals(Base64.getEncoder().encodeToString(expected.array()), sentence.getDictionaryLocationInBase64());
    }

    @Test
    public void collisionLikelyShouldApplyHeuristics() {
        ArrayList<Integer> dictionary = new ArrayList<Integer>();
        ArrayList<Integer> original = new ArrayList<Integer>();

        for (int i = 0; i < 5; i++) {
            dictionary.add(i);
            original.add(i);
        }
        Sentence sameLength = new Sentence("doc", 1.0f, dictionary, original);
        assertFalse(sameLength.collisionLikely());

        ArrayList<Integer> fewDictionary = new ArrayList<Integer>();
        for (int i = 0; i < 3; i++) {
            fewDictionary.add(i);
        }
        Sentence likelyCollision = new Sentence("doc", 0.9f, fewDictionary, original);
        assertTrue(likelyCollision.collisionLikely());
    }
}
