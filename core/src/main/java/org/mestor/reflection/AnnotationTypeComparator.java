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
import java.util.Comparator;

/**
 * Comparator that compares 2 annotations by their classes. 2 annotations that belong to the same class are equal.
 * The annotation attributes are ignored. 
 * @author alexr
 *
 * @param <A>
 */
public class AnnotationTypeComparator<A extends Annotation> implements Comparator<A> {
	@Override
	public int compare(A a1, A a2) {
		return getAnnotationTypeName(a1).compareTo(getAnnotationTypeName(a2));
	}
	
	private String getAnnotationTypeName(A a) {
		return a == null ? null : a.annotationType().getName();
	}
}
