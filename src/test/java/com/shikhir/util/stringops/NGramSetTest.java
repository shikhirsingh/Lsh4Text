package com.shikhir.util.stringops;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import opennlp.tools.dictionary.Dictionary;
import opennlp.tools.util.StringList;

public class NGramSetTest {

    @Test(expected = IllegalArgumentException.class)
    public void addTokenNGramsShouldRejectInvalidMinLength() {
        NGramSet set = new NGramSet();
        set.add(new StringList("a", "b"), 0, 2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void addTokenNGramsShouldRejectMinGreaterThanMax() {
        NGramSet set = new NGramSet();
        set.add(new StringList("a", "b"), 3, 2);
    }

    @Test
    public void addTokenNGramsShouldCreateExpectedUniqueNGrams() {
        NGramSet set = new NGramSet();
        set.add(new StringList("a", "b", "c"), 1, 2);

        assertEquals(5, set.size());
        assertTrue(set.contains(new StringList("a")));
        assertTrue(set.contains(new StringList("b")));
        assertTrue(set.contains(new StringList("c")));
        assertTrue(set.contains(new StringList("a", "b")));
        assertTrue(set.contains(new StringList("b", "c")));
    }

    @Test
    public void addCharacterNGramsShouldLowercaseOutput() {
        NGramSet set = new NGramSet();
        set.add("AbC", 1, 2);

        assertTrue(set.contains(new StringList("a")));
        assertTrue(set.contains(new StringList("b")));
        assertTrue(set.contains(new StringList("c")));
        assertTrue(set.contains(new StringList("ab")));
        assertTrue(set.contains(new StringList("bc")));
    }

    @Test
    public void toDictionaryShouldRespectCaseSensitivity() {
        NGramSet set = new NGramSet();
        set.add(new StringList("Token"));

        Dictionary insensitive = set.toDictionary(false);
        Dictionary sensitive = set.toDictionary(true);

        assertTrue(insensitive.contains(new StringList("token")));
        assertTrue(sensitive.contains(new StringList("Token")));
    }
}
