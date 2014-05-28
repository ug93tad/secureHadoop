package crypto;

import java.util.Random;

/**
 * wrapper for Paillier - additive homomorphism
 *
 */
public class Paillier {
	static{
		System.loadLibrary("hryto"); 
	}
	
	public static final String PUBLIC_KEY_FILE = "paillier.pub.key";
	public static final String PRIVATE_KEY_FILE = "paillier.priv.key"; 
	
	//generate random key, storing to files accordingly
	public native void keygen(String pubFile, String privFile);
	public native void init_public_key(String pubFile); 
	public native void init_private_key(String privFile); 
	
	public native byte[] encrypt(int val); 
	public native byte[] add(byte[] val1, byte[] val2); 
	public native byte[] subtract(byte[] val1, byte[] val2); 
	
	public native long decrypt(byte[] val); 
	public native byte[] decryptRaw(byte[] val); 
	public native String decryptToString(byte[] val); 
	
	public static void main(String[] args){
		Paillier paillier = new Paillier();
		paillier.keygen(PUBLIC_KEY_FILE, PRIVATE_KEY_FILE);
		paillier.init_public_key(PUBLIC_KEY_FILE);
		paillier.init_private_key(PRIVATE_KEY_FILE);
		long start = System.currentTimeMillis();
		Random rand = new Random();
		int size = new Integer(args[0]).intValue(); 
		Paillier paillier1 = new Paillier();
		paillier1.init_public_key(PUBLIC_KEY_FILE);
		paillier1.init_private_key(PRIVATE_KEY_FILE);
		
		
		for (int i=0; i<size; i++){
			int m = rand.nextInt(100); 
			byte[] enc1 = paillier.encrypt(m);
			if (enc1.length!=256){
				System.out.println("WRONG: "+enc1.length+" for value "+m);
			}
			System.out.println("dec = "+paillier1.decrypt(enc1)); 
		}
		long end = System.currentTimeMillis(); 
	}
}
