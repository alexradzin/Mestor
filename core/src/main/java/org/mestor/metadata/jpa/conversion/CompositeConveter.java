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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.AttributeConverter;

import com.google.common.collect.Lists;

public class CompositeConveter<F, C> implements AttributeConverter<F, C> {
	private List<AttributeConverter<Object, Object>> converters = new ArrayList<>();
	
	
	@SafeVarargs
	public CompositeConveter(AttributeConverter<Object, Object> ... converters) {
		this(Arrays.asList(converters));
	}

	public CompositeConveter(List<AttributeConverter<Object, Object>> converters) {
		converters.addAll(converters);
	}
	

	@Override
	public C convertToDatabaseColumn(F attribute) {
		Object a = attribute;
		for (AttributeConverter<Object, Object> converter : converters) {
			a = converter.convertToDatabaseColumn(a);
		}
		
		// the last converter in chain must return value compatible with type C
		@SuppressWarnings("unchecked")
		C result = (C)a;
		
		return result;
	}

	@Override
	public F convertToEntityAttribute(C dbData) {
		// iterate over converters with reverse order
		Object d = dbData;
		for (AttributeConverter<Object, Object> converter : Lists.reverse(converters)) {
			d = converter.convertToEntityAttribute(d);
		}
		
		// the last converter in chain must return value compatible with type F
		@SuppressWarnings("unchecked")
		F result = (F)d;
		
		return result;
	}
}
