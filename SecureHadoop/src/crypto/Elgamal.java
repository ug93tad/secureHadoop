package crypto;

import java.util.Random;

/**
 * wrapper for Paillier - additive homomorphism
 *
 */
public class Elgamal {
	static{
		System.loadLibrary("hryto"); 
	}
	
	public static final String PUBLIC_KEY_FILE = "elgamal.pub.key";
	public static final String PRIVATE_KEY_FILE = "elgamal.priv.key"; 
	
	//generate random key, storing to files accordingly
	public native void keygen(String pubFile, String privFile);
	public native void init_public_key(String pubFile); 
	public native void init_private_key(String privFile); 
	
	public native byte[] encrypt(int val); 
	public native byte[] multiply(byte[] val1, byte[] val2); 
	
	public native long decrypt(byte[] val); 

	public native byte[] decryptRaw(byte[] val); 

	public native String decryptToString(byte[] val); 
	
	public static void main(String[] args){
		Elgamal elgamal = new Elgamal();
		elgamal.init_public_key("/home/dinhtta/elgamal.pub.key"); 
		elgamal.init_private_key("/home/dinhtta/elgamal.priv.key");
		byte[] val1 = elgamal.encrypt(1726);
		byte[] val2 = elgamal.encrypt(13281);
		byte[] mul = elgamal.multiply(val1, val2); 
		System.out.println("result = "+elgamal.decryptToString(mul)); 
	}
}
