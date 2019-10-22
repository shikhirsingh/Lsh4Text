package com.shikhir.lsh.untrimmed.forest.shingling;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class ForestShingle implements Comparable<ForestShingle>{
	Shingle shingle;
	private int count;          // current value

	
	public ForestShingle(){
		
	}
    // create a new counter with the given parameters
    public ForestShingle(String token, int count) {
    	shingle = new Shingle(token);
        this.count = count;
    } 

    // increment the counter by 1
    public void increment() {
        ++count;
    } 

    public String getToken() {
    	return shingle.getToken();
    }

    @JsonIgnore
    public int getId() {
    	return shingle.getId();
    }
    // return the current count
    public int getShingleCountInForest() {
        return count;
    } 

    public String toString() {
        return "ForestShingle [ shingle: "+shingle+", count: "+ count+ " ]";

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
