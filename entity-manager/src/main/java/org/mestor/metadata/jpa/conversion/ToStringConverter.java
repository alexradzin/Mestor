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

package org.mestor.metadata.jpa.conversion;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import javax.persistence.AttributeConverter;

public class ToStringConverter<T> implements AttributeConverter<T, String> {
	private final Class<T> clazz;
	private final Method factoryMethod;
	private final Constructor<T> constructor;

	public ToStringConverter(final Class<T> clazz) {
		this.clazz = clazz;
		this.factoryMethod = findFactoryMethod("valueOf", "fromString");
		if (factoryMethod == null) {
			try {
				constructor = clazz.getConstructor(String.class);
			} catch (NoSuchMethodException | SecurityException e) {
				throw new IllegalArgumentException("Class " + clazz +
						" does not have neigher suitable factory method nore constructor " +
						clazz.getSimpleName() + "(String)");
			}
		} else {
			constructor = null;
		}
	}

	@Override
	public String convertToDatabaseColumn(final T attribute) {
		return attribute == null ? null : attribute.toString();
	}

	@Override
	public T convertToEntityAttribute(final String dbData) {
		try {
			if (factoryMethod != null) {
				@SuppressWarnings("unchecked")
				final
				T result = (T)factoryMethod.invoke(null, dbData);
				return result;
			}
			return constructor.newInstance(dbData);
		} catch (final ReflectiveOperationException e) {
			throw new IllegalArgumentException(e);
		}
	}


	private Method findFactoryMethod(final String ... names) {
		for (final String name : names) {
			Method m;
			try {
				m = clazz.getMethod(name, String.class);
				if (Modifier.isStatic(m.getModifiers()) && clazz.isAssignableFrom(m.getReturnType())) {
					return m;
				}
			} catch (NoSuchMethodException | SecurityException e) {
				continue;
			}
		}
		return null;
	}
}
