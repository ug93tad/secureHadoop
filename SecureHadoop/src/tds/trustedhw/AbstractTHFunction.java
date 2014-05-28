package tds.trustedhw;

import java.io.IOException;
import java.util.List;

import tds.ICrypto;
import tds.common.Rand;

/**
 * All trusted hardware functions use RND. This class implements methods used for them.  
 *
 */
public class AbstractTHFunction implements ICrypto{

	Rand rand = new Rand(); 		
	
	@Override
	public void keyGen(String pub, String priv) {
		rand.keyGen(pub, priv); 		
	}

	@Override
	public void initPublicParameters(List pub) {
		rand.initPublicParameters(pub); 		
	}

	@Override
	public void initPrivateParameters(List priv) {
		rand.initPrivateParameters(priv); 		
	}

	@Override
	public byte[] encryptRaw(byte[] plaintext) throws IOException {
		return rand.encryptRaw(plaintext); 
	}

	@Override
	public byte[] encryptString(String plaintext) throws IOException {
		return rand.encryptString(plaintext); 
	}

	@Override
	public byte[] decryptRaw(byte[] ciphertext) throws IOException {
		return rand.decryptRaw(ciphertext); 
	}

	@Override
	public String decryptToText(byte[] ciphertext) throws IOException {
		return rand.decryptToText(ciphertext); 
	}
}
