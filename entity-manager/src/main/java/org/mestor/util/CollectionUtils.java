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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

public class CollectionUtils {

	public static Map<?,?> merge(Map<?, ?> ... maps) {
		Map<Object, Object> result = null;
		for (Map<?,?> map : maps) {
			if (map != null) {
				if (result == null) {
					result = new LinkedHashMap<Object, Object>();
				}
				result.putAll(map);
			}
		}
		return result;
	}

	public static <F, T> Collection<T> nullableTransform(
			Collection<F> fromCollection,
			Function<? super F, T> function) {
		
		return fromCollection == null ? null : Collections2.transform(fromCollection, function);
	}	
	
	public static <F, T> Collection<T> nullableTransform(
			F[] fromArray,
			Function<? super F, T> function) {
	
		return fromArray == null ? null : Collections2.transform(Arrays.asList(fromArray), function);
	}
	

	@SafeVarargs
	public static <T> List<T> nullsafeAsList(T ... elements) {
		return elements == null ? null : Arrays.asList(elements);
	}
	
	
	public static <K, V> Map<K, V> subMap(Map<K,V> map, Predicate<K> keyPredicate, Function<K, K> transformer) {
		Map<K, V> result = new HashMap<>();

		for (Entry<K, V> entry : map.entrySet()) {
			if (keyPredicate.apply(entry.getKey())) {
				result.put(transformer.apply(entry.getKey()), entry.getValue());
			}
		}
		
		return result;
	}
	
	public static <V> Map<String, V> subMap(Map<String, V> map, final String keyPrefix) {
		final int prefixLength = keyPrefix.length();
		return subMap(map, new Predicate<String>() {
			@Override
			public boolean apply(String key) {
				return key != null && key.startsWith(keyPrefix);
			}
			
		}, new Function<String, String>() {
			@Override
			public String apply(String key) {
				return key.substring(prefixLength);
			}
			
		});
	}
}
