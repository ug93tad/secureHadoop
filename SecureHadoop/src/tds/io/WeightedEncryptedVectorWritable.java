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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.Writable;

import tds.functions.IVectorFunction;
import tds.io.EncryptedVector;
import tds.io.EncryptedVectorWritable;

public class WeightedEncryptedVectorWritable implements Writable {

  private final EncryptedVectorWritable vectorWritable = new EncryptedVectorWritable();
  private double weight;

  public WeightedEncryptedVectorWritable() {
  }

  public WeightedEncryptedVectorWritable(double weight, EncryptedVector vector) {
    this.vectorWritable.set(vector);
    this.weight = weight;
  }

  public EncryptedVector getVector() {
    return vectorWritable.get();
  }

  public void setVector(EncryptedVector vector) {
    vectorWritable.set(vector);
  }

  public double getWeight() {
    return weight;
  }

  @Override
  public void readFields(DataInput in) throws IOException {
    vectorWritable.readFields(in);
    weight = in.readDouble();
  }

  @Override
  public void write(DataOutput out) throws IOException {
    vectorWritable.write(out);
    out.writeDouble(weight);
  }

  @Override
  public String toString() {
    return null; 
  }
  
  /**
   * Decrypt the vector and print out to human-readable format
   */
  public String decryptToString(IVectorFunction crypto){
	  
	  return null; 
  }
}
