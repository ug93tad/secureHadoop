package types;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.WritableComparable;

/**
 * Basically a wrapper for BytesWritable
 */
public class Ciphertext implements WritableComparable<Ciphertext>{

	public BytesWritable content;
	public int length; 
	
	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public Ciphertext(){
		this.content = new BytesWritable();
		this.length = 0; 
	}

	public Ciphertext(byte[] ct){
		this.content = new BytesWritable(ct);
		this.length = ct.length; 
	}
	
	
	public BytesWritable getContent() {
		return content;
	}

	public void setContent(BytesWritable content) {
		this.content = content;
	}

	public Ciphertext(BytesWritable ct){
		this.content = ct; 
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
	public int compareTo(Ciphertext o) {
		// TODO Auto-generated method stub
		return this.content.compareTo(o.getContent());
	}

}
