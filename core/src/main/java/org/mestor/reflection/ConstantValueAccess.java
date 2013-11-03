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
import java.lang.reflect.Modifier;

public class ConstantValueAccess <T, V> implements Access<T, V, AccessibleObject> {
	private final V value;

	public ConstantValueAccess(final V value) {
		this.value = value;
	}

	@Override
	public V get(final T instance) {
		return value;
	}

	@Override
	public void set(final T instance, final V parameter) {
		// Nothing to do. This is a constant value
	}

	@Override
	public boolean isAnnotationPresent(final Class<? extends Annotation> annotationClass) {
		return false;
	}

	@Override
	public <N extends Annotation> N getAnnotation(final Class<N> annotationClass) {
		return null;
	}

	@Override
	public Annotation[] getAnnotations() {
		return null;
	}

	@Override
	public Annotation[] getDeclaredAnnotations() {
		return null;
	}

	@Override
	public Class<?> getDeclaringClass() {
		return null;
	}

	@Override
	public String getName() {
		return "$constant";
	}

	@Override
	public int getModifiers() {
		return Modifier.PUBLIC;
	}

	@Override
	public boolean isSynthetic() {
		return true;
	}
}
