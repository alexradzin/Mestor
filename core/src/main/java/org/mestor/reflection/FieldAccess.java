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


public class FieldAccess<T, P> extends AbstractAccess<T, P, Field> {

	protected FieldAccess(Field accessibleObject) {
		super(accessibleObject);
		if (accessibleObject != null) {
			accessibleObject.setAccessible(true);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public P get(T instance) {
		try {
			return (P)accessibleObjects[0].get(instance);
		} catch (IllegalAccessException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public void set(T instance, P parameter) {
		try {
			accessibleObjects[0].set(instance, parameter);
		} catch (IllegalAccessException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public Class<?> getDeclaringClass() {
		return findNotNullAccessibleObject().getDeclaringClass();
	}

	@Override
	public String getName() {
		return findNotNullAccessibleObject().getName();
	}

	@Override
	public int getModifiers() {
		return accessibleObjects[0].getModifiers();
	}

	@Override
	public boolean isSynthetic() {
		return findNotNullAccessibleObject().isSynthetic();
	}
}
