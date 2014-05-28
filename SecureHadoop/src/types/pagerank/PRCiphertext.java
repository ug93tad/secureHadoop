package types.pagerank;

import types.Ciphertext;
import types.PaillierCiphertext;

public class PRCiphertext extends Ciphertext{
	
	public PRCiphertext(){
		super(); 
	}
	
	public PRCiphertext(byte[] bt){
		super(bt); 
	}
}
