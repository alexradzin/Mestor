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

package org.mestor.util;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.annotation.Nullable;

import org.mestor.reflection.ConstructorAccessor;
import org.mestor.reflection.MethodAccessor;

import com.google.common.base.Function;

public class FromStringFunction<T> implements Function<String, T> {
	private final static String FROM_STRING_FACTORY_METHOD_DEF = "fromstring.properties";
	private final static Map<Class<?>, java.lang.reflect.Method> classToFactoryMethod = new LinkedHashMap<>(); // ordering may be important here when mapping is done using base class or interfaece
	private final Class<T> type;
	
	static {
		init();
	}

	public FromStringFunction(final Class<T> type) {
		this.type = type;
	}
	
	
	@Override
	public T apply(@Nullable final String input) {
		final Class<?>[] stringParamType = new Class[] {String.class};
		
		
		if (type.isEnum()) {
			return new MethodAccessor<T>(type, "valueOf", stringParamType, null).value(input);
		}
	
		java.lang.reflect.Method factoryMethod = classToFactoryMethod.get(type);
		if (factoryMethod == null) {
			for (final Entry<Class<?>, java.lang.reflect.Method> c2f : classToFactoryMethod.entrySet()) {
				if (c2f.getKey().isAssignableFrom(type)) {
					factoryMethod = c2f.getValue();
				}
			}
		}

		if (factoryMethod != null) {
			return new MethodAccessor<T>(factoryMethod).value(input);
		}
		
		
		return new ConstructorAccessor<T>(type, stringParamType).value(input);
	}

	// TODO: should it look for all available fromstring.properties files and merge them?
	private static void init() {
		final ClassLoader cl = Thread.currentThread().getContextClassLoader();
		try (InputStream init = cl.getResourceAsStream(FROM_STRING_FACTORY_METHOD_DEF)) {
			if (init == null) {
				return;
			}
			final Properties classNameToFactoryMethod = new Properties();
			classNameToFactoryMethod.load(init);
			
			for (final Entry<Object, Object> entry : classNameToFactoryMethod.entrySet()) {
				final String className = (String)entry.getKey();
				final String factoryMethodName = (String)entry.getValue();
				
				final Class<?> clazz = cl.loadClass(className);
				
				// look for the static method that accepts String or CharSequence and returns instance of given class
				java.lang.reflect.Method factoryMethod = null;
				for (final Class<?> paramType : new Class[] {String.class, CharSequence.class}) {
					try {
						factoryMethod = clazz.getMethod(factoryMethodName, paramType);
						if (!Modifier.isStatic(factoryMethod.getModifiers())) {
							continue; // factory method must be static
						}
						if(clazz.isAssignableFrom(factoryMethod.getReturnType())) {
							continue; // wrong return type
						}
						
					} catch (final NoSuchMethodException e) {
						continue;
					}
				}

				if (factoryMethod == null) {
					throw new IllegalArgumentException("Cannot find accessible factory method " + factoryMethodName + " in class " + clazz);
				}
				
				classToFactoryMethod.put(clazz, factoryMethod);
			}
			
		} catch (final IOException e) {
			throw new IllegalStateException(e);
		} catch (final ClassNotFoundException e) {
			throw new IllegalStateException(e);
		}
	}
}
