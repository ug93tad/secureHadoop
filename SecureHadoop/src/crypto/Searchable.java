package crypto;

public class Searchable {	
	static{
		System.loadLibrary("hryto"); 
	}
	
	public native void init(String key); 
	
	public native byte[] encrypt(String word); 
	
	public native SearchToken getToken(String word); 
		
	public native boolean match(byte[] ciphertext, SearchToken token); 
	
	public static void main(String[] args){
		String key = "hello world ande"; 
		Searchable se = new Searchable();
		se.init(key);
		String word = "testing123456789abcdeftesting123456789abcdef";
		SearchToken token = se.getToken(word);
		System.out.println("ciphertext size= "+token.ciphertext.length+" ; wordKey size = "+token.wordKey.length); 		
		System.out.println("LD_LIBRARY_PATH = " + System.getProperty("LD_LIBRARY_PATH")); 
		System.out.println("java library path = "+System.getenv("java.library.path")); 
		byte[] cipher1 = se.encrypt(word+""); 		
		byte[] cipher2 = se.encrypt(word+" "); 
		System.out.println("RESULT1 = " + se.match(cipher1, token));
		System.out.println("RESULT2 = " + se.match(cipher2, token));
	}
}
