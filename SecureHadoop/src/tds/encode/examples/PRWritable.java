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
 */
public class PRWritable implements WritableComparable<PRWritable>{

	//this vector contains pagerank, pagerankNormalized values. 
	public EncryptedVectorWritable prVector; 
		
	
	public PRWritable(){
		this.prVector = new EncryptedVectorWritable(); 					
	}
	
	public PRWritable(Ciphertext pr, Ciphertext prNorm, Ciphertext prConstNorm){		
		ArrayList<byte[]> cc = new ArrayList<byte[]>();
		cc.add(pr.getContent().copyBytes());
		cc.add(prNorm.getContent().copyBytes());
		cc.add(prConstNorm.getContent().copyBytes());
		this.prVector = new EncryptedVectorWritable(new EncryptedDenseVector(cc)); 		
	}
	
	@Override
	public void write(DataOutput out) throws IOException {
		this.prVector.write(out); 		 
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		this.prVector.readFields(in); 		 
	}

	
	public Ciphertext getPagerank() {
		return new Ciphertext(this.prVector.get().get(0)); 		
	}

	public Ciphertext getPagerankNormalized() {
		return new Ciphertext(this.prVector.get().get(1)); 
	}	

	public Ciphertext getPagerankConstNormalized(){
		return new Ciphertext(this.prVector.get().get(2)); 
	}
	
	/* Nothing to do here, as this will never be used as key
	 */
	@Override
	public int compareTo(PRWritable arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

}
