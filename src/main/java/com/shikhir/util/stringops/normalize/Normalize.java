package com.shikhir.util.stringops.normalize;


public class Normalize {
	
	public static String hex(String body) {
		//[a-fA-F][0-9a-fA-F]{20,}
		return body;
	}
	public static String base64(String body) {
		//[a-fA-F][0-9a-fA-F]{20,}
		return body;
	}
	public static String removeExtraWhiteSpace(String body) {
	    String regex = "\\s+";	
		return body.replaceAll(regex, " ").trim();
	}
	public static String all(String body) {
		if(body!=null && body.length()>0) {
			String retVal = Digits.normalize(body);
			retVal = Urlz.normalize(retVal);
			return removeExtraWhiteSpace(retVal);			
		}
		else return body;
	}
	
	
	public static String stripNormalizedReplacements(String body) {
		return removeExtraWhiteSpace(Urlz.strip(Digits.strip(body)));
	}
}