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


	public CompositePropertyAccessor(final Class<T> type, final Class<P> compositeType, final String name, final PropertyAccessor<T, Object>[] accessors) {
		super(type, compositeType, name, null, null, null);

		this.compositeType = compositeType;
		this.accessors = Arrays.copyOf(accessors, accessors.length);


		name2accessors = new LinkedHashMap<>();
		for (final PropertyAccessor<T, Object> accessor : accessors) {
			name2accessors.put(accessor.getName(), accessor);
		}


		this.pm = mdf.create(compositeType);
	}


	//TODO: setValue() and getValue() are null-safe. Take a look on corresponding comment in PropertyAccessor

	@Override
	public P getValue(final T instance) {
		P p;
		try {
			p = compositeType.newInstance();
		} catch (final InstantiationException e) {
			throw new IllegalStateException(e);
		} catch (final IllegalAccessException e) {
			throw new IllegalArgumentException("Class " + compositeType + " does not have accessible default constructor", e);
		}


		for (final PropertyAccessor<T, Object> accessor : accessors) {
			final String name = accessor.getName();
			final Object value = accessor.getValue(instance);

			final PropertyAccessor<P, Object> readAccessor = pm.getFieldByName(name).getAccessor();
			if (readAccessor != null) {
				readAccessor.setValue(p, value);
			}
		}

		return p;
	}

	@Override
	public void setValue(final T instance, final P value) {
		for(final FieldMetadata<P, ?, ?> fmd : pm.getFields()) {
			final Object pmFieldValue = fmd.getAccessor().getValue(value);
			final PropertyAccessor<T, Object> writeAccessor = name2accessors.get(fmd.getName());
			if (writeAccessor != null) {
				writeAccessor.setValue(instance, pmFieldValue);
			}
		}
	}
}
