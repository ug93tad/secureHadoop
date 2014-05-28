package tds.functions;

import tds.io.Ciphertext;

public interface IAddFunction {
	public Ciphertext add(Ciphertext val1, Ciphertext val2) throws Exception; 
	public Ciphertext subtract(Ciphertext val1, Ciphertext val2) throws Exception;
}
