/******************************************************************************************************/
/*                                                                                                    */
/*    Infinidat Ltd.  -  Proprietary and Confidential Material                                        */
/*                                                                                                    */
/*    Copyright (C) 2013, Infinidat Ltd. - All Rights Reserved                                        */
/*                                                                                                    */
/*    NOTICE: All information contained herein is, and remains the property of Infinidat Ltd.         */
/*    All information contained herein is protected by trade secret or copyright law.                 */
/*    The intellectual and technical concepts contained herein are proprietary to Infinidat Ltd.,     */
/*    and may be protected by U.S. and Foreign Patents, or patents in progress.                       */
/*                                                                                                    */
/*    Redistribution or use, in source or binary forms, with or without modification,                 */
/*    are strictly forbidden unless prior written permission is obtained from Infinidat Ltd.          */
/*                                                                                                    */
/*                                                                                                    */
/******************************************************************************************************/

package org.mestor.util;

import java.util.Map.Entry;

import com.google.common.base.Objects;

/**
 * Implementation of {@link Entry Map.Entry} interface. 
 * This class is a container for pair of objects. It can be used 
 * to store the pair in collection. Since it implements {@link Entry Map.Entry}
 * there is no semantic difference between iterating over map entries or over
 * list that contains {@link Pair}s.
 * @author alexr
 *
 * @param <K> type of key
 * @param <V> type of value
 */
public class Pair<K, V> implements Entry<K, V> {
	private final K key;
	private V value;
	
	public Pair(K key, V value) {
		this.key = key;
		this.value = value;
	}

	@Override
	public K getKey() {
		return key;
	}

	@Override
	public V getValue() {
		return value;
	}

	@Override
	public V setValue(V value) {
		return this.value = value;
	}

	@Override
	public String toString() {
		return "<" + key + ":" + value + ">";
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(key, value);
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == null || !(obj instanceof Pair)) {
			return false;
		}
		@SuppressWarnings("unchecked")
		Entry<K, V> other = (Entry<K, V>)obj;
		return Objects.equal(key, other.getKey()) && Objects.equal(value, other.getValue());
	}
}
