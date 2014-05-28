package types.pagerank;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.WritableComparable;

import types.Ciphertext;
import types.ElgamalCiphertext;
import types.PaillierCiphertext;

/**
 * @author dinhtta
 * 
 * <pagerank> <pagerank/|outdegree|> <0.85*N*N> <0.85*N*N/|outdegree|>
 *
 */
public class PRWritable implements WritableComparable<PRWritable>{

	public PaillierCiphertext pagerank, pagerankNormalized;
	public ElgamalCiphertext pagerankConst, pagerankConstNormalized; 
	
	public PRWritable(){
		this.pagerank = new PaillierCiphertext();
		this.pagerankNormalized = new PaillierCiphertext(); 
		this.pagerankConst = new ElgamalCiphertext();
		this.pagerankConstNormalized = new ElgamalCiphertext(); 		
	}
	
	public PRWritable(byte[] pr, byte[] prNorm, byte[] prConst, byte[] prConstNorm){
		this.pagerank = new PaillierCiphertext(pr);
		this.pagerankNormalized = new PaillierCiphertext(prNorm); 
		this.pagerankConst = new ElgamalCiphertext(prConst);
		this.pagerankConstNormalized = new ElgamalCiphertext(prConstNorm);
	}
	
	@Override
	public void write(DataOutput out) throws IOException {
		this.pagerank.write(out); 
		this.pagerankNormalized.write(out); 
		this.pagerankConst.write(out); 
		this.pagerankConstNormalized.write(out); 
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		this.pagerank.readFields(in); 
		this.pagerankNormalized.readFields(in); 
		this.pagerankConst.readFields(in); 
		this.pagerankConstNormalized.readFields(in); 
	}

	
	public PaillierCiphertext getPagerank() {
		return pagerank;
	}

	public PaillierCiphertext getPagerankNormalized() {
		return pagerankNormalized;
	}

	public ElgamalCiphertext getPagerankConst() {
		return pagerankConst;
	}

	public ElgamalCiphertext getPagerankConstNormalized() {
		return pagerankConstNormalized;
	}

	/* Nothing to do here, as this will never be used as key
	 */
	@Override
	public int compareTo(PRWritable arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

}
