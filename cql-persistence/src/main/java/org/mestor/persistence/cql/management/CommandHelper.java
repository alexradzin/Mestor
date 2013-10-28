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
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import org.apache.cassandra.cql3.CQL3Type;
import org.apache.cassandra.cql3.CQL3Type.Native;
import org.apache.cassandra.db.marshal.AbstractType;
import org.apache.cassandra.db.marshal.ListType;
import org.apache.cassandra.db.marshal.MapType;
import org.apache.cassandra.db.marshal.SetType;
import org.codehaus.jackson.map.ObjectMapper;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Collections2;
import com.google.common.primitives.Primitives;

public class CommandHelper {
	private final static Pattern capitalLetter = Pattern.compile("[A-Z]");
	private final static Map<Class<?>, AbstractType<?>> cassandraTypes;
	static {
		cassandraTypes = Collections.unmodifiableMap(initTypeMapping());
	}

	public static Map<Class<?>, AbstractType<?>> getCassandraTypes() {
		return cassandraTypes;
	}

	private static Map<Class<?>, AbstractType<?>> initTypeMapping() {
		final Map<Class<?>, AbstractType<?>> cassandraTypesTmp = new HashMap<>();
		for (final Native nativeType : Native.values()) {
			final AbstractType<?> cassandraType = nativeType.getType();

			Class<?> type = cassandraType.getClass();
			while(type != null && !AbstractType.class.equals(type.getSuperclass())){
				type = type.getSuperclass();
			}
			// if type is null here NullPointerException will be thrown.
			@SuppressWarnings("null")
			final Type abstractType = type.getGenericSuperclass();

			final Class<?> clazz = (Class<?>)((ParameterizedType)abstractType).getActualTypeArguments()[0];
			cassandraTypesTmp.put(clazz, cassandraType);
		}

		// define some mappings explicitly to avoid ambiguity.
		cassandraTypesTmp.put(UUID.class, Native.UUID.getType());
		cassandraTypesTmp.put(String.class, Native.TEXT.getType());
		cassandraTypesTmp.put(Long.class, Native.BIGINT.getType());
		cassandraTypesTmp.put(BigInteger.class, Native.BIGINT.getType());
		cassandraTypesTmp.put(BigDecimal.class, Native.DECIMAL.getType());


		cassandraTypesTmp.put(byte[].class, Native.BLOB.getType());
		cassandraTypesTmp.put(Byte[].class, Native.BLOB.getType());

		// define collection types
		cassandraTypesTmp.put(List.class, ListType.getInstance((AbstractType<?>)null));
		cassandraTypesTmp.put(Set.class, SetType.getInstance((AbstractType<?>)null));
		cassandraTypesTmp.put(Map.class, MapType.getInstance(null, null));

		// find all primitive wrappers and create additional mapping for corresponding primitives
		// wrap entry set with HashSet to avoid ConcurrentModificationException when adding entries while iterating over the map.
		for (final Entry<Class<?>, AbstractType<?>> entry : new HashSet<Entry<Class<?>, AbstractType<?>>>(cassandraTypesTmp.entrySet())) {
			final Class<?> mappedClass = entry.getKey();
			if (Primitives.isWrapperType(mappedClass)) {
				cassandraTypesTmp.put(Primitives.unwrap(mappedClass), entry.getValue());
			}
		}

		// define array as list.
		cassandraTypesTmp.put(Object[].class, ListType.getInstance((AbstractType<?>)null));
		return cassandraTypesTmp;
	}

	// quote
	public static String fullname(final String keyspace, final String name) {
		return keyspace == null ? name : quote(keyspace) + "." + quote(name);
	}


	static String createQueryString(final String[] tokens, final Map<String, Object> with) {
		final StringBuilder buf = new StringBuilder();
		Joiner.on(" ").skipNulls().appendTo(buf, tokens);
		if (with != null && !with.isEmpty()) {
			buf.append(" ").append("WITH").append(" ");
			final int n = with.size();
			int i = 0;
			for (final Entry<String, Object> prop : with.entrySet()) {
				buf.append(" ").append(prop.getKey()).append("=").append(formatValue(prop.getValue()));
				if (i < n - 1) {
					buf.append(" AND ");
				}
				i++;
			}
		}
		return buf.toString();
	}


//	static String createIndex(String indexName, String tableName, String columnName) {
//		return Joiner.on(" ").join("CREATE INDEX", indexName, "ON", tableName, "(", columnName, ")");
//	}


	private static String formatValue(final Object value) {
		if (value == null) {
			return null;
		}
		final Class<?> type = value.getClass();
		if (CharSequence.class.isAssignableFrom(type)) {
			return "'" + value + "'";
		}
		if (Number.class.isAssignableFrom(type)) {
			return value.toString();
		}
		if (Map.class.isAssignableFrom(type)) {
			final Map<?,?> mapvalue = (Map<?,?>)value;
			return formatValue(mapvalue);
		}
		throw new IllegalArgumentException(String.valueOf(value));
	}


	private static String formatValue(final Map<?,?> mapvalue) {
		try {
			// use JSON format but replace " by ' as it is required by CQL syntax.
			return new ObjectMapper().writeValueAsString(mapvalue).replace('\"', '\'');
		} catch (final IOException e) {
			throw new IllegalArgumentException(String.valueOf(mapvalue), e);
		}
	}


	@SuppressWarnings("unchecked")
	public static <T> AbstractType<T> toCassandraType(final Class<?> clazz) {
		// first try to find the direct mapping
		final AbstractType<T> cassandraType = (AbstractType<T>)cassandraTypes.get(clazz);

		if (cassandraType != null) {
			return cassandraType;
		}


		// direct mapping is not found. Try to find mapping of base class or interface
		for (final Entry<Class<?>, AbstractType<?>> entry : cassandraTypes.entrySet()) {
			final Class<?> mappedClass = entry.getKey();
			if (mappedClass.isAssignableFrom(clazz)) {
				return (AbstractType<T>)entry.getValue();
			}
		}

		// if we are here no mapping between java class and cassandra type is found.
		throw new IllegalArgumentException(clazz == null ? null : clazz.getName());
	}

	public static CQL3Type toCqlType(final Class<?> clazz, final Class<?> ... generics) {
		AbstractType<?> cassandraType = toCassandraType(clazz);

		if (cassandraType == null) {
			return null;
		}

		// Here is a special patch for collections.
		// Fortunately only lists, sets and maps are supported.
		if (cassandraType.isCollection()) {
			if (cassandraType instanceof ListType) {
				// since for convenience we treat arrays as lists we need a little patch here.
				if (clazz.isArray()) {
					cassandraType = ListType.getInstance(toCassandraType(clazz.getComponentType()));
				} else {
					cassandraType = ListType.getInstance(toCassandraType(generics[0]));
				}
			} else if (cassandraType instanceof SetType) {
				cassandraType = SetType.getInstance(toCassandraType(generics[0]));
			} else if (cassandraType instanceof MapType) {
				cassandraType = MapType.getInstance(toCassandraType(generics[0]), toCassandraType(generics[1]));
			} else {
				// Just in case. To be on the safe side.
				throw new IllegalArgumentException("Unsupported cassandra collection type " + cassandraType);
			}
		}

		return cassandraType.asCQL3Type();
	}

	/**
	 * Quotes given string if it contains at least one capital latter.
	 * This is needed to preserve given case of identifiers when using Cassandra.
	 * For lower case identifiers quoting is redundant.
	 * @param name
	 * @return quoted name if needed
	 */
	public static String quote(final String name) {
		return capitalLetter.matcher(name).find() ? "\"" + name + "\"" : name;
	}

	public static Collection<String> quote(final Collection<String> strings) {
		return Collections2.transform(strings, new Function<String, String>() {
			@Override
			public String apply(final String s) {
				return quote(s);
			}
		});
	}
}
