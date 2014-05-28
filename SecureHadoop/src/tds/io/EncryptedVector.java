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


import java.util.Iterator;

/**
 * Encrypted version of org.apache.mahout.math.Vector which only deals with double value
 * 
 * In this case, Element is of type byte array (BytesWritable).
 * 
 * Removed all the vector operations. Store data only
 */
public interface EncryptedVector extends Cloneable, Iterable<EncryptedVector.Element> {

  /** @return a formatted String suitable for output */
  String asFormatString();

  /**
   * Assign the value to all elements of the receiver
   *
   * @param a byte array
   * @return the modified receiver
   */
  EncryptedVector assign(byte[] value);

  /**
   * Assign the values to the receiver
   *
   */
  EncryptedVector assign(byte[][] values);

  /**
   * Assign the other vector values to the receiver
   *
   */
  EncryptedVector assign(EncryptedVector other);
   

  /**
   * Return the cardinality of the recipient (the maximum number of values)
   *
   * @return an int
   */
  int size();

  /**
   * @return true iff this implementation should be considered dense -- that it explicitly
   *  represents every value
   */
  boolean isDense();

  /**
   * @return true iff this implementation should be considered to be iterable in index order in an efficient way.
   *  In particular this implies that {@link #iterator()} and {@link #iterateNonZero()} return elements
   *  in ascending order by index.
   */
  boolean isSequentialAccess();

  /**
   * Return a copy of the recipient
   *
   * @return a new Vector
   */
  EncryptedVector clone();

  /**
   * Iterates over all elements <p/> * NOTE: Implementations may choose to reuse the Element returned for performance
   * reasons, so if you need a copy of it, you should call {@link #getElement(int)} for the given index
   *
   * @return An {@link Iterator} over all elements
   */
  @Override
  Iterator<Element> iterator();

  /**
   * Iterates over all non-zero elements. <p/> NOTE: Implementations may choose to reuse the Element returned for
   * performance reasons, so if you need a copy of it, you should call {@link #getElement(int)} for the given index
   *
   * @return An {@link Iterator} over all non-zero elements
   */
  Iterator<Element> iterateNonZero();

  /**
   * Return an object of Vector.Element representing an element of this Vector. Useful when designing new iterator
   * types.
   *
   * @param index Index of the Vector.Element required
   * @return The Vector.Element Object
   */
  Element getElement(int index);

  /**
   * A holder for information about a specific item in the Vector. <p/> When using with an Iterator, the implementation
   * may choose to reuse this element, so you may need to make a copy if you want to keep it
   */
  interface Element {

    /** @return the value of this vector element. */
    byte[] get();

    /** @return the index of this vector element. */
    int index();

    /** @param value Set the current element to value. */
    void set(byte[] value);
  }


  /**
   * Return the value at the given index
   */
  byte[] get(int index);

  /**
   * Return the value at the given index, without checking bounds
   */
  byte[] getQuick(int index);

  /**
   * Return an empty vector of the same underlying class as the receiver
   *
   * @return a Vector
   */
  EncryptedVector like();

 

  /**
   * Set the value at the given index
   */
  void set(int index, byte[] value);

  /**
   * Set the value at the given index, without checking bounds
   */
  void setQuick(int index, byte[] value);
 
}
