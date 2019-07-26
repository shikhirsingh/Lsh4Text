package com.shikhir.lsh.shingling;

import java.util.Arrays;
import java.util.Iterator;
import java.util.TreeSet;

import com.shikhir.hash.MurmurHash;
import com.shikhir.util.stringops.StringOperations;

import opennlp.tools.tokenize.SimpleTokenizer;
import opennlp.tools.ngram.NGramModel;
import opennlp.tools.util.StringList;

public class ShinglingSet{

	private TreeSet<Integer> shinglingSet = new TreeSet<Integer>();
	private final static int CJK_NGRAM = 3;
	
	public ShinglingSet(){
	}	
	
	ShinglingSet(String set, int kGramsMin, int kGramsMax){
		addShingling(set, kGramsMin, kGramsMax);
	}
	
	
	public static Integer[] getTokensForMessage(String message, int kGramsMin, int kGramsMax) {
		TreeSet<Integer> localSet = new TreeSet<Integer>();

        NGramModel nGramModel = new NGramModel();

		if(StringOperations.countCJKCharecters(message)>0) {
	        nGramModel.add(message, CJK_NGRAM, CJK_NGRAM);
		}
		else {
			StringList slTokens = new StringList(SimpleTokenizer.INSTANCE.tokenize(message.toLowerCase()));
	        nGramModel.add(slTokens, kGramsMin, kGramsMax); // TO DO>
		}
        for (StringList ngram : nGramModel) {
			Integer hashVal = new Integer(MurmurHash.hash32(ngram.toString()));
			localSet.add(hashVal);        	
        }

		Object[] objArray = localSet.toArray();
		Integer[] tokenArray = Arrays.copyOf(objArray, objArray.length, Integer[].class);

		
		return tokenArray;

	}
	

	public void addShingling(String shingling, int kGramsMin, int kGramsMax) {
		
		Integer[] tokenArray = getTokensForMessage(shingling, kGramsMin, kGramsMax);
		
		for(Integer tkn : tokenArray) {
			shinglingSet.add(tkn);
		}
	}

	public int size() {
		return shinglingSet.size();
	}

	public boolean contains(Integer token){
		return shinglingSet.contains(token);
	}
	
	
	public static boolean hasCjk(String body) {
		return false;
	}


	public Integer[] getAllTokens() {
		Integer[] tokenArray = new Integer[shinglingSet.size()];
        Iterator<Integer> value = shinglingSet.iterator(); 

        int i=0;
        while (value.hasNext() && i<tokenArray.length) { 
        	tokenArray[i] = value.next();
        	i++;
        } 
		
		return tokenArray;
	}
	
	public Integer[] subset(int count) {
		
		Integer[] subSet = new Integer[count];

        Iterator<Integer> value = shinglingSet.iterator(); 

        int i=0;
        while (value.hasNext() && i<count) { 
        	subSet[i] = value.next();
        	i++;
        } 

        return subSet;
	}

}
