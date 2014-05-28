package tds.hom.switchable;

import java.io.IOException;
import java.util.List;
import java.util.Random;

import tds.CryptoOperationNotSupportedException;
import tds.ICrypto;
import tds.Utils;
import tds.hom.Elgamal;
import tds.hom.Ope;
import tds.hom.Paillier;
import tds.io.Ciphertext;

public class Switchable implements ICrypto{
	static {
		System.loadLibrary("hryto");
	}
	
	// generate random key, storing to files accordingly
	 native void init(String paillierPubFile, String paillierPrivFile,
			String elgamalPubFile, String elgamalPrivFile);
	 native void init(String paillierPubFile, String paillierPrivFile,
			String elgamalPubFile, String elgamalPrivFile, String opeKey, int pLen, int cLen);
	
	 native byte[] addToMulNative(byte[] val); 
	 native byte[] addToOpeNative(byte[] val);
	 native byte[] mulToAddNative(byte[] val); 
	 native byte[] mulToOpeNative(byte[] val); 
	 native byte[] opeToAddNative(byte[] val); 
	 native byte[] opeToMulNative(byte[] val); 
	 
	 public Ciphertext addToMul(Ciphertext val){
		 return new Ciphertext(addToMulNative(val.getContent().copyBytes())); 
	 }
	 
	 /**
	 * Convert to Big Endian before returning
	 */
	public Ciphertext addToOpe(Ciphertext val) throws CryptoOperationNotSupportedException{
		byte[] ct = this.addToOpeNative(val.getContent().copyBytes()); 
		return new Ciphertext(Utils.reverseOrder(ct)); 	  
	 }
	
	 public Ciphertext mulToAdd(Ciphertext val){
		 return new Ciphertext(mulToAddNative(val.getContent().copyBytes())); 
	 }
	 
	 public Ciphertext mulToOpe(Ciphertext val) throws CryptoOperationNotSupportedException{
		byte[] ct = this.mulToOpeNative(val.getContent().copyBytes()); 
		return new Ciphertext(Utils.reverseOrder(ct)); 
	 }
	 
	 public Ciphertext opeToAdd(Ciphertext val){
		 //ope ciphertext for comparison must first be switched back to Little Endian format
		 byte[] ct = Utils.reverseOrder(val.getContent().copyBytes()); 
		 return new Ciphertext(opeToAddNative(ct)); 
	 }
	 
	 public Ciphertext opeToMul(Ciphertext val){
		//ope ciphertext for comparison must first be switched back to Little Endian format
		 byte[] ct = Utils.reverseOrder(val.getContent().copyBytes());
		 return new Ciphertext(opeToMulNative(ct)); 
	 }
	 
	 public Switchable(Paillier paillier, Elgamal elgamal, Ope ope){
		this.init(paillier.getPubKey(), paillier.getPrivKey(),
				elgamal.getPubKey(), elgamal.getPrivKey(), ope.getKey(),
				ope.getpLen(), ope.getcLen());		
	 }
	
	 public Switchable(Paillier paillier, Elgamal elgamal){
			this.init(paillier.getPubKey(), paillier.getPrivKey(),
					elgamal.getPubKey(), elgamal.getPrivKey());
		 }	 
	
	@Override
	public void keyGen(String pub, String priv) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void initPublicParameters(List pub) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void initPrivateParameters(List priv) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public byte[] encryptRaw(byte[] plaintext) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public byte[] encryptString(String plaintext) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public byte[] decryptRaw(byte[] ciphertext) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public String decryptToText(byte[] ciphertext) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}
}
