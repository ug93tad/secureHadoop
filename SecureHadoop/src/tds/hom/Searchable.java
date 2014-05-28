package tds.hom;

import java.io.IOException;
import java.util.List;

import tds.CryptoOperationNotSupportedException;
import tds.ICrypto;
import tds.functions.IEqualityFunction;
import tds.io.Ciphertext;

public class Searchable implements ICrypto, IEqualityFunction{	
	static{
		System.loadLibrary("hryto"); 
	}
	
	native void init(String key); 
	
	native byte[] encrypt(String word); 
	
	native SearchToken getToken(String word); 
		
	native boolean match(byte[] ciphertext, SearchToken token); 		

	public SearchToken genToken(String keyword){
		return this.getToken(keyword); 
	}
	
	@Override
	public void keyGen(String pub, String priv) {
		// DO NOTHING HERE
		
	}

	@Override
	public void initPublicParameters(List pub) {
		// DO NOTHING HERE
		
	}

	@Override
	public void initPrivateParameters(List priv) {
		this.init((String)priv.get(0)); 		
	}

	
	@Override
	public byte[] encryptRaw(byte[] plaintext) throws IOException {
		throw new CryptoOperationNotSupportedException("SEARCH scheme encrypts strings only"); 
	}

	@Override
	public byte[] encryptString(String plaintext) throws IOException {
		return this.encrypt(plaintext); 
	}

	@Override
	public byte[] decryptRaw(byte[] ciphertext) throws IOException {
		throw new CryptoOperationNotSupportedException("SEARCH does not support decryption"); 
	}

	@Override
	public String decryptToText(byte[] ciphertext) throws IOException {
		throw new CryptoOperationNotSupportedException("SEARCH does not support decryption"); 
	}
	
	@Override	
	public boolean isAMatch(Ciphertext ciphertext, Ciphertext token){
		return this.match(ciphertext.getContent().copyBytes(), (SearchToken)token); 
	}
}
