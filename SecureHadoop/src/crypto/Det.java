package crypto;

public class Det {
	static{
		System.loadLibrary("hryto"); 
	}
	
	//default choice is AES, CFB mode
	public native void det_init(String key, String iv);
	
	//det
	public native byte[] encrypt_word(String word); 	
	public native byte[] decrypt_word(byte[] ciphertext); 
		
	//randomize
	public native byte[] encrypt_word_rnd(String word, byte[] iv); 
	public native byte[] decrypt_word_rnd(byte[] ciphertext, byte[] iv); 	
	public native byte[] randomBytes(int length); 
		
	
	public static void main(String[] args){
		String key="hello world ande";
		String iv="dontusethisinput";
		Det crypto = new Det();
		crypto.det_init(key, iv); 
		
		String[] s = args[0].split(" "); 
		for (int i = 0; i < s.length; i++) {
			byte[] ct = crypto.encrypt_word(s[i]);
			System.out.format("ciphertext of %d bytes: %s\n", ct.length,
					Det.bytesToHex(ct));
			byte[] pt = crypto.decrypt_word(ct);
			System.out.format("decrypted text of %d bytes: %s\n", pt.length,
					new String(pt));
		}
	}
	
	final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
	public static String bytesToHex(byte[] bytes) {
	    char[] hexChars = new char[bytes.length * 2];
	    int v;
	    for ( int j = 0; j < bytes.length; j++ ) {
	        v = bytes[j] & 0xFF;
	        hexChars[j * 2] = hexArray[v >>> 4];
	        hexChars[j * 2 + 1] = hexArray[v & 0x0F];
	    }
	    return new String(hexChars);
	}
}
