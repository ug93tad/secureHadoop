package crypto;

public class Ope {
	static{
		System.loadLibrary("hryto"); 
	}
		
	//init with given plaintext and ciphertext length
	public native void init(String key, int pLen, int cLen);
	
	public native byte[] encrypt(byte[] pt);
	
	public native void voidEncrypt(byte[] pt); 
	
	public native byte[] decrypt(byte[] ct); 		
		
	public native int compareBytes(byte[] val1, byte[] val2); 
	
	public static void main(String[] args){
		String key = "hello world ande";
		int pLen = 10; 
		int cLen = 20; 
		
		Ope ope = new Ope(); 
		ope.init(key, pLen, cLen);
		
		int n=new Integer(args[0]).intValue(); 
		long start = System.currentTimeMillis(); 
		for (int i=0; i<n; i++){
			Rand rand = new Rand();		
			byte[] in = rand.randomBytes(10); 
		//System.out.println("plain text = "+Utils.bytesToHex(in));
		
			byte[] out = ope.encrypt(in);
		}
		long end = System.currentTimeMillis(); 
		System.out.println(" time = "+(end-start)); 
		
		/*byte[] dec = ope.decrypt(out);
		System.out.println("decrypted text = "+Utils.bytesToHex(dec));*/
	}
}
