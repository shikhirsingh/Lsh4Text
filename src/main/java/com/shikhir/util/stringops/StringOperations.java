package com.shikhir.util.stringops;

public class StringOperations {

	public static int binarySearch(String[] arr, String x) 
    { 
        int l = 0, r = arr.length - 1; 
        while (l <= r) { 
            int m = l + (r - l) / 2; 
  
            int res = x.compareTo(arr[m]); 
  
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
	 * This method is used to test if the string contains characters that are from
	 * the CJKV languages (Chinese, Japanese, Korean, or Vietnamese ).
	 * The CJKV languages are unique because they don't use space a separator 
	 * for words. This is often important because the words need to be tokenized
	 * differently.
	 *   
	 * @param strTest The string that needs tested for CJKV
	 * @return the count of CJKV characters in string
	 * @since 0.4.0
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
