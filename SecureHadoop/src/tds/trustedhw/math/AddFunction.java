package tds.trustedhw.math;

import tds.functions.IAddFunction;
import tds.io.Ciphertext;
import tds.trustedhw.AbstractTHFunction;

public class AddFunction extends AbstractTHFunction implements IAddFunction{

	@Override
	public Ciphertext add(Ciphertext val1, Ciphertext val2) throws Exception{
		double v1 = Double.parseDouble(new String(this.decryptRaw(val1.getContent().copyBytes())));
		double v2 = Double.parseDouble(new String(this.decryptRaw(val2.getContent().copyBytes())));
		double v = v1+v2; 
		return new Ciphertext(this.encryptString(v+"")); 
	}

	@Override
	public Ciphertext subtract(Ciphertext val1, Ciphertext val2)
			throws Exception {
		double v1 = Double.parseDouble(new String(this.decryptRaw(val1.getContent().copyBytes())));
		double v2 = Double.parseDouble(new String(this.decryptRaw(val2.getContent().copyBytes())));
		double v = v1-v2; 
		return new Ciphertext(this.encryptString(v+"")); 
	}
	
}
