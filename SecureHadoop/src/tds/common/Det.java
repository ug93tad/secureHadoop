package tds.common;

import java.io.IOException;
import java.util.List;

import tds.CryptoOperationNotSupportedException;
import tds.ICrypto;

public class Det implements ICrypto{
	static{
		System.loadLibrary("hryto"); 
	}				
	Rand rand; 
	byte[] fixedIv; 
		
	@Override
	public void initPublicParameters(List pub) {
		// Do Nothing here	
	}

	/* <key> <fixed IV> 
	 */
	@Override
	public void initPrivateParameters(List priv) {
		this.rand = new Rand();
		this.rand.init((String)priv.get(0)); 
		this.fixedIv = ((String)priv.get(1)).getBytes(); 		
	}

	@Override
	public byte[] encryptRaw(byte[] plaintext) throws IOException {
		return this.rand.encrypt_word_cbc(plaintext, fixedIv); 
	}

	@Override
	public byte[] encryptString(String plaintext) throws IOException {
		return this.rand.encrypt_word_cbc(plaintext, fixedIv);
	}

	@Override
	public byte[] decryptRaw(byte[] ciphertext) throws IOException {
		return this.rand.decrypt_word_cbc(ciphertext, fixedIv); 
	}

	@Override
	public String decryptToText(byte[] ciphertext) throws IOException {
		return new String(decryptRaw(ciphertext)); 
	}

	@Override
	public void keyGen(String pub, String priv) {
		//DO NOTHING
		
	}

}
