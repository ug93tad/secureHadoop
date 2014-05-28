package types;

public class PaillierCiphertext extends Ciphertext{
	public static int PAILLIER_LENGTH = 256; 
	
	public PaillierCiphertext(){
		super(); 		
	}
	
	public PaillierCiphertext(byte[] ct){
		super(ct); 
	}
}
