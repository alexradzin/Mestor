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

package org.mestor.persistence.cql.management;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.cassandra.cql3.CQL3Type.Native;
import org.apache.cassandra.db.marshal.AbstractType;
import org.codehaus.jackson.map.ObjectMapper;

import com.google.common.base.Joiner;

public class CommandHelper {
	private final static Map<Class<?>, AbstractType<?>> cassandraTypes = new HashMap<>();
	static {
		initTypeMapping();
	}
	
	
	private static void initTypeMapping() {
		for (Native nativeType : Native.values()) {
			AbstractType<?> cassandraType = nativeType.getType();
			System.out.println(cassandraType);
			
			Class<?> type = null;
			for (type = cassandraType.getClass(); type != null && !AbstractType.class.equals(type.getSuperclass()); type = type.getSuperclass()) {
				// empty loop body. This loop is looking for the direct subclass of AbstractType,
				// so the logic is coded into the loop header
			}
			@SuppressWarnings("null") // if type is null here NullPointerException will be thrown. 
			Type abstractType = type.getGenericSuperclass(); 
			
			Class<?> clazz = (Class<?>)((ParameterizedType)abstractType).getActualTypeArguments()[0];
			cassandraTypes.put(clazz, cassandraType);
		}
	}
	
	
	static String fullname(String keyspace, String name) {
		return keyspace == null ? name : keyspace + "." + name;
	}
	
	
	static String createQueryString(String[] tokens, Map<String, Object> with) {
		StringBuilder buf = new StringBuilder();
		Joiner.on(" ").skipNulls().appendTo(buf, tokens);
		if (with != null && !with.isEmpty()) {
			buf.append(" ").append("WITH").append(" ");
			int n = with.size();
			int i = 0;
			for (Entry<String, Object> prop : with.entrySet()) {
				buf.append(" ").append(prop.getKey()).append("=").append(formatValue(prop.getValue()));
				if (i < n - 1) {
					buf.append(" AND ");
				}
				i++;
			}
		}
		return buf.toString();
	}

	private static String formatValue(Object value) {
		if (value == null) {
			return null;
		}
		Class<?> type = value.getClass();
		if (CharSequence.class.isAssignableFrom(type)) {
			return "'" + value + "'";
		}
		if (Number.class.isAssignableFrom(type)) {
			return value.toString();
		}
		if (Map.class.isAssignableFrom(type)) {
			Map<?,?> mapvalue = (Map<?,?>)value;
			return formatValue(mapvalue);
		}
		throw new IllegalArgumentException(String.valueOf(value));
	}

	
	private static String formatValue(Map<?,?> mapvalue) {
		try {
			// use JSON format but replace " by ' as it is required by CQL syntax.
			return new ObjectMapper().writeValueAsString(mapvalue).replace('\"', '\'');
		} catch (IOException e) {
			throw new IllegalArgumentException(String.valueOf(mapvalue), e);
		}
	}
	
	
	public static <T> AbstractType<T> toCassandraType(Class<?> clazz) {
		@SuppressWarnings("unchecked")
		AbstractType<T> cassandraType = (AbstractType<T>)cassandraTypes.get(clazz);
		if (cassandraType == null) {
			throw new IllegalArgumentException(clazz == null ? null : clazz.getName());
		}
		return cassandraType;
	}
}
