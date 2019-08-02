package com.shikhir.lsh.shingling;

import com.shikhir.hash.MurmurHash;

public class Shingle implements Comparable<Shingle> {
	private Integer id=null;
	private String token = null;
		
	public Shingle(String token){
		if(token==null) {
			id=0;
			return;
		};
		this.id = MurmurHash.hash32(token);
		this.token = token;
	}
	
	public Integer getId() {
		return id;
	}
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
		this.id = MurmurHash.hash32(token);
	}
    public boolean equals(Object o) {
        return (o instanceof Shingle) && (((Shingle) o).getId()).equals(this.getId());
    }

    public int hashCode() {
        return id;
    }

	@Override
	public int compareTo(Shingle that) {
		// TODO Auto-generated method stub
		if(that==null) throw new IllegalArgumentException();
		int val= this.getId().compareTo(that.getId());
        return val;
	}

	public String toString() {
        return token;
    }

}
