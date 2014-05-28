package types;

public class ElgamalCiphertext extends Ciphertext{
	public static int ELGAMAL_LENGTH = 192;
	
	public ElgamalCiphertext(){
		super(); 
	}
	
	public ElgamalCiphertext(byte[] ct){
		super(ct); 
	}
}
