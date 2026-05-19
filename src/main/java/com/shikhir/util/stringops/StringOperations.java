package com.shikhir.util.stringops;

import org.apache.commons.lang3.StringUtils;

public class StringOperations {

	/**
	 * Performs binary search on a sorted string array.
	 *
	 * @param preSortedArray pre-sorted array of strings to search
	 * @param find value to find
	 * @return index of the matching value, or -1 if not found
	 * @since 1.0.0
	 */

	public static int binarySearch(String[] preSortedArray, String find) 
    { 
        int l = 0, r = preSortedArray.length - 1; 
        while (l <= r) { 
            int m = l + (r - l) / 2; 
  
            int res = find.compareTo(preSortedArray[m]); 
  
            // Check if x is present at mid 
            if (res == 0) 
                return m; 
  
            // If x greater, ignore left half 
            if (res > 0) 
                l = m + 1; 
  
            // If x is smaller, ignore right half 
            else
                r = m - 1; 
        } 
  
        return -1; 
    } 
	

	/**
	 * Removes configured stop characters from a string.
	 *
	 * @param text input text
	 * @return cleaned text with stop characters removed
	 * @since 1.0.0
	 */

	public static String removeStopChar(String text) {
	    if(StringUtils.isBlank(text)) return "";

		return text.replaceAll("[.?=_,【】%:*;|�!()'-]", "").replaceAll("\\s+"," ").trim();
	}

	/**
	 * Counts CJK ideographic characters in the input string.
	 *
	 * This helps detect text that may require character-level tokenization.
	 *
	 * @param strTest string to inspect
	 * @return number of CJK ideographic characters
	 * @since 1.0.0
	 */
	public static int countCJKCharecters(String strTest) {
		final int length = strTest.length();
		int counter=0;
		for (int offset = 0; offset < length; ) {
		    final int codepoint = Character.codePointAt(strTest, offset);
			if(Character.isIdeographic(codepoint)){
				counter++;
			};
			
		    offset += Character.charCount(codepoint);
		}
		return counter;
	}

}
