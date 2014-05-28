package tds.trustedhw.math;

import java.io.IOException;
import java.util.regex.Pattern;

import tds.functions.IEqualityFunction;
import tds.hom.SearchToken;
import tds.io.Ciphertext;
import tds.trustedhw.AbstractTHFunction;

public class EqualityFunction extends AbstractTHFunction implements IEqualityFunction{

	@Override
	public boolean isAMatch(Ciphertext ciphertext, Ciphertext token) throws IOException
			 {
		String text =this.decryptToText(ciphertext.getContent().copyBytes()); 
		String exp = this.decryptToText(token.getContent().copyBytes());
		return Pattern.matches(exp, text); 
	}
}
