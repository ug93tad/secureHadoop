package tds;

import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;

import tds.functions.IVectorFunction;
import tds.hom.vector.HOMVector;
import tds.trustedhw.vector.THVector;

public class TdsOptions {
	public static String HOM_OPTION="crypto.hom"; 
	public static String KEY_OPTION="key";	
	public static String IV_OPTION="iv"; //for fixed IV
	public static final String ELGAMAL_PUBLIC_KEY_FILE = "elgamal.pub.key";
	public static final String ELGAMAL_PRIVATE_KEY_FILE = "elgamal.priv.key";
	public static final String PAILLIER_PUBLIC_KEY_FILE = "paillier.pub.key";
	public static final String PAILLIER_PRIVATE_KEY_FILE = "paillier.priv.key"; 
	
	public static int AES_BLOCK_SIZE=16;
	public static int IV_LENGTH = 16;
	
	//terasort options
	public static final String P_LEN = "ope.plaintext.length";
	public static final String CT_LEN = "ope.ciphertext.length";
	public static final int TERA_KEY_LEN = 10; 
	
	//pagerank
	public static String NUM_NODES = "num.nodes";
	
	//kmeans
	public static final String NUM_SAMPLES="kmeans.nSamples"; 
	
	public static enum HEEDOOP_COUNTER{
		RND, RND_TO_OPE, SWITCH, SWITCH_TO_OPE, SWITCH_TO_PAL, SWITCH_TO_ELG
	}
}
