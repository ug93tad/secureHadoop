package tds.functions;

import org.apache.hadoop.mapreduce.TaskAttemptContext;

import tds.io.Ciphertext;
import tds.io.EncryptedVector;


public interface IVectorFunction {
	//scalar multiplication
	public EncryptedVector multiply(EncryptedVector vector, double val) throws Exception;
	
	//scale up to factor of N
	public EncryptedVector scaleUp(EncryptedVector vector) throws Exception; 
	
	//addition
	public EncryptedVector sum(EncryptedVector v1, EncryptedVector v2) throws Exception;
	
	//division
	public EncryptedVector divideAndScale(EncryptedVector vector, double factor) throws Exception; 
	
	//squared Euclidean distance
	public Ciphertext squaredDistance(EncryptedVector v1, EncryptedVector v2) throws Exception; 
	
	//dot product
	public Ciphertext dot(EncryptedVector v1, EncryptedVector v2) throws Exception; 
	
	//x1_2 + x_2^2 + .. + x_n^2
	public Ciphertext squaredLength(EncryptedVector vector) throws Exception;
	
	//max's index
	public int maxValueIndex(EncryptedVector vector) throws Exception; 
	
	//initialize vector's elements to val
	public void init(EncryptedVector vector, int val) throws Exception;
	
	//decrypt the vector and print to string
	public String decryptToString(EncryptedVector vector) throws Exception; 
	
	public void resetCounter(); 
}
