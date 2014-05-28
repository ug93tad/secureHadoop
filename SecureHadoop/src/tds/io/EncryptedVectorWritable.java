/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tds.io;

import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.WritableComparable;
import org.apache.mahout.math.Varint;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Iterator;

/**
 * @author dinhtta
 * Vectors are written out as BytesWritable objects.
 *
 * This is a simplified version of VectorWritable. Each record:
 * <size> <BytesWritable array>
 */
public final class EncryptedVectorWritable implements WritableComparable<EncryptedVectorWritable> {
  

  private EncryptedVector vector;

  
  public void set(EncryptedVector vector) {
	this.vector = vector;
}

public EncryptedVectorWritable() {	   
  }

  public EncryptedVectorWritable(EncryptedVector vector){
	  this.vector = vector; 
  }
  

  @Override
  public void write(DataOutput out) throws IOException {
	  writeVector(out,vector); 
	 
  }

  public EncryptedVector get(){
	  return this.vector; 
  }
  
  public static EncryptedVector readVector(DataInput in) throws IOException {
	    EncryptedVectorWritable v = new EncryptedVectorWritable();
	    v.readFields(in);
	    return v.get();
  }
  
  //writing out:
  //<size> {index:BytesWritable}
  public static void writeVector(DataOutput out, EncryptedVector vector) throws IOException{
	  int size = vector.size(); 
	  Varint.writeUnsignedVarInt(size, out); 
	  Iterator<EncryptedVector.Element> iter = vector.iterateNonZero();
	  int count = 0 ;
	  while (iter.hasNext()) {
		  EncryptedVector.Element element = iter.next();
          Varint.writeUnsignedVarInt(element.index(), out);
          BytesWritable bw = new BytesWritable(element.get()); 
          bw.write(out);
          count++;
        }
  }
  
@Override
public void readFields(DataInput in) throws IOException {
	int size = Varint.readUnsignedVarInt(in); 	
	this.vector = new EncryptedDenseVector(size);
	for (int i=0; i<size; i++){
		int index = Varint.readUnsignedVarInt(in);
		BytesWritable bw = new BytesWritable();
		bw.readFields(in);	
		vector.setQuick(index, bw.copyBytes());		
	}	
	
}

@Override
//nothing here, since this is not used as a key
public int compareTo(EncryptedVectorWritable o) {
	return 0;
}
  

}
