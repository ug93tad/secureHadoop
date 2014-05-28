package tds.trustedhw.transform;

import tds.common.Det;
import tds.io.Ciphertext;
import tds.trustedhw.AbstractTHFunction;

/**
 * Transforming RND to DET
 *
 */
public class TransformFunction extends AbstractTHFunction{
	
	public Ciphertext transform(Ciphertext ciphertext, Det det) throws Exception{
		byte[] ct = this.decryptRaw(ciphertext.getContent().copyBytes()); 		
		return new Ciphertext(det.encryptRaw(ct)); 
	}
}
