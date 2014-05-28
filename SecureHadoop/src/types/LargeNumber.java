package types;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;

/**
 * Basically string representing the decrypted ciphertext
 */
public class LargeNumber implements WritableComparable<LargeNumber>{

	public Text content;
	

	public LargeNumber(){
		this.content = new Text(); 		 
	}

	public LargeNumber(String s){
		this.content = new Text(s);  
	}
		
	
	public Text getContent() {
		return content;
	}

	@Override
	public void write(DataOutput out) throws IOException {
		// TODO Auto-generated method stub
		this.content.write(out);
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		// TODO Auto-generated method stub
		this.content.readFields(in);
	}

	@Override
	public int compareTo(LargeNumber o) {
		String local = this.content.toString(); 
		String other = o.getContent().toString(); 
		if (local.length()!=other.length()){
			return local.length() - other.length(); 
		}
		else{
			for (int i=0; i<local.length(); i++){
				if (local.charAt(i)!=other.charAt(i))
					return local.charAt(i) - other.charAt(i); 
			}
		}
		return 0; 
	}

}
