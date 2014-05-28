package types.pagerank;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.WritableComparable;

import tds.io.EncryptedDenseVector;
import types.Ciphertext;
import types.ElgamalCiphertext;
import types.PaillierCiphertext;

/**
 * @author dinhtta
 *
 * <pref> <pref/|outdegree|> 
 */
public class PrefWritable implements WritableComparable<PrefWritable>{

	public PaillierCiphertext pref, prefNormalized;	
	
	public PrefWritable(){	
		this.pref = new PaillierCiphertext();
		this.prefNormalized = new PaillierCiphertext(); 	
	}
	
	public PrefWritable(byte[] pr, byte[] prNorm){
		this.pref = new PaillierCiphertext(pr);
		this.prefNormalized = new PaillierCiphertext(prNorm); 		
	}
	
	@Override
	public void write(DataOutput out) throws IOException {
		this.pref.write(out); 
		this.prefNormalized.write(out); 
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		this.pref.readFields(in); 
		this.prefNormalized.readFields(in); 
	}

	
	public PaillierCiphertext getPref() {
		return pref;
	}

	public PaillierCiphertext getPrefNormalized() {
		return prefNormalized;
	}

	/* Nothing to do here, as this will never be used as key
	 */
	@Override
	public int compareTo(PrefWritable arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

}
