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

import static java.lang.reflect.Modifier.isPublic;
import static java.lang.reflect.Modifier.isStatic;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;

public class MethodAccessor<V> extends Valuable<java.lang.reflect.Method, V> {
	private static final Set<Class<?>> booleans = new HashSet<>(Arrays.<Class<?>>asList(boolean.class, Boolean.class)); 
	private final Object obj;

	//TODO think about changing the constructors to move obj to the beginning and chang paramTypes to elipsis
	public MethodAccessor(Class<?> type, String methodName, Class<?>[] paramTypes, @Nullable Object obj) {
		this(getMethod(type, methodName, paramTypes), obj);
	}

	public MethodAccessor(Class<?> type, String methodName, Class<?>[] paramTypes) {
		this(getMethod(type, methodName, paramTypes), null);
	}
	
	
	public MethodAccessor(java.lang.reflect.Method m, @Nullable Object obj) {
		super(m);
		this.obj = obj;
	}

	public MethodAccessor(java.lang.reflect.Method m) {
		this(m, null);
	}
	
	
	@Override
	protected Object valueImpl(Object ... args) throws ReflectiveOperationException {
		return accessible.invoke(obj, args);
	}

	public static java.lang.reflect.Method getMethod(Class<?> type, String methodName, Class<?> ... paramTypes) {
		try {
			return type.getMethod(methodName, paramTypes);
		} catch (NoSuchMethodException e) {
			throw new IllegalArgumentException(e);
		}
	}
	
	public static boolean isGetter(Method m) {
		Class<?> returnType = m.getReturnType();
		String name = m.getName();
		Class<?>[] paramTypes = m.getParameterTypes();
		return isPublicNonStatic(m) && paramTypes.length == 0 && 
				((name.startsWith("is") && booleans.contains(returnType)) || name.startsWith("get")); 
	}
	
	
	public static boolean isSetter(Method m) {
		Class<?> returnType = m.getReturnType();
		String name = m.getName();
		Class<?>[] paramTypes = m.getParameterTypes();
		return isPublicNonStatic(m) && paramTypes.length == 1 && name.startsWith("set") && void.class.equals(returnType); 
	}
	
	private static boolean isPublicNonStatic(Method m) {
		int mod = m.getModifiers();
		return isPublic(mod) && !isStatic(mod); 
	}
}
