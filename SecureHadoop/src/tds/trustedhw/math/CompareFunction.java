package tds.trustedhw.math;

import tds.functions.ICompareFunction;
import tds.io.Ciphertext;
import tds.trustedhw.AbstractTHFunction;

public class CompareFunction extends AbstractTHFunction implements ICompareFunction{

	@Override
	public int compare(Ciphertext val1, Ciphertext val2) throws Exception {
		String v1 = new String(this.decryptRaw(val1.getContent().copyBytes())); 
		String v2 = new String(this.decryptRaw(val2.getContent().copyBytes()));
		return v1.compareTo(v2); 
	}

}
