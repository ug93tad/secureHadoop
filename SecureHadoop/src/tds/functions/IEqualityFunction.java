package tds.functions;

import tds.hom.SearchToken;
import tds.io.Ciphertext;

public interface IEqualityFunction {
	public boolean isAMatch(Ciphertext ciphertext, Ciphertext token) throws Exception; 
}
