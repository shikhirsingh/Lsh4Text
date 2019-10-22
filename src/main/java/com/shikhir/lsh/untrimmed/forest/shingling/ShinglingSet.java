package com.shikhir.lsh.untrimmed.forest.shingling;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.shikhir.util.stringops.NGramSet;
import com.shikhir.util.stringops.StringOperations;

import opennlp.tools.tokenize.SimpleTokenizer;
import opennlp.tools.ngram.NGramModel;
import opennlp.tools.util.StringList;

public class ShinglingSet{

	private TreeMap<Integer, Shingle> shinglingSet = new TreeMap<Integer, Shingle>();
	
	public ShinglingSet(){
	}	
	
	ShinglingSet(String text, boolean wordTokens, int kGramsMin, int kGramsMax){
		addShingling(text, wordTokens, kGramsMin, kGramsMax);
	}

	public static Shingle[] getTokensForMessage(String text, boolean wordTokens, int kGramsMin, int kGramsMax) {
			
		text = text.trim();
		if(text==null || text.length()==0) {
			return new Shingle[0];
		};

        NGramSet nGramModel = new NGramSet();
		StringList slTokens = new StringList(SimpleTokenizer.INSTANCE.tokenize(text.trim()));

		if(slTokens.size()==0) return null;
		if(!wordTokens) { // character tokens
			for(String strTkn: slTokens) {
				nGramModel.add(strTkn, kGramsMin, kGramsMax); 		
			}
		}
		else {
	        nGramModel.add(slTokens, kGramsMin, kGramsMax);
		}
		
		LinkedHashSet<Shingle> localSet = new LinkedHashSet<Shingle>();
		
		for (StringList ngram : nGramModel) {
 
			Shingle s = new Shingle(ngram.toString());
			if(!localSet.contains(s)) {
				localSet.add(s);
			}
        }

		Object[] objArray = localSet.toArray();
		Shingle[] tokenArray = Arrays.copyOf(objArray, objArray.length, Shingle[].class);

		localSet=null;  // to save memory
		nGramModel=null; // to save memory
		return tokenArray;

	}

	public void addShingling(String text, boolean wordTokens, int kGramsMin, int kGramsMax) {
		
		Shingle[] shingleArray = getTokensForMessage(text, wordTokens, kGramsMin, kGramsMax);
		
		for(Shingle s : shingleArray) {
			shinglingSet.put(s.getId(), s);
		}
	}
	public int size() {
		return shinglingSet.size();
	}

	public boolean contains(Integer id){
		return shinglingSet.containsKey(id);
	}
	
	public Integer[] getAllId() {
		Set<Integer> allIdSet = shinglingSet.keySet();
		
        Integer[] arr = Arrays.copyOf(allIdSet.toArray(), allIdSet.size(), Integer[].class); 
        Arrays.sort(arr);

		return arr;
	}
	
	public Integer[] subset(int count) {
		
		if(count>shinglingSet.size()) throw new IllegalArgumentException();
		Integer[] subSet = new Integer[count];

        Set<Integer> keys = shinglingSet.keySet(); 

        int i=0;
        Iterator<Integer> itr = keys.iterator();
        
        while (itr.hasNext() && i<count) { 
        	subSet[i] = itr.next();
        	i++;
        } 

        return subSet;
	}

}
