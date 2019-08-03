package com.shikhir.lsh.forest;

import com.shikhir.lsh.shingling.Shingle;

public class ForestShingle implements Comparable<ForestShingle>{
	Shingle shingle;
	private int count;          // current value

    // create a new counter with the given parameters
    public ForestShingle(String token, int count) {
    	shingle = new Shingle(token);
        this.count = count;
    } 

    // increment the counter by 1
    public void increment() {
        count++;
    } 

    public String getToken() {
    	return shingle.getToken();
    }

    public int getId() {
    	return shingle.getId();
    }
    // return the current count
    public int getShingleCountInForest() {
        return count;
    } 

    // return a string representation of this counter
    public String toString() {
        return shingle.getToken()+":"+shingle.getId() + ": " + count;
    } 

    // compare two Counter objects based on their count
    public int compareTo(ForestShingle that) {
    	
    	Integer thisCount = count;
    	if(thisCount.compareTo(that.count)==0) {
    		return that.getToken().length()-this.getToken().length();
    	}
    	return thisCount.compareTo(that.count);
    	
    	
    }
    
    
}
