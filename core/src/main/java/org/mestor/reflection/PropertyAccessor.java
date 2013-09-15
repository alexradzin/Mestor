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
	private final String name;
	private final Field field;
	private final Method getter;
	private final Method setter;
	
	private final Access<? extends AccessibleObject> readAccess;
	private final Access<? extends AccessibleObject> writeAccess;

	
	public PropertyAccessor(Class<T> type, String name, Field field, Method getter, Method setter, Class<? extends Access<AccessibleObject>> readAccessType, Class<? extends Access<AccessibleObject>> writeAccessType) {
		this.type = type;
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


	public PropertyAccessor(Class<T> type, String name, Field field, Method getter, Method setter) {
		this.type = type;
		this.name = name;
		this.field = field;
		this.getter = getter;
		this.setter = setter;

		// discover access mode automatically
		
		final MethodAccess methodAccess = new MethodAccess();
		final FieldAccess fieldAccess = new FieldAccess();
		
		
		if (getter != null) {
			this.readAccess = methodAccess;
		} else if (field != null ) {
			this.readAccess = fieldAccess;
		} else {
			this.readAccess = null;
		}
		
		if (setter != null) {
			this.writeAccess = new MethodAccess();
		} else if (field != null ) {
			this.writeAccess = fieldAccess;
		} else {
			this.writeAccess = null;
		}
	}
	
	
	
	public abstract class Access<A extends AccessibleObject> {
		public abstract P get(T instance);
		public abstract void set(T instance, P parameter);
	}

	
	@SuppressWarnings("unchecked")
	public class FieldAccess extends Access<Field> {
		FieldAccess() {
			field.setAccessible(true);
		}
		
		@Override
		public P get(T instance) {
			try {
				return (P)field.get(instance);
			} catch (IllegalAccessException e) {
				throw new IllegalStateException(e);
			}
		}

		@Override
		public void set(T instance, P parameter) {
			try {
				field.set(instance, parameter);
			} catch (IllegalAccessException e) {
				throw new IllegalStateException(e);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public class MethodAccess extends Access<Method> {
		MethodAccess() {
			setter.setAccessible(true);
			getter.setAccessible(true);
		}
		
		@Override
		public P get(T instance) {
			try {
				return (P)getter.invoke(instance);
			} catch (ReflectiveOperationException e) {
				throw new IllegalStateException(e);
			}
		}

		@Override
		public void set(T instance, P parameter) {
			try {
				setter.invoke(instance, parameter);
			} catch (ReflectiveOperationException e) {
				throw new IllegalStateException(e);
			}
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


	public Access<? extends AccessibleObject> getReadAccess() {
		return readAccess;
	}


	public Access<? extends AccessibleObject> getWriteAccess() {
		return writeAccess;
	}
	
	
}
