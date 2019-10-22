package com.shikhir.lsh.trimmed;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.stream.IntStream;

public class Sentence {
	
	private final String document;
	private ArrayList<Integer> dictionaryLocation;
	private ArrayList<Integer> originalSentence;	
	private float collisionProbability;

	
	Sentence(String document, float percentage, ArrayList<Integer> dictionaryLocation, ArrayList<Integer> originalSentence){
		this.document = document;
		this.collisionProbability = percentage;
		this.dictionaryLocation = dictionaryLocation;
		this.originalSentence = originalSentence;
	}
	
	public long getSignature() {
		if(dictionaryLocation.size()==0) return 0L;
		
		int[] data = getDictionaryLocationAsIntArray();
		if(this.collisionProbability<0.0001 && this.dictionaryLocation.size()>=5) {
			Arrays.sort(data);
		}

		ByteBuffer byteBuffer = ByteBuffer.allocate(data.length * 4);        
        IntBuffer intBuffer = byteBuffer.asIntBuffer();
        intBuffer.put(data);

        byte[] array = byteBuffer.array();

		return com.shikhir.StrWrangler4j.hash.MurmurHash.hash64(array, array.length);
	}

	private int[] getDictionaryLocationAsIntArray() {
		return dictionaryLocation.stream().mapToInt(i -> i).toArray();
	}
	
	public String getDictionaryLocationInBase64() {
		int[] ints = getDictionaryLocationAsIntArray();
	    ByteBuffer buf = ByteBuffer.allocate(ints.length);
	    IntStream.of(ints).forEach(i -> buf.put((byte)i));
	    return Base64.getEncoder().encodeToString(buf.array());
	}

	public float getCollisionProbability() {
		return collisionProbability;
	}

	public ArrayList<Integer> getOriginalSentence() {
		return originalSentence;
	}

	public void setOriginalSentence(ArrayList<Integer> originalSentence) {
		this.originalSentence = originalSentence;
	}
	public ArrayList<Integer> getDictionaryLocation() {
		return dictionaryLocation;
	}

	public void setDictionaryLocation(ArrayList<Integer> dictionarySentence) {
		this.dictionaryLocation = dictionarySentence;
	}

	public boolean collisionLikely() {
		if(dictionaryLocation.size()==originalSentence.size()) {
			return false;
		}
		else if(dictionaryLocation.size()==originalSentence.size()-1 && originalSentence.size() > 4) {
			return false;
		}
		else if(dictionaryLocation.size()>=7) {
			return false;
		}
		else if(collisionProbability<0.005 && dictionaryLocation.size()>=5) {
			return false;
		}
		return true;
		
	}

	public String getDocument() {
		return document;
	}
	
}
