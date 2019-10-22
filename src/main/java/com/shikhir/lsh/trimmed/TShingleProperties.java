package com.shikhir.lsh.trimmed;


public class TShingleProperties  {
	private int location;
	private Float percentage;
	
	public TShingleProperties(){
		
	}

	TShingleProperties (float percentage){
		setPercentage(percentage);
	}
	
	
	TShingleProperties (float percentage, Integer location){
		setLocation(location);
		setPercentage(percentage);
	}

	public void setLocation(Integer location) {
		this.location = location;
	}

	public int getLocation() {
		return this.location;
	}
	
	public Float getPercentage() {
		return this.percentage;
	}

	public void setPercentage(float percentage) {
		this.percentage = percentage;
	}

    public String toString() {
        return "TShingleProperties [ location: "+location+", percentage: "+ percentage+ " ]";
    }
	
}
