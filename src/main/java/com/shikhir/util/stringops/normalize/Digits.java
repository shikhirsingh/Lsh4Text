package com.shikhir.util.stringops.normalize;


import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Digits {
	private static String phone(String body) {
		String retVal=body;
		String regex="(\\+\\d)?[\\d() -]{6,20}+";
		Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
		Matcher m = p.matcher(body);

        while(m.find()){
        	String possibleNumber = m.group().trim();
        	int digit_count = 0;
        	for (int i = 0, len = possibleNumber.length(); i < len; i++) {
        	    if (Character.isDigit(possibleNumber.charAt(i))) {
        	    	digit_count++;
        	    }
        	}
        	if(digit_count>8) {
        		retVal=retVal.replace(possibleNumber, "PHONENUMBERZ");
        	}
        }      
		return retVal;        
	}	
	
	
	private static String currency(String body) {
		String regex = "(\\¥|\\₹|\\$|\\£|\\€|Rs\\.|Rs|CNY|RMB|CNH|人民币|人民幣|元|INR|USD|CAD|CHF|AUD|TRY|GBP|JPY|EURO|EUR)(\\s)*(\\d)+(\\d|\\,|\\.)*(\\d)*((\\:)\\d\\d)?";
		String regex2 = "(\\d)+(\\d|\\,|\\.)*(\\d)*((\\:)\\d\\d)?(\\s)*(\\¥|\\₹|\\$|\\£|\\€|Rs\\.|Rs|CNY|INR|USD|CAD|CHF|AUD|TRY|GBP|JPY|EURO|EUR|CNY|RMB|CNH|人民币|人民幣|元)";
		//https://regex101.com/r/cO8lqs/788
		return body.replaceAll(regex, " CURRENCYZ ").replaceAll(regex2, " CURRENCYZ ");        
	}
	
	private static String time(String body) {
		// http://natty.joestelmach.com/ <-- integrate this asap
        String regex = "([0-1]?[0-9]|[2][0-3]):([0-5][0-9])(\\s)?(am|pm|AM|PM)?";
		return body.replaceAll(regex, " NOONZ ");        
	}
	
	private static String digits(String body) {
        String regex = "\\d+";
		return body.replaceAll(regex, " DIGITZ ");        		
	}
	public static String normalize(String body) {
		String retVal = currency(body);
		retVal = phone(retVal);
		retVal = time(retVal);
		retVal = digits(retVal);
		return retVal;
	}
	
	public static String strip(String body) {
		return body.replace("CURRENCYZ","").replace("DIGITZ","").replace("NOONZ","").replace("PHONENUMBERZ","");
	}
}