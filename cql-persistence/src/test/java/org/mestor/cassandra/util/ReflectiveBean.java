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

package org.mestor.cassandra.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/** 
 * This class contains utilities that provide naive implementation 
 * of bean properties access. Good for tests.
 * @author alexr
 *
 */
public class ReflectiveBean {
	public static <T> Field getField(Class<?> clazz, String name) {
		try {
			Field f = clazz.getDeclaredField(name);
			return f;
		} catch (ReflectiveOperationException e) {
			return null;
		}
	}

	public static <T> Method getGetter(Class<?> clazz, String name) {
		try {
			String methodName = getMethodName(name, "get");
			Method m = clazz.getMethod(methodName);
			return m;
		} catch (ReflectiveOperationException e1) {
			try {
				String methodName = getMethodName(name, "is");
				Method m = clazz.getMethod(methodName);
				return m;
			} catch (ReflectiveOperationException e2) {
				return null;
			}
		}
	}

	
	public static <T> Method getSetter(Class<?> clazz, Class<?> type, String name) {
		try {
			String methodName = getMethodName(name, "set");
			Method m = clazz.getMethod(methodName, type);
			return m;
		} catch (ReflectiveOperationException e) {
			return null;
		}
	}
	
	
	private static String getMethodName(String propertyName, String prefix) {
		return prefix + propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
	}
}
