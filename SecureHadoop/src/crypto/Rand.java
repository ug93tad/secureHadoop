package crypto;

public class Rand {
	static{
		System.loadLibrary("hryto"); 
	}
	
		public native void init(String key);
		//no padding
		public native byte[] encrypt_word_rnd(String word, byte[] iv); 
		public native byte[] decrypt_word_rnd(byte[] ciphertext, byte[] iv);
		
		//with padding
		public native byte[] encrypt_word_cbc(String word, byte[] iv); 
		public native byte[] encrypt_word_cbc(byte[] word, byte[] iv); 
		public native byte[] decrypt_word_cbc(byte[] ciphertext, byte[] iv); 
		
		//random bytes
		public native byte[] randomBytes(int length); 
		
		//assuming that the first 16 bytes are the iv
		public byte[] decryptBytes(byte[] ct){
			byte[] iv = new byte[16]; 
			System.arraycopy(ct, 0, iv, 0, 16); 
			byte[] cipher = new byte[ct.length-16];
			System.arraycopy(ct, 16, cipher, 0, ct.length-16);
			return this.decrypt_word_rnd(cipher, iv); 			
		}
		
		//generate the first random 16-byte iv
		public byte[] encrypteBytes(byte[] pt){
			byte[] iv = this.randomBytes(16); 
			byte[] ct = this.encrypt_word_cbc(pt, iv); 
			byte[] result = new byte[iv.length+ct.length]; 
			System.arraycopy(iv, 0, result, 0, 16); 
			System.arraycopy(ct, 0, result, 16, ct.length); 
			return result; 
		}
		
		//generate the first random 16-byte iv
		public byte[] encryptString(String word){
			byte[] iv = this.randomBytes(16); 
			byte[] ct = this.encrypt_word_cbc(word, iv);  
			byte[] result = new byte[iv.length+ct.length]; 
			System.arraycopy(iv, 0, result, 0, 16); 
			System.arraycopy(ct, 0, result, 16, ct.length); 
			return result; 
		}
}
