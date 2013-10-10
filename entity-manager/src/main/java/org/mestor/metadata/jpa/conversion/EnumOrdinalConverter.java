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

import java.lang.reflect.Array;

import javax.persistence.AttributeConverter;

public class EnumOrdinalConverter<T extends Enum<T>> implements AttributeConverter<T, Integer> {
	private final Class<T> enumType;
	private T[] elements;
	
	public EnumOrdinalConverter(Class<T> enumType) {
		this.enumType = enumType;
		elements = init();
	}

	private T[] init() {
		Object values;
		try {
			values = enumType.getMethod("values").invoke(null);
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException(e);
		}
		
		int n = Array.getLength(values);
		@SuppressWarnings("unchecked")
		T[] elems = (T[])Array.newInstance(enumType, n);
		for (int i = 0; i < n; i++) {
			@SuppressWarnings("unchecked")
			T element = (T)Array.get(values, i);
			int ordinal = element.ordinal();
			elems[ordinal] = element;
		}
		return elems;
	}
	
	
	@Override
	public Integer convertToDatabaseColumn(T attribute) {
		return attribute == null ? null : attribute.ordinal();
	}

	@Override
	public T convertToEntityAttribute(Integer ordinal) {
		return ordinal == null ? null : elements[ordinal];
	}
}
