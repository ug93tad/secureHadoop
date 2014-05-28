package types.pagerank;

import types.Ciphertext;
import types.PaillierCiphertext;

public class EdgeCiphertext extends Ciphertext{
	
	public EdgeCiphertext(){
		super(); 
	}
	
	public EdgeCiphertext(byte[] bt){
		super(bt); 
	}
}
