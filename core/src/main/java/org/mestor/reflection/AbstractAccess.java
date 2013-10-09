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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * Base class for implementations of {@link Access} 
 * @author alexr
 *
 * @param <T>
 * @param <P>
 * @param <A>
 */
public abstract class AbstractAccess<T, P, A extends AccessibleObject> implements Access<T, P, A> {
	protected final A[] accessibleObjects;
	
	@SafeVarargs
	protected AbstractAccess(A ...accessibleObjects) {
		this.accessibleObjects = accessibleObjects;
	}

	
	@Override
	public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
		for (A accessibleObject : accessibleObjects) {
			if (accessibleObject.isAnnotationPresent(annotationClass)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public <N extends Annotation> N getAnnotation(Class<N> annotationClass) {
		for (A accessibleObject : accessibleObjects) {
			N annotation = accessibleObject.getAnnotation(annotationClass);
			if (annotation != null) {
				return annotation;
			}
		}
		return null;
	}

	@Override
	public Annotation[] getAnnotations() {
		Collection<Annotation> annotations = new ArrayList<>();
		for (A accessibleObject : accessibleObjects) {
			annotations.addAll(Arrays.asList(accessibleObject.getAnnotations()));
		}
		return annotations.toArray(new Annotation[annotations.size()]);
	}

	@Override
	public Annotation[] getDeclaredAnnotations() {
		Collection<Annotation> annotations = new ArrayList<>();
		for (A accessibleObject : accessibleObjects) {
			annotations.addAll(Arrays.asList(accessibleObject.getDeclaredAnnotations()));
		}
		return annotations.toArray(new Annotation[annotations.size()]);
	}	
	
}
