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

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class PropertyAccessor<T, P> {
	private final Class<T> type;
	private final Class<P> propertyType;
	private final String name;
	private final Field field;
	private final Method getter;
	private final Method setter;
	
	private final Access<T, P, ? extends AccessibleObject> readAccess;
	private final Access<T, P, ? extends AccessibleObject> writeAccess;


	public PropertyAccessor(Class<T> type, Class<P> fieldType, String name, Field field, Method getter, Method setter, Access<T, P, ? extends AccessibleObject> readAccess, Access<T, P, ? extends AccessibleObject> writeAccess) {
		this.type = type;
		this.propertyType = fieldType;
		this.name = name;
		this.field = field;
		this.getter = getter;
		this.setter = setter;
		
		this.readAccess = readAccess;
		this.writeAccess = writeAccess;
	}
	
	
	
	public PropertyAccessor(Class<T> type, Class<P> fieldType, String name, Field field, Method getter, Method setter, Class<? extends Access<T, P, AccessibleObject>> readAccessType, Class<? extends Access<T, P, AccessibleObject>> writeAccessType) {
		this.type = type;
		this.propertyType = fieldType;
		this.name = name;
		this.field = field;
		this.getter = getter;
		this.setter = setter;
		
		try {
			this.readAccess = readAccessType.newInstance();
			this.writeAccess = writeAccessType.newInstance();
		} catch (ReflectiveOperationException e) {
			throw new IllegalArgumentException(e);
		}
	}


	public PropertyAccessor(Class<T> type, Class<P> fieldType, String name, Field field, Method getter, Method setter) {
		this.type = type;
		this.propertyType = fieldType;
		this.name = name;
		this.field = field;
		this.getter = getter;
		this.setter = setter;

		// discover access mode automatically
		
		final MethodAccess<T, P> methodAccess = new MethodAccess<T, P>(getter, setter);
		final FieldAccess<T, P> fieldAccess = new FieldAccess<T, P>(field);
		
		
		if (getter != null) {
			this.readAccess = methodAccess;
		} else if (field != null ) {
			this.readAccess = fieldAccess;
		} else {
			this.readAccess = null;
		}
		
		if (setter != null) {
			this.writeAccess = methodAccess;
		} else if (field != null ) {
			this.writeAccess = fieldAccess;
		} else {
			this.writeAccess = null;
		}
	}
	
	
	
	
	public P getValue(T instance) {
		return readAccess.get(instance);
	}
	
	public void setValue(T instance, P value) {
		writeAccess.set(instance, value);
	}

	
	public Class<T> getType() {
		return type;
	}

	public Class<P> getPropertyType() {
		return propertyType;
	}
	public String getName() {
		return name;
	}


	public Field getField() {
		return field;
	}


	public Method getGetter() {
		return getter;
	}


	public Method getSetter() {
		return setter;
	}


	public Access<T, P, ? extends AccessibleObject> getReadAccess() {
		return readAccess;
	}


	public Access<T, P, ? extends AccessibleObject> getWriteAccess() {
		return writeAccess;
	}
	
	
}
