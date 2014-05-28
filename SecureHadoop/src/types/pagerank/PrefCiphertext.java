package types.pagerank;

import types.Ciphertext;
import types.PaillierCiphertext;

public class PrefCiphertext extends Ciphertext{
	
	public PrefCiphertext(){
		super(); 
	}
	
	public PrefCiphertext(byte[] bt){
		super(bt); 
	}
}
