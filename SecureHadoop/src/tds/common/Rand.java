package tds.common;

import java.io.IOException;
import java.util.List;

import tds.CryptoOperationNotSupportedException;
import tds.ICrypto;
import tds.TdsOptions;
import tds.io.Ciphertext;

public class Rand implements ICrypto{	
	static{
		System.loadLibrary("hryto"); 
	}
		
		
		 native void init(String key);
		//no padding
		 native byte[] encrypt_word_rnd(String word, byte[] iv); 
		 native byte[] decrypt_word_rnd(byte[] ciphertext, byte[] iv);
		//with padding
		 native byte[] encrypt_word_cbc(String word, byte[] iv); 
		 native byte[] encrypt_word_cbc(byte[] word, byte[] iv); 
		 native byte[] decrypt_word_cbc(byte[] ciphertext, byte[] iv);
		//random bytes
		 public native byte[] randomBytes(int length); 
		
		//assuming that the first 16 bytes are the iv
		public byte[] decryptBytes(byte[] ct){
			byte[] iv = new byte[TdsOptions.IV_LENGTH]; 
			System.arraycopy(ct, 0, iv, 0, TdsOptions.IV_LENGTH); 
			byte[] cipher = new byte[ct.length-TdsOptions.IV_LENGTH];
			System.arraycopy(ct, TdsOptions.IV_LENGTH, cipher, 0, ct.length-TdsOptions.IV_LENGTH);
			return this.decrypt_word_cbc(cipher, iv); 			
		}
		
		//generate the first random 16-byte iv
		private byte[] encryptBytesNative(byte[] pt){
			byte[] iv = this.randomBytes(TdsOptions.IV_LENGTH); 
			byte[] ct = this.encrypt_word_cbc(pt, iv); 
			byte[] result = new byte[iv.length+ct.length]; 
			System.arraycopy(iv, 0, result, 0, TdsOptions.IV_LENGTH); 
			System.arraycopy(ct, 0, result, TdsOptions.IV_LENGTH, ct.length); 
			return result; 
		}
		
		//generate the first random 16-byte iv
		private byte[] encryptStringNative(String word){
			byte[] iv = this.randomBytes(TdsOptions.IV_LENGTH); 
			byte[] ct = this.encrypt_word_cbc(word, iv);  
			byte[] result = new byte[iv.length+ct.length]; 
			System.arraycopy(iv, 0, result, 0, TdsOptions.IV_LENGTH); 
			System.arraycopy(ct, 0, result, TdsOptions.IV_LENGTH, ct.length); 
			return result; 
		}
				
		@Override
		public void initPublicParameters(List pub) {
			// Do nothing here			
		}
		
		@Override
		public void initPrivateParameters(List priv) {
			String key = (String)priv.get(0);
			this.init(key); 			
		}
		
		@Override
		public byte[] encryptRaw(byte[] plaintext) {
			return encryptBytesNative(plaintext); 			
		}
		
		@Override
		public byte[] encryptString(String plaintext) {
			return this.encryptStringNative(plaintext); 
		}
		
		@Override
		public byte[] decryptRaw(byte[] ciphertext) {			
			return this.decryptBytes(ciphertext); 
		}
		
		@Override
		public String decryptToText(byte[] ciphertext) throws IOException{
			return new String(decryptRaw(ciphertext));  			
		}
		@Override
		public void keyGen(String pub, String priv) {
			//DO NOTHING
			
		}		
		
		
}
