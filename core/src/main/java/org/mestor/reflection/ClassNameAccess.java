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

/**
 * Implementation of {@link Access} that transforms instance to its class name. 
 * Good for implementation of "virtual" entity fields, i.e. fields that do not really exist.
 * In practice this class is used for discriminators.
 * @author alexr
 *
 * @param <T>
 */
public class ClassNameAccess <T> implements Access<T, String, AccessibleObject> {

	@Override
	public String get(T instance) {
		return instance.getClass().getName();
	}

	@Override
	public void set(T instance, String parameter) {
		// Nothing to do. Class name is a fake field. 
	}

	@Override
	public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
		return false;
	}

	@Override
	public <N extends Annotation> N getAnnotation(Class<N> annotationClass) {
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
		return "$classname";
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
