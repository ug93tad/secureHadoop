package tds.functions;

import tds.io.Ciphertext;

public interface ICompareFunction {
	public int compare(Ciphertext val1, Ciphertext val2) throws Exception; 
}
