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
import java.util.HashMap;
import java.util.Iterator;

import com.google.common.collect.AbstractIterator;

import org.apache.mahout.math.CardinalityException;

public class RandomAccessSparseEncryptedVector extends EncryptedAbstractVector {

  private static final int INITIAL_CAPACITY = 11;

  private HashMap<Integer, byte[]> values;

  /** For serialization purposes only. */
  public RandomAccessSparseEncryptedVector() {
    super(0);
  }

  public RandomAccessSparseEncryptedVector(int cardinality) {
    this(cardinality, Math.min(cardinality, INITIAL_CAPACITY)); // arbitrary estimate of 'sparseness'
  }

  public RandomAccessSparseEncryptedVector(int cardinality, int initialCapacity) {
    super(cardinality);
    values = new HashMap<Integer, byte[]>(initialCapacity);
  }

  public RandomAccessSparseEncryptedVector(EncryptedVector other) {
    this(other.size(), INITIAL_CAPACITY);
    Iterator<Element> it = other.iterateNonZero();
    Element e;
    while (it.hasNext() && (e = it.next()) != null) {
      values.put(e.index(), e.get());
    }
  }

  private RandomAccessSparseEncryptedVector(int cardinality, HashMap<Integer, byte[]> values) {
    super(cardinality);
    this.values = values;
  }



  @Override
  public RandomAccessSparseEncryptedVector clone() {
    return new RandomAccessSparseEncryptedVector(size(), (HashMap<Integer, byte[]>) values.clone());
  }

  @Override
  public String toString() {
    StringBuilder result = new StringBuilder();
    result.append('{');
    Iterator<Element> it = iterateNonZero();
    boolean first = true;
    while (it.hasNext()) {
      if (first) {
        first = false;
      } else {
        result.append(',');
      }
      Element e = it.next();
      result.append(e.index());
      result.append(':');
      result.append(e.get());
    }
    result.append('}');
    return result.toString();
  }

  @Override
  public EncryptedVector assign(EncryptedVector other) {
    if (size() != other.size()) {
      throw new CardinalityException(size(), other.size());
    }
    values.clear();
    Iterator<Element> it = other.iterateNonZero();
    Element e;
    while (it.hasNext() && (e = it.next()) != null) {
      setQuick(e.index(), e.get());
    }
    return this;
  }

  /**
   * @return false
   */
  @Override
  public boolean isDense() {
    return false;
  }

  /**
   * @return false
   */
  @Override
  public boolean isSequentialAccess() {
    return false;
  }

  @Override
  public byte[] getQuick(int index) {
    return values.get(index);
  }

  @Override
  public void setQuick(int index, byte[] value) {    
    if (value == null) {    	
      values.remove(index);
    } else {
      values.put(index, value);
    }
  }


  @Override
  public RandomAccessSparseEncryptedVector like() {
    return new RandomAccessSparseEncryptedVector(size(), values.size());
  }

  /**
   * NOTE: this implementation reuses the Vector.Element instance for each call of next(). If you need to preserve the
   * instance, you need to make a copy of it
   *
   * @return an {@link Iterator} over the Elements.
   * @see #getElement(int)
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

    private final RandomAccessElement element = new RandomAccessElement();
   // private final IntArrayList indices = new IntArrayList();
    
    private final ArrayList<Integer> indices;  
    private int offset;

    private NonDefaultIterator() {
    	indices = new ArrayList<Integer>(values.keySet());  	
    }

    @Override
    protected Element computeNext() {
      if (offset >= indices.size()) {
        return endOfData();
      }
      element.index = indices.get(offset);      
      offset++;
      return element;
    }

  }

  private final class AllIterator extends AbstractIterator<Element> {

    private final RandomAccessElement element = new RandomAccessElement();

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

  private final class RandomAccessElement implements Element {

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
      if (value == null) {
        values.remove(index);
      } else {
        values.put(index, value);
      }
    }
  }
  
}
