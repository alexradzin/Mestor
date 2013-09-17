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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.apache.cassandra.cql3.CQL3Type;
import org.apache.cassandra.cql3.CQL3Type.Native;
import org.apache.cassandra.db.marshal.AbstractType;
import org.apache.cassandra.db.marshal.ListType;
import org.apache.cassandra.db.marshal.MapType;
import org.apache.cassandra.db.marshal.SetType;
import org.codehaus.jackson.map.ObjectMapper;

import com.google.common.base.Joiner;
import com.google.common.primitives.Primitives;

public class CommandHelper {
	private final static Map<Class<?>, AbstractType<?>> cassandraTypes = new HashMap<>();
	static {
		initTypeMapping();
	}
	
	
	private static void initTypeMapping() {
		for (Native nativeType : Native.values()) {
			AbstractType<?> cassandraType = nativeType.getType();
			
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

		// define some mappings explicitly to avoid ambiguity.   
		cassandraTypes.put(UUID.class, Native.UUID.getType());
		cassandraTypes.put(String.class, Native.TEXT.getType());
		cassandraTypes.put(Long.class, Native.BIGINT.getType());
		cassandraTypes.put(byte[].class, Native.BLOB.getType());
		cassandraTypes.put(Byte[].class, Native.BLOB.getType());

		// define collection types
		cassandraTypes.put(List.class, ListType.getInstance((AbstractType<?>)null));
		cassandraTypes.put(Set.class, SetType.getInstance((AbstractType<?>)null));
		cassandraTypes.put(Map.class, MapType.getInstance(null, null));

		// find all primitive wrappers and create additional mapping for corresponding primitives
		// wrap entry set with HashSet to avoid ConcurrentModificationException when adding entries while iterating over the map.
		for (Entry<Class<?>, AbstractType<?>> entry : new HashSet<Entry<Class<?>, AbstractType<?>>>(cassandraTypes.entrySet())) {
			Class<?> mappedClass = entry.getKey();
			if (Primitives.isWrapperType(mappedClass)) {
				cassandraTypes.put(Primitives.unwrap(mappedClass), entry.getValue());
			}
		}

		cassandraTypes.put(Object[].class, Native.BLOB.getType());
	}
	
	
	public static String fullname(String keyspace, String name) {
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

	
	static String createIndex(String indexName, String tableName, String columnName) {
		return Joiner.on(" ").join("CREATE INDEX", indexName, "ON", tableName, "(", columnName, ")");
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
	
	
	@SuppressWarnings("unchecked")
	public static <T> AbstractType<T> toCassandraType(Class<?> clazz) {
		// first try to find the direct mapping 
		AbstractType<T> cassandraType = (AbstractType<T>)cassandraTypes.get(clazz);

		if (cassandraType != null) {
			return cassandraType;
		}
		
		
		// direct mapping is not found. Try to find mapping of base class or interface
		for (Entry<Class<?>, AbstractType<?>> entry : cassandraTypes.entrySet()) {
			Class<?> mappedClass = entry.getKey();
			if (mappedClass.isAssignableFrom(clazz)) {
				return (AbstractType<T>)entry.getValue(); 
			}
		}
	
		// if we are here no mapping between java class and cassandra type is found.
		throw new IllegalArgumentException(clazz == null ? null : clazz.getName());
	}
	
	public static CQL3Type toCqlType(Class<?> clazz) {
		AbstractType<?> cassandraType = toCassandraType(clazz);
		return cassandraType == null ? null : cassandraType.asCQL3Type();
	}
}
