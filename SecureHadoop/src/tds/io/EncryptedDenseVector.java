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

import java.util.ArrayList;

import java.util.Iterator;

import com.google.common.collect.AbstractIterator;

import org.apache.mahout.math.CardinalityException;

/** Implements vector as an array of doubles */
public class EncryptedDenseVector extends EncryptedAbstractVector {

	
  private ArrayList<byte[]> values;

  /** For serialization purposes only */
  public EncryptedDenseVector() {
    super(0);
  }

  /** Construct a new instance using provided values */
  public EncryptedDenseVector(ArrayList<byte[]> values) {
    this(values, false);
  }

  public EncryptedDenseVector(ArrayList<byte[]> values, boolean shallowCopy) {
    super(values.size());    
    this.values = shallowCopy ? values : (ArrayList<byte[]>)values.clone();
  }

  public EncryptedDenseVector(EncryptedDenseVector values, boolean shallowCopy) {
    this(values.values, shallowCopy);
  }

  /** Construct a new instance of the given cardinality */
  public EncryptedDenseVector(int cardinality) {
    super(cardinality);
    this.values = new ArrayList<byte[]>(cardinality);
    for (int i=0; i<cardinality; i++)
    	this.values.add(null);  
  }

  /**
   * Copy-constructor (for use in turning a sparse vector into a dense one, for example)
   * @param vector
   */
  public EncryptedDenseVector(EncryptedVector vector) {
    super(vector.size());
    values = new ArrayList<byte[]>(vector.size()); 
    Iterator<Element> it = vector.iterateNonZero();
    while (it.hasNext()) {
      Element e = it.next();
      values.add(e.index(), e.get());       
    }
  }


  @Override
  public EncryptedDenseVector clone() {
    return new EncryptedDenseVector((ArrayList<byte[]>)values.clone());
  }

  /**
   * @return true
   */
  @Override
  public boolean isDense() {
    return true;
  }

  /**
   * @return true
   */
  @Override
  public boolean isSequentialAccess() {
    return true;
  }
  

  @Override
  public byte[] getQuick(int index) {
    return values.get(index);
  }

  @Override
  public EncryptedDenseVector like() {
    return new EncryptedDenseVector(size());
  }

  @Override
  public void setQuick(int index, byte[] value) {   
	  values.set(index, value);     
  }
  
  @Override
  public EncryptedVector assign(byte[] value) {
	  for (int i=0; i<values.size(); i++){
		  values.set(i, value); 
	  }    
    return this;
  }
  
  @Override
  public EncryptedVector assign(byte[][] values) {
    if (size != values.length) {
      throw new CardinalityException(size, values.length);
    }
    for (int i = 0; i < size; i++) {
      setQuick(i, values[i]);
    }
    return this;
  }
   

  public EncryptedVector assign(EncryptedDenseVector vector) {
		if (size() != vector.size()) {
			throw new CardinalityException(size(), vector.size());
		}
		values.clear();
		Iterator<Element> it = vector.iterateNonZero();
		Element e;
		while (it.hasNext() && (e = it.next()) != null) {
			setQuick(e.index(), e.get());
		}
		return this;
  }


  /**
   * Returns an iterator that traverses this Vector from 0 to cardinality-1, in that order.
   */
  @Override
  public Iterator<Element> iterateNonZero() {
    return new NonDefaultIterator();
  }

  @Override
  public Iterator<Element> iterator() {
    return new AllIterator();
  }
  
  

  private final class NonDefaultIterator extends AbstractIterator<Element> {

    private final DenseElement element = new DenseElement();
    private int index = 0;

    @Override
    protected Element computeNext() {    	
     /* while (index < size() && values.get(index)==null) {    	   
        index++;
      }*/
      if (index < size()) {
        element.index = index;
        index++;
        return element;
      } else {
        return endOfData();
      }
    }

  }

  private final class AllIterator extends AbstractIterator<Element> {

    private final DenseElement element = new DenseElement();

    private AllIterator() {
      element.index = -1;
    }

    @Override
    protected Element computeNext() {
      if (element.index + 1 < size()) {
        element.index++;
        return element;
      } else {
        return endOfData();
      }
    }

  }

  private final class DenseElement implements Element {

    int index;

    @Override
    public byte[] get() {
      return values.get(index); 
    }

    @Override
    public int index() {
      return index;
    }

    @Override
    public void set(byte[] value) {
    	values.set(index, value);       
    }
  }

}
