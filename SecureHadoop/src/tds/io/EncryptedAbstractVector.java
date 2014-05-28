/*
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

import org.apache.mahout.math.CardinalityException;
import org.apache.mahout.math.IndexException;

/** Implementations of generic capabilities like sum of elements and dot products 
 * For encrypted vector
 */
public abstract class EncryptedAbstractVector implements EncryptedVector {
  
  protected int size;    

  protected EncryptedAbstractVector(int size) {
	    this.size = size;
	  }
  
  @Override
  public EncryptedVector clone() {
    try {
      EncryptedAbstractVector r = (EncryptedAbstractVector) super.clone();
      r.size = size;
      return r;
    } catch (CloneNotSupportedException e) {
      throw new IllegalStateException("Can't happen");
    }
  }
  

  @Override
  public byte[] get(int index) {
    if (index < 0 || index >= size) {
      throw new IndexException(index, size);
    }
    return getQuick(index);
  }

  @Override
  public Element getElement(int index) {
    return new LocalElement(index);
  }


  @Override
  public void set(int index, byte[] value) {
    if (index < 0 || index >= size) {
      throw new IndexException(index, size);
    }
    setQuick(index, value);
  }


  @Override
  public EncryptedVector assign(byte[] value) {
    for (int i = 0; i < size; i++) {
      setQuick(i, value);
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

  @Override
  public EncryptedVector assign(EncryptedVector other) {
    if (size != other.size()) {
      throw new CardinalityException(size, other.size());
    }
    for (int i = 0; i < size; i++) {
      setQuick(i, other.getQuick(i));
    }
    return this;
  }



  @Override
  public final int size() {
    return size;  
  }

  @Override
  public String asFormatString() {
    return toString();
  }

  /**
   * Determines whether this {@link Vector} represents the same logical vector as another
   * object. Two {@link Vector}s are equal (regardless of implementation) if the value at
   * each index is the same, and the cardinalities are the same.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof EncryptedVector)) {
      return false;
    }
    EncryptedVector that = (EncryptedVector) o;
    if (size != that.size()) {
      return false;
    }
    for (int index = 0; index < size; index++) {
      if (!getQuick(index).equals(that.getQuick(index))) {
        return false;
      }
    }
    return true;
  }

  @Override
  public String toString() {
    return toString(null);
  }

  public String toString(String[] dictionary) {
    StringBuilder result = new StringBuilder();
    result.append('{');
    for (int index = 0; index < size; index++) {
      byte[] value = getQuick(index);
      if (value != null) {
        result.append(dictionary != null && dictionary.length > index ? dictionary[index] : index);
        result.append(':');
        result.append(value);
        result.append(',');
      }
    }
    if (result.length() > 1) {
      result.setCharAt(result.length() - 1, '}');
    } else {
      result.append('}');
    }
    return result.toString();
  }


  protected final class LocalElement implements Element {
    int index;

    LocalElement(int index) {
      this.index = index;
    }

    @Override
    public byte[] get() {
      return getQuick(index);
    }

    @Override
    public int index() {
      return index;
    }

    @Override
    public void set(byte[] value) {
      setQuick(index, value);
    }
  }
}
