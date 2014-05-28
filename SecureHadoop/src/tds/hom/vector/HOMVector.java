package tds.hom.vector;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.log4j.Logger;
import org.apache.mahout.clustering.AbstractCluster;
import org.apache.mahout.math.Vector;

import tds.CryptoOperationNotSupportedException;
import tds.TdsOptions;
import tds.functions.IVectorFunction;
import tds.hom.Elgamal;
import tds.hom.Ope;
import tds.hom.Paillier;
import tds.hom.switchable.Switchable;
import tds.io.Ciphertext;
import tds.io.EncryptedVector;

/**
 * Support vector operations over ciphertext.
 * 
 * Assume that original format is Paillier ciphertext
 *
 */
public class HOMVector implements IVectorFunction{

	Switchable switchable;	
	Elgamal elgamal;
	Paillier paillier; 	
	Ope ope; 
	long scaleFactor; // = N, to scale up when divide
	long COUNT_SWITCH=0; 
	long COUNT_SWITCH_TO_ELG=0;
	long COUNT_SWITCH_TO_PAL=0;
	long COUNT_SWITCH_TO_OPE=0; 
	
	public HOMVector(List<String> paillierPub, List<String> paillierPriv,
			List<String> elgamalPub, List<String> elgamalPriv,
			List<String> opePriv, long scaleFactor) {
		this.paillier = new Paillier();
		this.elgamal = new Elgamal();
		this.ope = new Ope();
		this.paillier.initPublicParameters(paillierPub); 
		this.paillier.initPrivateParameters(paillierPriv);
		this.elgamal.initPublicParameters(elgamalPub); 
		this.elgamal.initPrivateParameters(elgamalPriv); 
		this.ope.initPrivateParameters(opePriv); 
		
		this.switchable = new Switchable(paillier, elgamal, ope); 
		this.scaleFactor = scaleFactor; 				
	}
			
	
	/* new centroid is also Elgamal. input is Paillier
	 */
	@Override
	public EncryptedVector multiply(EncryptedVector vector, double val) throws Exception {
		//first, encrypt the val		
		Ciphertext factor = new Ciphertext(this.elgamal.encryptString((int)val+""));
		
		Iterator<EncryptedVector.Element> iter = vector.iterateNonZero();
		EncryptedVector newVec = vector.like();
		while (iter.hasNext()){
			EncryptedVector.Element el = iter.next();			
			Ciphertext vals = new Ciphertext(el.get()); 				
			byte[] prod = this.elgamal.product(vals,factor).getContent().copyBytes();						
			newVec.setQuick(el.index(), prod); 
		}			
		return newVec;
	}

	@Override
	public EncryptedVector sum(EncryptedVector v1, EncryptedVector v2) {
		Iterator<EncryptedVector.Element> iter = v1.iterateNonZero();
		EncryptedVector newVec = v1.like();
		while (iter.hasNext()){
			EncryptedVector.Element el = iter.next();			
			//byte[] val1 = this.switchable.mulToAdd(el.get());
			//byte[] val2 = this.switchable.mulToAdd(v2.getQuick(el.index()));
			Ciphertext val1 = new Ciphertext(el.get()); 
			Ciphertext val2 = new Ciphertext(v2.getQuick(el.index())); 
			newVec.setQuick(el.index(), this.paillier.add(val1, val2)
					.getContent().copyBytes()); 
		}		
		return newVec;				
	}

	//v1 is Paillier, v2 is Elgamal
	public EncryptedVector convertAndSum(EncryptedVector v1, EncryptedVector v2) {
		Iterator<EncryptedVector.Element> iter = v1.iterateNonZero();
		EncryptedVector newVec = v1.like();
		while (iter.hasNext()){
			EncryptedVector.Element el = iter.next();			
		
			Ciphertext val1 = new Ciphertext(el.get());
			Ciphertext val2 = new Ciphertext(v2.getQuick(el.index())); 
			val2 = this.switchable.mulToAdd(val2);
			COUNT_SWITCH++; 
			COUNT_SWITCH_TO_PAL++; 
			
			newVec.setQuick(el.index(), this.paillier.add(val1, val2)
					.getContent().copyBytes()); 
		}				
		return newVec;				
	}
	
	@Override
	//have to convert factor to multiplicative: x -> N/x. Resulting centroid is Elgamal
	public EncryptedVector divideAndScale(EncryptedVector vector, double factor) throws Exception {	
		vector = this.switchToElgamalr(vector); 
		EncryptedVector res = this.multiply(vector, this.scaleFactor/factor); 
		return res;//this.switchToPaillier(res); 
	}
	
	/* Both v1 and v2 are Elgamal ciphertext
	 */
	@Override
	public Ciphertext squaredDistance(EncryptedVector v1,
			EncryptedVector v2) throws Exception {		
		Ciphertext sl1 = squaredLength(v1); //new Ciphertext(this.squaredLength(v1)); 
		Ciphertext sl2 = squaredLength(v2); //new Ciphertext(this.squaredLength(v2));
				 
		
		Ciphertext sl = this.paillier.add(sl1, sl2); 
		
		Ciphertext sr =  this.paillier.subtract(sl,this.dot(v1, v2)); 		
		return sr; 
	}

	/* v1, v2 are Elgamal ciphertexts
	 */
	@Override
	public Ciphertext dot(EncryptedVector v1, EncryptedVector v2) throws IOException {
		Iterator<EncryptedVector.Element> iter = v1.iterateNonZero();
		Ciphertext dotProd = null; 
		Ciphertext two = new Ciphertext(this.elgamal.encryptString("2"));
		boolean firstTime = true; 
		
		while (iter.hasNext()){
			EncryptedVector.Element el = iter.next();			
			//Ciphertext val1 = new Ciphertext(this.switchable.addToMul(el.get()));
			Ciphertext val1 = new Ciphertext(el.get()); 
			Ciphertext val2 = new Ciphertext(v2.getQuick(el.index())); 						 
			Ciphertext prod = this.elgamal.product(val1, val2); 
			prod = this.elgamal.product(prod, two); 
			if (firstTime){
				dotProd = this.switchable.mulToAdd(prod); 
				COUNT_SWITCH++; 
				COUNT_SWITCH_TO_PAL++; 
				//dotProd = new Ciphertext(this.switchable.mulToAdd(prod.getContent().copyBytes())); 
				firstTime =false; 
			}
			else{
				dotProd = this.paillier.add(
						dotProd, this.switchable.mulToAdd(prod)); 
				COUNT_SWITCH++; 
				COUNT_SWITCH_TO_PAL++; 
				
						//new Ciphertext(this.switchable.mulToAdd(prod.getContent().copyBytes())));				
			}			 									 
		}		
		return dotProd;
	}

	/* vector is Elgamal
	 */
	@Override
	public Ciphertext squaredLength(EncryptedVector vector) {
		Iterator<EncryptedVector.Element> iter = vector.iterateNonZero();
		Ciphertext sl = null; 
		boolean firstTime = true; 
		while (iter.hasNext()){
			EncryptedVector.Element el = iter.next();
			//Ciphertext val = new Ciphertext(this.switchable.addToMul(el.get())); 
			Ciphertext val = new Ciphertext(el.get()); 
			Ciphertext valS = this.switchable.mulToAdd(this.elgamal.product(val, val));  
			COUNT_SWITCH++; 
			COUNT_SWITCH_TO_PAL++; 
					//new Ciphertext(this.switchable.mulToAdd(this.elgamal.product(val, val).getContent().copyBytes()));
			
			if (firstTime){
				sl = valS; 
				firstTime = false; 
			}
			else{
				sl = this.paillier.add(sl, valS); 
			}
		}		
		return sl; 
	}

	@Override
	public int maxValueIndex(EncryptedVector vector) throws Exception {
		Ciphertext minVal = null;  
		Iterator<EncryptedVector.Element> iter = vector.iterateNonZero();		 
		boolean firstTime = true; 
		int idx=0; 		
		while (iter.hasNext()){
			EncryptedVector.Element el = iter.next();
			Ciphertext val = this.switchable.addToOpe(new Ciphertext(el.get())); 
			COUNT_SWITCH++; 
			COUNT_SWITCH_TO_OPE++; 
			//Ciphertext val = new Ciphertext(this.switchable.addToOpe(el.get())); 			
			if (firstTime){
				minVal = val; 
				firstTime = false;
				idx = el.index(); 				
			}
			else{								
				if (ope.compare(minVal, val)>0){
					minVal = val;
					idx = el.index(); 
				}
			}
		}
		return idx;
	}

	@Override
	public void init(EncryptedVector vector, int val) throws Exception {
		Iterator<EncryptedVector.Element> iter = vector.iterateNonZero();
		byte[] ct = this.paillier.encryptString(val+""); 
		while (iter.hasNext()){
			EncryptedVector.Element el = iter.next();
			vector.setQuick(el.index(), ct);  
		}
		
	}


	@Override
	public EncryptedVector scaleUp(EncryptedVector vector) throws Exception {
		return multiply(vector, this.scaleFactor); 
	}


	@Override
	public String decryptToString(EncryptedVector vector) throws Exception {
		double weight=0;
		StringBuilder buf = new StringBuilder();
		buf.append(weight+": "); 
		int n = vector.size(); 
		for (int i=0; i<n; i++){			
			buf.append(this.elgamal.decryptToText(vector.getQuick(i))+ " "); 
		}		 		 
		return buf.toString(); 
	}	
			
	public EncryptedVector switchToElgamalr(EncryptedVector vector){
		Iterator<EncryptedVector.Element> iter = vector.iterateNonZero();		 
		EncryptedVector newVec = vector.like();
		
		while (iter.hasNext()){
			EncryptedVector.Element el = iter.next();			
		
			Ciphertext val1 = new Ciphertext(el.get());			 
			val1 = this.switchable.addToMul(val1);
			COUNT_SWITCH++; 
			COUNT_SWITCH_TO_ELG++; 
			//Ciphertext val2 = new Ciphertext(this.switchable.mulToAdd(v2.getQuick(el.index()))); 
			newVec.setQuick(el.index(), val1.getContent().copyBytes()); 
		}				
		return newVec;		
		
	}
	
	public String decryptPaillier(byte[] val) throws IOException{
		return this.paillier.decryptToText(val); 
	}
	public String decryptToStringPaillier(EncryptedVector vector) throws Exception {
		double weight=0;
		StringBuilder buf = new StringBuilder();
		buf.append(weight+": "); 
		int n = vector.size(); 
		for (int i=0; i<n; i++){			
			buf.append(this.paillier.decryptToText(vector.getQuick(i))+ " "); 
		}		 		 
		return buf.toString(); 
	}

	public void resetCounter(){
		System.out.println("COUNT SWTICH = "+COUNT_SWITCH);
		System.out.println("COUNT OPE SWITCH = "+COUNT_SWITCH_TO_OPE);
		System.out.println("COUNT PAL SWITCH = "+COUNT_SWITCH_TO_PAL);
		System.out.println("COUNT ELG SWITCH = "+COUNT_SWITCH_TO_ELG);
		COUNT_SWITCH=0; 
		
	}
}
