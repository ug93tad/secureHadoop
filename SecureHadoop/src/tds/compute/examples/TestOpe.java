package tds.compute.examples;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.io.Text;

import tds.common.Rand;
import tds.hom.Ope;

public class TestOpe {
	public static void main(String[] args) throws IOException{
		String key="hello world ande";
		Ope ope =new Ope(); 
		List<String> params = new ArrayList<String>();
		params.add(key);
		params.add(10+"");
		params.add(20+"");
		ope.initPrivateParameters(params); 
		
		Rand rnd = new Rand();
		rnd.initPrivateParameters(params); 
		for (int i = 0; i < 100; i++) {
			byte[] val1 = rnd.randomBytes(10);
			byte[] val2 = rnd.randomBytes(10);
			byte[] ct1 = ope.encryptRaw(val1);
			byte[] ct2 = ope.encryptRaw(val2);
			if (new Text(val1).compareTo(new Text(val2)) != new Text(ct1)
					.compareTo(new Text(ct2)))
				System.out.println(i + ", WRONG ORDER");
			
			byte[] pt1 = ope.decryptRaw(ct1); 
			byte[] pt2 = ope.decryptRaw(ct2); 
			if (new Text(val1).compareTo(new Text(pt1))!=0 || new Text(val2).compareTo(new Text(pt2))!=0)
				System.out.println(i+", WRONG DECRYPTION"); 
			
		}
		System.out.println("done testing");
		
	}
}
