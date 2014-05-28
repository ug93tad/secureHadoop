package types;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.WritableComparable;

public class LongPair implements WritableComparable<LongPair>{

	LongWritable first, second;
	public LongPair(){
		first = new LongWritable(0);
		second = new LongWritable(0); 
	}
	
	public LongPair(long first, long second){
		this.first = new LongWritable(first);
		this.second = new LongWritable(second); 
	}
	
	@Override
	public void readFields(DataInput arg0) throws IOException {
		// TODO Auto-generated method stub
		first.readFields(arg0); 
		second.readFields(arg0); 
	}

	@Override
	public void write(DataOutput arg0) throws IOException {
		// TODO Auto-generated method stub
		first.write(arg0);
		second.write(arg0); 
	}

	
	@Override
	public int compareTo(LongPair arg0) {
		int cmp = first.compareTo(arg0.getFirst()); 
		return (cmp==0)?cmp:second.compareTo(arg0.getSecond());  
	}

	public LongWritable getFirst() {
		return first;
	}

	public LongWritable getSecond() {
		return second;
	}

	@Override
	public String toString(){
		return first.get()+"\t"+second.get(); 
	}
}
