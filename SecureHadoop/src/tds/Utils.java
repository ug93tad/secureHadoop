package tds;

import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;

import tds.functions.IVectorFunction;
import tds.hom.Paillier;
import tds.hom.vector.HOMVector;
import tds.trustedhw.vector.THVector;

public class Utils {
	final protected static char[] hexArray = "0123456789abcdef".toCharArray();
	public static String bytesToHex(byte[] bytes) {
	    char[] hexChars = new char[bytes.length * 2];
	    int v;
	    for ( int j = 0; j < bytes.length; j++ ) {
	        v = bytes[j] & 0xFF;
	        hexChars[j * 2] = hexArray[v >>> 4];
	        hexChars[j * 2 + 1] = hexArray[v & 0x0f];
	    }
	    return new String(hexChars);
	}

	public static byte[] HexToBytes(String hex){
		byte[] bytes = new byte[hex.length()/2]; 
		for (int i=0; i<hex.length(); i=i+2){
			bytes[i/2] = (byte)(hexVal(hex.charAt(i))*16+hexVal(hex.charAt(i+1))); 
		}
		return bytes; 
	}
	
	static int hexVal(char c){
		if (c>='0' && c<='9')
			return c-'0';
		else
			return c-'a'+10; 
	}
	
	public static IVectorFunction getCrypto(Configuration conf) {
		IVectorFunction crypto;
		if (conf.getBoolean(TdsOptions.HOM_OPTION, false)) {
			int nSamples = Integer.parseInt(conf.get(TdsOptions.NUM_SAMPLES));

			crypto = new HOMVector(getPaillierPub(conf), getPaillierPriv(conf),
					getElgamalPub(conf), getElgamalPriv(conf),
					getOpeParams(conf), nSamples);
		} else {
			List<String> params = new ArrayList<String>();
			params.add(conf.get(TdsOptions.KEY_OPTION));
			crypto = new THVector();
			((ICrypto) crypto).initPrivateParameters(getSymParams(conf));
		}
		return crypto;
	}
	
	public static List<String> getPaillierPub(Configuration conf){
		List<String> params = new ArrayList<String>();
		params.add(conf.get(TdsOptions.PAILLIER_PUBLIC_KEY_FILE));
		return params;
	}
	
	public static List<String> getPaillierPriv(Configuration conf){
		List<String> params = new ArrayList<String>();
		params.add(conf.get(TdsOptions.PAILLIER_PRIVATE_KEY_FILE));
		return params;
	}
	
	public static List<String> getElgamalPub(Configuration conf){
		List<String> params = new ArrayList<String>();
		params.add(conf.get(TdsOptions.ELGAMAL_PUBLIC_KEY_FILE));
		return params;
	}
	
	public static List<String> getElgamalPriv(Configuration conf){
		List<String> params = new ArrayList<String>();
		params.add(conf.get(TdsOptions.ELGAMAL_PRIVATE_KEY_FILE));
		return params;
	}
	
	public static List<String> getSymParams(Configuration conf){
		List<String> params = new ArrayList<String>();
		params.add(conf.get(TdsOptions.KEY_OPTION));
		params.add(conf.get(TdsOptions.IV_OPTION)); 
		return params;
	}
	public static List<String> getOpeParams(Configuration conf){
		List<String> params = new ArrayList<String>();
		params.add(conf.get(TdsOptions.KEY_OPTION));
		params.add(conf.get(TdsOptions.P_LEN));
		params.add(conf.get(TdsOptions.CT_LEN));
		return params;
	}
	
	public static byte[] reverseOrder(byte[] val){
		int n = val.length;
		byte[] result = new byte[n]; 
		for (int i=0; i<n; i++)
			result[i] = val[n-(i+1)]; 
		return result; 
	}
}
