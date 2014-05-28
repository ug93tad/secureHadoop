package crypto;

import java.util.Random;

public class Switchable {
	static {
		System.loadLibrary("hryto");
	}
	public static final String PAILLIER_PUBLIC_KEY_FILE = "paillier.pub.key";
	public static final String PAILLIER_PRIVATE_KEY_FILE = "paillier.priv.key";
	public static final String ELGAMAL_PUBLIC_KEY_FILE = "elgamal.pub.key";
	public static final String ELGAMAL_PRIVATE_KEY_FILE = "elgamal.priv.key";

	// generate random key, storing to files accordingly
	public native void init(String paillierPubFile, String paillierPrivFile,
			String elgamalPubFile, String elgamalPrivFile);
	public native void init(String paillierPubFile, String paillierPrivFile,
			String elgamalPubFile, String elgamalPrivFile, String opeKey, int pLen, int cLen);
	
	public native byte[] addToMul(byte[] val); 
	public native byte[] addToOpe(byte[] val);
	public native byte[] mulToAdd(byte[] val); 
	public native byte[] mulToOpe(byte[] val); 
	public native byte[] opeToAdd(byte[] val); 
	public native byte[] opeToMul(byte[] val); 
		
	public native String bytesToZZString(byte[] val); 
	
	public static void main(String[] args) {
	}
}
