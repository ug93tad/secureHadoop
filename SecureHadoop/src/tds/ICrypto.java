package tds;

import java.io.IOException;
import java.util.List;

import org.apache.hadoop.mapreduce.TaskAttemptContext;

import tds.io.Ciphertext;


/**
 * @author dinhtta
 * All crypto realization implements this
 */
public interface ICrypto {	
	//generate key (for public-key schemes)
	public void keyGen(String pub, String priv); 
	
	public void initPublicParameters(List pub); 
	public void initPrivateParameters(List priv); 
	
	public byte[] encryptRaw(byte[] plaintext) throws IOException;
	public byte[] encryptString(String plaintext) throws IOException; 
	public byte[] decryptRaw(byte[] ciphertext) throws IOException;
	public String decryptToText(byte[] ciphertext) throws IOException;
		
}
