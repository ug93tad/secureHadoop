package tds.functions;

import tds.io.Ciphertext;

public interface IMultiplyFunction {
	public Ciphertext product(Ciphertext val1, Ciphertext val2) throws Exception; 
	public Ciphertext product(Ciphertext val1, double factor) throws Exception; 
}
