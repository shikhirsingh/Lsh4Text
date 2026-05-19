package com.shikhir.lsh.trimmed;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class TForestAdditionalTest {

    @Test(expected = IllegalArgumentException.class)
    public void getVectorShouldRejectBlankDocument() {
        TForest forest = new TForest();
        forest.getVector("  ", true, 1, 1);
    }

    @Test
    public void defaultBucketSizeShouldUseThreeTimesSqrtOfSize() {
        TForest forest = new TForest();
        for (int i = 0; i < 16; i++) {
            forest.add(i, 0.5f);
        }
        forest.finalize();

        assertEquals(12, forest.defaultBucketSize());
    }

    @Test
    public void vectorOverloadsShouldMatchForSameConfiguration() {
        TForest forest = new TForest(true, false, false, false);
        forest.add(97, 1.0f); // token "a"
        forest.add(98, 1.0f); // token "b"
        forest.add(99, 1.0f); // token "c"
        forest.finalize();

        boolean[] a = forest.getVector("a b c", true, 1, 1);
        boolean[] b = forest.getVector("a b c", true, 1, 1, true, false, false);

        assertArrayEquals(a, b);
        assertTrue(a.length >= 3);
    }
}
