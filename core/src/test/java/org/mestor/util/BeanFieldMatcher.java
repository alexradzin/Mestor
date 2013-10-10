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

import java.util.Arrays;

import org.hamcrest.CustomMatcher;
import org.mestor.metadata.EntityMetadata;
import org.mestor.metadata.jpa.BeanMetadataFactory;

import com.google.common.base.Objects;

public class BeanFieldMatcher<T> extends CustomMatcher<T> {
	private final Class<T> clazz; 
	private final String[] fields;
	private final Object[] values;
	private final EntityMetadata<T> emd;
	

	public BeanFieldMatcher(Class<T> clazz, String field, Object value) {
		this(clazz, new String[] {field}, new Object[] {value});
	}
	
	
	public BeanFieldMatcher(Class<T> clazz, String[] fields, Object[] values) {
		super(createDescription(clazz, fields));
		this.clazz = clazz;
		this.fields = fields;
		this.values = values;
		emd = new BeanMetadataFactory().create(clazz);
	}
	
	private static String createDescription(Class<?> clazz, String ... fields) {
		return clazz.getName() + "@" + Arrays.toString(fields);
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean matches(Object item) {
		return item == null ? false : clazz.isAssignableFrom(item.getClass()) ? matchesImpl((T)item) : false;
	}

	public boolean matchesImpl(T instance) {
		for (int i = 0; i < fields.length; i++) {
			Object actual = emd.getFieldByName(fields[i]).getAccessor().getValue(instance);
			if (!Objects.equal(values[i], actual)) {
				return false;
			}
		}
		return true;
	}
}
