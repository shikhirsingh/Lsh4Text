package com.shikhir.util.stringops;

import org.apache.commons.lang3.StringUtils;

public class StringOperations {

	/**
	 * A binary search on a sorted string array for a keyword
	 *   
	 * @param preSortedArray The presorted array of String values
	 * @param find The value that is being sought for
	 * @return an index value of the location of the item found in array; or -1 if not found 
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
	 * Remove stop characters from string
	 *   
	 * @param text The input string 
	 * @return return stop characters
	 * @since 1.0.0
	 */

	public static String removeStopChar(String text) {
	    if(StringUtils.isBlank(text)) return "";

		return text.replaceAll("[.?=_,【】%:*;|�!()'-]", "").replaceAll("\\s+"," ").trim();
	}

	/**
	 * This method is used to test if the string contains characters that are from
	 * the CJKV languages (Chinese, Japanese, Korean, or Vietnamese ).
	 * The CJKV languages are unique because they don't use space a separator 
	 * for words. This is often important because the words need to be tokenized
	 * differently.
	 *   
	 * @param strTest The string that needs tested for CJKV
	 * @return the count of CJKV characters in string
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
