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

import java.lang.reflect.Method;

public class MethodAccess<T, P> extends AbstractAccess<T, P, Method> {
	MethodAccess(Method getter, Method setter) {
		super(getter, setter);
		if (getter != null) {
			getter.setAccessible(true);
		}
		if (setter != null) {
			setter.setAccessible(true);
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public P get(T instance) {
		try {
			return (P)accessibleObjects[0].invoke(instance);
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException(e.getMessage(), e);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			throw e;
		}
	}

	@Override
	public void set(T instance, P parameter) {
		try {
			accessibleObjects[1].invoke(instance, parameter);
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException(e);
		}
	}

}
