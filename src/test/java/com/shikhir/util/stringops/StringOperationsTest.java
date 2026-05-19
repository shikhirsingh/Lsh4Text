package com.shikhir.util.stringops;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class StringOperationsTest {

    @Test
    public void removeStopCharShouldHandleBlankInput() {
        assertEquals("", StringOperations.removeStopChar(null));
        assertEquals("", StringOperations.removeStopChar("   "));
    }

    @Test
    public void removeStopCharShouldRemovePunctuationAndNormalizeWhitespace() {
        String input = " Hello,  world!  (test)-case?  ";
        assertEquals("Hello world testcase", StringOperations.removeStopChar(input));
    }

    @Test
    public void binarySearchShouldFindOrReturnMinusOne() {
        String[] sorted = {"alpha", "beta", "delta", "omega"};

        assertEquals(0, StringOperations.binarySearch(sorted, "alpha"));
        assertEquals(2, StringOperations.binarySearch(sorted, "delta"));
        assertEquals(-1, StringOperations.binarySearch(sorted, "gamma"));
    }

    @Test
    public void countCjkCharactersShouldCountOnlyIdeographicCodePoints() {
        String input = "A你B好C";
        assertEquals(2, StringOperations.countCJKCharecters(input));
    }
}
