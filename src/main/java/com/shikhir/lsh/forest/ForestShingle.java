package com.shikhir.lsh.forest;

public class ForestShingle implements Comparable<ForestShingle>{
    private final int id;     // counter name
    private int count;          // current value

    // create a new counter with the given parameters
    public ForestShingle(int id, int count) {
        this.id = id;
        this.count = count;
    } 

    // increment the counter by 1
    public void increment() {
        count++;
    } 

    public int getId() {
    	return id;
    }
    // return the current count
    public int getShingleCountInForest() {
        return count;
    } 

    // return a string representation of this counter
    public String toString() {
        return id + ": " + count;
    } 

    // compare two Counter objects based on their count
    public int compareTo(ForestShingle that) {
    	
    	Integer thisCount = count;

    	return thisCount.compareTo(that.count);
    }
    
}
