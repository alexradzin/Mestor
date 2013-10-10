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

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import org.mestor.metadata.BeanMetadataFactory;
import org.mestor.metadata.EntityMetadata;
import org.mestor.metadata.FieldMetadata;

public class CompositePropertyAccessor<T, P> extends PropertyAccessor<T, P> {
	private final Class<P> compositeType; 
	private final PropertyAccessor<T, Object>[] accessors;
	private final Map<String, PropertyAccessor<T, Object>> name2accessors;
	private final BeanMetadataFactory mdf = new BeanMetadataFactory();
	private final EntityMetadata<P> pm;
	
	
	public CompositePropertyAccessor(Class<T> type, Class<P> compositeType, String name, PropertyAccessor<T, Object>[] accessors) {
		super(type, compositeType, name, null, null, null);

		this.compositeType = compositeType;
		this.accessors = Arrays.copyOf(accessors, accessors.length);
		
		
		name2accessors = new LinkedHashMap<>();
		for (PropertyAccessor<T, Object> accessor : accessors) {
			name2accessors.put(accessor.getName(), accessor);
		}
		
		
		this.pm = mdf.create(compositeType);
	}

	
	
	@Override
	public P getValue(T instance) {
		P p;
		try {
			p = compositeType.newInstance();
		} catch (InstantiationException e) {
			throw new IllegalStateException(e);
		} catch (IllegalAccessException e) {
			throw new IllegalArgumentException("Class " + compositeType + " does not have accessible default constructor", e);
		}
		
		
		for (PropertyAccessor<T, Object> accessor : accessors) {
			String name = accessor.getName();
			Object value = accessor.getValue(instance);
			
			pm.getFieldByName(name).getAccessor().setValue(p, value);
		}
		
		return p;
	}
	
	@Override
	public void setValue(T instance, P value) {
		for(FieldMetadata<P, ?, ?> fmd : pm.getFields()) {
			Object pmFieldValue = fmd.getAccessor().getValue(value);
			name2accessors.get(fmd.getName()).setValue(instance, pmFieldValue);
		}
	}
}
