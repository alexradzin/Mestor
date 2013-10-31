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

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PropertyAccessor<T, P> implements AnnotatedElement, Member {
	private final Class<T> type;
	private final Class<P> propertyType;
	private final String name;
	private final Field field;
	private final Method getter;
	private final Method setter;

	private final Access<T, P, ? extends AccessibleObject> readAccess;
	private final Access<T, P, ? extends AccessibleObject> writeAccess;


	public PropertyAccessor(final Class<T> type, final Class<P> fieldType, final String name, final Field field, final Method getter, final Method setter, final Access<T, P, ? extends AccessibleObject> readAccess, final Access<T, P, ? extends AccessibleObject> writeAccess) {
		this.type = type;
		this.propertyType = fieldType;
		this.name = name;
		this.field = field;
		this.getter = getter;
		this.setter = setter;

		this.readAccess = readAccess;
		this.writeAccess = writeAccess;
	}



	public PropertyAccessor(final Class<T> type, final Class<P> fieldType, final String name, final Field field, final Method getter, final Method setter, final Class<? extends Access<T, P, AccessibleObject>> readAccessType, final Class<? extends Access<T, P, AccessibleObject>> writeAccessType) {
		this.type = type;
		this.propertyType = fieldType;
		this.name = name;
		this.field = field;
		this.getter = getter;
		this.setter = setter;

		try {
			this.readAccess = readAccessType.newInstance();
			this.writeAccess = writeAccessType.newInstance();
		} catch (final ReflectiveOperationException e) {
			throw new IllegalArgumentException(e);
		}
	}


	public PropertyAccessor(final Class<T> type, final Class<P> fieldType, final String name, final Field field, final Method getter, final Method setter) {
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




	public P getValue(final T instance) {
		return readAccess.get(instance);
	}

	public void setValue(final T instance, final P value) {
		writeAccess.set(instance, value);
	}


	public Class<T> getType() {
		return type;
	}

	public Class<P> getPropertyType() {
		return propertyType;
	}

	@Override
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



	@Override
	public boolean isAnnotationPresent(final Class<? extends Annotation> annotationClass) {
		return getAnnotation(annotationClass) != null;
	}



	@Override
	public <A extends Annotation> A getAnnotation(final Class<A> annotationClass) {
		for (final AnnotatedElement ae : new AnnotatedElement[] {field, getter}) {
			if (ae == null) {
				continue;
			}
			final A a = ae.getAnnotation(annotationClass);
			if (a != null) {
				return a;
			}
		}
		return null;
	}



	@Override
	public Annotation[] getAnnotations() {
		final List<Annotation> annotations = new ArrayList<>();
		for (final AnnotatedElement ae : new AnnotatedElement[] {field, getter}) {
			if (ae != null) {
				annotations.addAll(Arrays.asList(ae.getAnnotations()));
			}
		}
		return annotations.toArray(new Annotation[annotations.size()]);
	}



	@Override
	public Annotation[] getDeclaredAnnotations() {
		final List<Annotation> annotations = new ArrayList<>();
		for (final AnnotatedElement ae : new AnnotatedElement[] {field, getter}) {
			if (ae != null) {
				annotations.addAll(Arrays.asList(ae.getDeclaredAnnotations()));
			}
		}
		return annotations.toArray(new Annotation[annotations.size()]);
	}

	public Type getGenericType() {
		Type genericType = null;
		if (field != null) {
			final Type fieldType = field.getGenericType();
			if (fieldType instanceof ParameterizedType) {
				return fieldType;
			}
			genericType = fieldType;
		}
		if (getter != null) {
			final Type getterType = getter.getGenericReturnType();
			if (getterType instanceof ParameterizedType) {
				return getterType;
			}
			genericType = getterType;
		}
		return genericType;
	}



	@Override
	public Class<?> getDeclaringClass() {
		// TODO Auto-generated method stub
		return null;
	}



	@Override
	public int getModifiers() {
		// TODO Auto-generated method stub
		return 0;
	}



	@Override
	public boolean isSynthetic() {
		// TODO Auto-generated method stub
		return false;
	}

}
