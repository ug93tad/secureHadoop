package tds.hom;

import java.io.IOException;
import java.util.List;
import java.util.Random;

import tds.CryptoOperationNotSupportedException;
import tds.ICrypto;
import tds.functions.IMultiplyFunction;
import tds.io.Ciphertext;

/**
 * wrapper for Paillier - additive homomorphism
 *
 */
public class Elgamal implements ICrypto, IMultiplyFunction{
	static{
		System.loadLibrary("hryto"); 
	}		
	
	private String pubKey, privKey; 
	
	public String getPubKey() {
		return pubKey;
	}
	public String getPrivKey() {
		return privKey;
	}
	//generate random key, storing to files accordingly
	 native void keygen(String pubFile, String privFile);
	 native void init_public_key(String pubFile); 
	 native void init_private_key(String privFile); 
	
	 native byte[] encrypt(int val); 
	 native byte[] multiply(byte[] val1, byte[] val2); 
	
	 native long decrypt(byte[] val); 


	 native String decryptToString(byte[] val); 	
	
	@Override
	public void keyGen(String pub, String priv) {
		this.keygen(pub, priv); 		
	}
	
	@Override
	public void initPublicParameters(List pub) {
		this.pubKey = (String)pub.get(0); 
		this.init_public_key(this.pubKey); 
		
	}
	
	@Override
	public void initPrivateParameters(List priv) {
		this.privKey = (String)priv.get(0); 
		this.init_private_key(this.privKey); 		
	}
	
	@Override
	public byte[] encryptRaw(byte[] plaintext) throws IOException {
		throw new CryptoOperationNotSupportedException("Elgamal encrypts numbers only"); 
	}
	
	@Override
	public byte[] encryptString(String plaintext) throws IOException {
		return this.encrypt(Integer.parseInt(plaintext)); 
	}
	
	@Override
	public String decryptToText(byte[] ciphertext) throws IOException {
		return this.decryptToString(ciphertext); 
	}
	
	@Override
	public byte[] decryptRaw(byte[] ciphertext) throws IOException {
		throw new CryptoOperationNotSupportedException("Elgamal does not decrypt to bytes (see JNI wrapper)"); 
	}
	
	@Override
	public Ciphertext product(Ciphertext val1, Ciphertext val2){
		return new Ciphertext(this.multiply(val1.getContent().copyBytes(), val2
				.getContent().copyBytes()));
	}
	
	@Override
	public Ciphertext product(Ciphertext val1, double factor) throws Exception{
		throw new CryptoOperationNotSupportedException(
				"Elgmal does not support muliplication of ciphertext with doubles");
	}
	
}
