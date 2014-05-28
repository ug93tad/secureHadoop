package tds.trustedhw.vector;

import java.util.Iterator;

import org.apache.hadoop.mapreduce.TaskAttemptContext;


import tds.TdsOptions;
import tds.functions.IVectorFunction;
import tds.io.Ciphertext;
import tds.io.EncryptedVector;
import tds.trustedhw.AbstractTHFunction;

/**
 * Support vector operations over AES-encrypted ciphertext.
 * 
 * This is used for the ideal-case (trusted hardware) implementation
 *
 */
public class THVector extends AbstractTHFunction implements IVectorFunction{
		
	long COUNT_RND; 
	
	@Override
	public EncryptedVector multiply(EncryptedVector vector, double val) throws Exception {
		
		Iterator<EncryptedVector.Element> iter = vector.iterateNonZero();
		EncryptedVector newVec = vector.like();
		String encryptedVal; 
		while (iter.hasNext()){
			EncryptedVector.Element el = iter.next();			
			byte[] vals = this.decryptRaw(el.get());
			
			encryptedVal = Double.parseDouble(new String(vals))*val + ""; 
			newVec.setQuick(el.index(), this.encryptString(encryptedVal)); 			
		}			
		COUNT_RND++;  
		return newVec;
	}

	@Override
	public EncryptedVector sum(EncryptedVector v1, EncryptedVector v2) throws Exception {
		Iterator<EncryptedVector.Element> iter = v1.iterateNonZero();
		EncryptedVector newVec = v1.like();
		int idx; 
		while (iter.hasNext()){
			EncryptedVector.Element el = iter.next();			
			//byte[] val1 = this.switchable.mulToAdd(el.get());
			//byte[] val2 = this.switchable.mulToAdd(v2.getQuick(el.index()));
			//byte[] val1 = el.get(); 
			double val1 = Double.parseDouble(new String(this.decryptRaw(el
					.get()))); 
			//byte[] val2 = v2.getQuick(el.index());
			double val2 = Double.parseDouble(new String(this.decryptRaw(v2
					.getQuick(el.index()))));
			String encryptedVal = (val1+val2)+""; 
			newVec.setQuick(el.index(), this.encryptString(encryptedVal)); 
		}		
		COUNT_RND++; 
		return newVec;				
	}

	@Override
	//have to convert factor to multiplicative: x -> N/x
	public EncryptedVector divideAndScale(EncryptedVector vector, double factor) throws Exception {	
		return this.multiply(vector, 1/factor); 
	}

	@Override
	public Ciphertext squaredDistance(EncryptedVector v1,
			EncryptedVector v2) throws Exception {
		Iterator<EncryptedVector.Element> iter = v1.iterateNonZero();
		double ss = 0; 
		while (iter.hasNext()){
			EncryptedVector.Element el = iter.next(); 
			double val1 = Double.parseDouble(new String(this.decryptRaw(v1.get(el.index()))));
			double val2 = Double.parseDouble(new String(this.decryptRaw(v2.get(el.index()))));
			ss += (val1-val2)*(val1-val2);
		}			
		COUNT_RND++; 
		return new Ciphertext(this.encryptString(ss+"")); 		
	}

	@Override
	public Ciphertext dot(EncryptedVector v1, EncryptedVector v2) {
		return null; 
	}

	@Override
	public Ciphertext squaredLength(EncryptedVector vector) {
		return null; 
	}

	@Override
	public int maxValueIndex(EncryptedVector vector) throws Exception {		 
		Iterator<EncryptedVector.Element> iter = vector.iterateNonZero(); 		
		double min = Double.MAX_VALUE; 
		int idx=-1; 
		while (iter.hasNext()){
			EncryptedVector.Element el = iter.next(); 
			double val = Double.parseDouble(new String(this.decryptRaw(vector.get(el.index()))));
			if (val<min){
				min = val; 
				idx = el.index(); 
			}
		}
		COUNT_RND++; 
		return idx; 
	}

	@Override
	public void init(EncryptedVector vector, int val) throws Exception {
		Iterator<EncryptedVector.Element> iter = vector.iterateNonZero();
		byte[] ct = this.encryptString(val+"");  
		while (iter.hasNext()){
			EncryptedVector.Element el = iter.next();
			vector.setQuick(el.index(), ct);  
		}
		
	}


	@Override
	public EncryptedVector scaleUp(EncryptedVector vector) {		
		return vector; 		 
	}


	@Override
	public String decryptToString(EncryptedVector vector) throws Exception {
		double weight=0;
		StringBuilder buf = new StringBuilder();
		buf.append(weight+": "); 
		int n = vector.size(); 
		for (int i=0; i<n; i++){				
			buf.append(new String(this.decryptRaw(vector.getQuick(i)))+ " "); 
		}	
		COUNT_RND++;  
		return buf.toString(); 
	}

	@Override
	public void resetCounter() {
		System.out.println("COUNT RND = "+COUNT_RND); 
		COUNT_RND=0; 		
	}
			
	
}
