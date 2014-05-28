package tds.hom;

import tds.io.Ciphertext;

public class SearchToken extends Ciphertext{
	public byte[] ciphertext;
	public byte[] wordKey;
	public SearchToken(byte[] a, byte[] b){
		this.ciphertext = a;
		this.wordKey = b; 
	}	
	
	@Override
	public String toString(){
		String ct="";
		for (int i=0; i<ciphertext.length; i++)
			ct+=(char)ciphertext[i];
		String wk="";
		for (int i=0; i<wordKey.length; i++)
			wk+=(char)wordKey[i];
		return ct+"\n"+wk;
	}
}
