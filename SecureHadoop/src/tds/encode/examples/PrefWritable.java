package tds.encode.examples;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.hadoop.io.WritableComparable;

import tds.io.Ciphertext;
import tds.io.EncryptedDenseVector;
import tds.io.EncryptedVectorWritable;

/**
 * @author dinhtta
 *
 * <pref> <pref/|outdegree|> 
 */
public class PrefWritable implements WritableComparable<PrefWritable>{

	public EncryptedVectorWritable pref; 		
	
	public PrefWritable(){	
		this.pref = new EncryptedVectorWritable(); 	
	}
	
	public PrefWritable(Ciphertext pr, Ciphertext prNorm){
		ArrayList<byte[]> cc = new ArrayList<byte[]>(); 
		cc.add(pr.getContent().copyBytes()); 
		cc.add(prNorm.getContent().copyBytes()); 
		this.pref = new EncryptedVectorWritable(new EncryptedDenseVector(cc)); 		
	}
	
	@Override
	public void write(DataOutput out) throws IOException {
		this.pref.write(out); 		
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		this.pref.readFields(in); 	
	}

	
	public Ciphertext getPref() {
		return new Ciphertext(pref.get().get(0));
	}

	public Ciphertext getPrefNormalized() {
		return new Ciphertext(this.pref.get().get(1));
	}

	/* Nothing to do here, as this will never be used as key
	 */
	@Override
	public int compareTo(PrefWritable arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

}
