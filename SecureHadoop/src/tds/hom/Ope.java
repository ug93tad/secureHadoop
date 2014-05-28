package tds.hom;

import java.io.IOException;
import java.util.List;

import tds.CryptoOperationNotSupportedException;
import tds.ICrypto;
import tds.Utils;
import tds.functions.ICompareFunction;
import tds.io.Ciphertext;

/**
 * OPE ciphertext must be in string format and must be padded so that 
 * they can be readily using Text objects (esp. during the shuffle phase
 * of Hadoop)
 *
 */
public class Ope implements ICrypto, ICompareFunction{
	static{
		System.loadLibrary("hryto"); 
	}
		
	//init with given plaintext and ciphertext length
	 native void init(String key, int pLen, int cLen);
	
	 native byte[] encrypt(byte[] pt);
		 	 
	 native byte[] decrypt(byte[] ct); 					 
		
	 private String key; 
	 private int pLen, cLen, maxLength; 
	 
	public int getpLen() {
		return pLen;
	}

	public int getcLen() {
		return cLen;
	}

	public String getKey() {
		return key;
	}

	@Override
	public void keyGen(String pub, String priv) {
		//DO NOTHING HERE		
	}

	@Override
	public void initPublicParameters(List pub) {
		//DO NOTHING HERE		
	}

	@Override
	public void initPrivateParameters(List priv) {
		//<key> <plength> <clenght>
		this.key = (String)priv.get(0); 
		this.pLen = Integer.parseInt((String)priv.get(1));
		this.cLen = Integer.parseInt((String)priv.get(2));
				
		this.init(key, pLen, cLen); 		
	}

	/* Have to reverse the order of the input, for ZZ encoding is little endian
	 * 
	 * The result must also be converted to Big Endian, for String comparison
	 */
	@Override
	public byte[] encryptRaw(byte[] plaintext) throws IOException {			
		byte[] ct = this.encrypt(Utils.reverseOrder(plaintext));
		return Utils.reverseOrder(ct);		 
	}

	@Override
	public byte[] encryptString(String plaintext) throws IOException {
		//DO NOTHING HERE
		throw new CryptoOperationNotSupportedException(
				"OPE for now does not support String encryption. Convert input to bytes"); 
	}

	@Override
	public byte[] decryptRaw(byte[] ciphertext) throws IOException {
		//reverse the byte order, then decrypt, then reverse again		
		return Utils.reverseOrder(this.decrypt(Utils.reverseOrder(ciphertext)));		 
	}

	@Override
	public String decryptToText(byte[] ciphertext) throws IOException {
		// TODO Auto-generated method stub
		throw new CryptoOperationNotSupportedException("OPE decryption not supported");
	}

	/* All in Big Endian format
	 */
	@Override
	public int compare(Ciphertext val1, Ciphertext val2) {
		return new String(val1.getContent().copyBytes()).compareTo(new String(val2.getContent().copyBytes()));				 
	}
}
