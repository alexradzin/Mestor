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

public class ClassAccessor {
	
	@SuppressWarnings("unchecked")
	public static <T> Class<T> forName(String className) {
		try {
			return (Class<T>)Class.forName(className);
		} catch (ClassNotFoundException e) {
			throw new IllegalArgumentException(className, e);
		}
	}

	public static <T> T newInstance(Class<T> clazz) {
		try {
			return clazz.newInstance();
		} catch (ReflectiveOperationException e) {
			throw new IllegalArgumentException(clazz.getName(), e);
		}
	}
	
	
	public static <T, R> R invoke(Class<? extends T> clazz, String methodName, Class<?>[] paramTypes, Class<R> returnType, T obj, Object[] args) {
		//TODO search for non-public method and for methods with not exact match of parameters, i.e. those that assignable from parameters
		Method method;
		try {
			method = clazz.getMethod(methodName, paramTypes);
			method.setAccessible(true);
			@SuppressWarnings("unchecked")
			R returnValue = (R)method.invoke(obj, args);
			return returnValue;
		} catch (ReflectiveOperationException e) {
			throw new IllegalArgumentException(e);
		}
	}
}
