package tds.hom;

import java.io.IOException;
import java.util.List;
import java.util.Random;

import tds.CryptoOperationNotSupportedException;
import tds.ICrypto;
import tds.functions.IAddFunction;
import tds.io.Ciphertext;

/**
 * wrapper for Paillier - additive homomorphism
 *
 */
public class Paillier implements ICrypto, IAddFunction{
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
	private native void keygen(String pubFile, String privFile);
	private native void init_public_key(String pubFile); 
	private native void init_private_key(String privFile); 
	
	private native byte[] encrypt(int val); 
	private native byte[] add(byte[] val1, byte[] val2); 
	private native byte[] subtract(byte[] val1, byte[] val2); 
		
	//convert to long if needed to
	private native String decryptToString(byte[] val); 	
	
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
		throw new CryptoOperationNotSupportedException("Paillier encrypts numbers only"); 
	}
	
	@Override
	public byte[] encryptString(String plaintext) throws IOException {
		return this.encrypt(Integer.parseInt(plaintext)); 
	}
	@Override
	public void keyGen(String pub, String priv) {		
		this.keygen(pub, priv); 
	}
	
	@Override
	public String decryptToText(byte[] ciphertext) throws IOException {
		return this.decryptToString(ciphertext); 
	}
	@Override
	public byte[] decryptRaw(byte[] ciphertext) throws IOException {
		throw new CryptoOperationNotSupportedException("Paillier does not decrypt to bytes (see JNI wrapper)");  
	}
	
	@Override
	public Ciphertext add(Ciphertext val1, Ciphertext val2){
		byte[] sum = this.add(val1.getContent().copyBytes(), val2.getContent().copyBytes()); 
		return new Ciphertext(sum); 
	}
	
	@Override
	public Ciphertext subtract(Ciphertext val1, Ciphertext val2){
		byte[] sub = this.subtract(val1.getContent().copyBytes(), val2.getContent().copyBytes()); 
		return new Ciphertext(sub); 
	}
}
