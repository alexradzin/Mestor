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

package org.mestor.metadata;

import java.util.Arrays;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Collections2;

public class IndexMetadata<T> {
	private final Class<T> type;
	private final String name;
	private final FieldMetadata<T, ? extends Object, ? extends Object>[] fields;



	@SuppressWarnings("unchecked")
	public IndexMetadata(final Class<T> type, final String name, final FieldMetadata<T, ? extends Object, ? extends Object> field) {
		this(type, name, new FieldMetadata[] {field});
	}


	public IndexMetadata(final Class<T> type, final String name, final FieldMetadata<T, ? extends Object, ? extends Object>[] fields) {
		this.type = type;
		this.fields = Arrays.copyOf(fields, fields.length);
		this.name = autoName(name);
	}

	@SuppressWarnings("unchecked")
	public IndexMetadata(final EntityMetadata<T> entityMetadata, final String name, final String[] columnNames) {
		this.type = entityMetadata.getEntityType();
		fields = new FieldMetadata[columnNames.length];

		for (int i = 0; i < columnNames.length; i++) {
			final FieldMetadata<T, ? extends Object, ? extends Object> fmd = entityMetadata.getField(columnNames[i]);
			if (fmd == null) {
				throw new IllegalArgumentException("Index " + name + " uses unknown field name " + columnNames[i]);
			}
			fields[i] = fmd;
		}

		this.name = autoName(name);
	}

	private String autoName(final String passedName) {
		if (passedName == null || "".equals(passedName)) {
			return Joiner.on("_").join(Collections2.transform(Arrays.asList(fields), new Function<FieldMetadata<T, ?, ?>, String>(){
				@Override
				public String apply(final FieldMetadata<T, ?, ?> fmd) {
					return fmd.getColumn();
				}
			}));
		}

		if (!passedName.matches("^[A-Za-z0-9_]+$")) {
			throw new IllegalArgumentException("Wrong index name " + passedName);
		}

		return passedName;
	}

	public Class<T> getType() {
		return type;
	}


	public String getName() {
		return name;
	}


	public FieldMetadata<T, ? extends Object, ? extends Object>[] getField() {
		return fields;
	}

	public String[] getFieldNames() {
		if (fields == null) {
			return null;
		}
		final String[] names = new String[fields.length];
		for (int i = 0; i < fields.length; i++) {
			names[i] = fields[i].getName();
		}
		return names;
	}

	public String[] getColumnNames() {
		if (fields == null) {
			return null;
		}
		final String[] names = new String[fields.length];
		for (int i = 0; i < fields.length; i++) {
			names[i] = fields[i].getColumn();
		}
		return names;
	}


}
