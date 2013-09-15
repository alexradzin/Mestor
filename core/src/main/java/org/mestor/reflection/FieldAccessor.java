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

package org.mestor.reflection;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nullable;

public class FieldAccessor<V> extends Valuable<java.lang.reflect.Field, V> {
	private final Object obj;

	public FieldAccessor(Class<?> type, String fieldName, @Nullable Object obj) {
		this(getField(type, fieldName), obj);
	}
	
	public FieldAccessor(java.lang.reflect.Field f, @Nullable Object obj) {
		super(f);
		this.obj = obj;
	}

	@Override
	protected Object valueImpl(@Nullable Object ... args) throws ReflectiveOperationException {
		if (args != null) {
			throw new IllegalArgumentException("Field cannot accept arguments");
		}
		return accessible.get(obj);
	}

	public static Field getField(Class<?> type, String fieldName) {
		NoSuchFieldException nsfe = null;
		
		for(Class<?> c = type; c != null; c = c.getSuperclass()) {
			try {
				Field f = c.getDeclaredField(fieldName);
				f.setAccessible(true);
				return f;
			} catch (NoSuchFieldException e) {
				if (nsfe == null) {
					nsfe = e;
				}
				continue; // to the superclass
			}
		}
		throw new IllegalArgumentException("Field " + fieldName + " is not found in class " + type, nsfe);
	}
	

	public static Field[] getFields(Class<?> type) {
		List<Field> allFields = new ArrayList<>();
		
		for(Class<?> c = type; c != null; c = c.getSuperclass()) {
			Field[] fields = c.getDeclaredFields();
			allFields.addAll(0, Arrays.asList(fields));
		}
		
		return allFields.toArray(new Field[allFields.size()]);
	}
	
	
}
