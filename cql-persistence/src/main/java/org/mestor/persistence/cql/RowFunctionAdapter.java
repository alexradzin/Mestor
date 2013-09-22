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

package org.mestor.persistence.cql;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.datastax.driver.core.Row;
import com.google.common.base.Function;

public abstract class RowFunctionAdapter<R> implements Function<Row, R> {
	/**
	 * This method extracts typed value from result set. It accepts row, field name and
	 * probably confusing array of types. This array is 1 element long for regular types. 
	 * However it can contain 2 elements if the first one is collection and 3 elements if 
	 * the first one is map. 
	 * 
	 * If first type in array is collection the second marks the type of collection elements. 
	 * If first type in array is map the next elements represent types of key and value respectively. 
	 *    
	 * @param row raw row from result set
	 * @param name field name
	 * @param types array of types. 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected <T> T getValue(Row row, String name, Class<?>[] types) {
		
		Class<T> type = (Class<T>)types[0];
		
		if (Boolean.class.equals(type) || boolean.class.equals(type)) {
			checkTypes(types, 1, name);
			return (T)Boolean.valueOf(row.getBool(name));
		}

		if (Integer.class.equals(type) || int.class.equals(type)) {
			checkTypes(types, 1, name);
			return (T)Integer.valueOf(row.getInt(name));
		}
		
		if (Long.class.equals(type) || long.class.equals(type)) {
			checkTypes(types, 1, name);
			return (T)Long.valueOf(row.getLong(name));
		}

		if (Double.class.equals(type) || double.class.equals(type)) {
			checkTypes(types, 1, name);
			return (T)Double.valueOf(row.getDouble(name));
		}
		
		if (Float.class.equals(type) || float.class.equals(type)) {
			checkTypes(types, 1, name);
			return (T)Float.valueOf(row.getFloat(name));
		}
		
		if (CharSequence.class.isAssignableFrom(type)) {
			checkTypes(types, 1, name);
			return (T)row.getString(name);
		}
		
		if (Date.class.isAssignableFrom(type)) {
			checkTypes(types, 1, name);
			return (T)row.getDate(name);
		}

		if (InetAddress.class.isAssignableFrom(type)) {
			checkTypes(types, 1, name);
			return (T)row.getInet(name);
		}

		if (UUID.class.isAssignableFrom(type)) {
			checkTypes(types, 1, name);
			return (T)row.getUUID(name);
		}

		
		if (BigInteger.class.isAssignableFrom(type)) {
			checkTypes(types, 1, name);
			return (T)BigInteger.valueOf(row.getLong(name));
		}
		
		if (BigDecimal.class.isAssignableFrom(type)) {
			checkTypes(types, 1, name);
			return (T)row.getDecimal(name);
		}
		

		if (ByteBuffer.class.isAssignableFrom(type)) {
			checkTypes(types, 1, name);
			return (T)row.getBytes(name);
		}
		
		if (byte[].class.equals(type)) {
			checkTypes(types, 1, name);
			return (T)row.getBytes(name).array();
		}
		

		// collections
		
		if (List.class.isAssignableFrom(type)) {
			checkTypes(types, 2, name);
			return (T)row.getList(name, types[1]);
		}
		
		if (Set.class.isAssignableFrom(type)) {
			checkTypes(types, 2, name);
			return (T)row.getSet(name, types[1]);
		}
		
		if (Map.class.isAssignableFrom(type)) {
			checkTypes(types, 3, name);
			return (T)row.getMap(name, types[1], types[2]);
		}
		
		if (type.isArray()) {
			checkTypes(types, 1, name);
			
			Class<?> t = types[0].getComponentType();
			List<?> list = row.getList(name, t);
			int n = list.size();
			// create typed array and copy list elements there
			Object array = Array.newInstance(type.getComponentType(), n);
			for(int i = 0; i < n; i++) {
				Array.set(array, i, list.get(i));
			}

			return (T)array;
		}
		
		throw new UnsupportedOperationException(type.getName());
	}
	
	private void checkTypes(Class<?>[] types, int expected, String name) {
		if (types.length != expected) {
			throw new IllegalArgumentException("Wrong number of types for field " + name);
		}
	}

}
