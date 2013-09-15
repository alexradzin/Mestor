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

public class IndexMetadata<T> {
	private final Class<T> type;
	private final String name;
	private final FieldMetadata<T, Object>[] fields;


	public IndexMetadata(Class<T> type, String name, FieldMetadata<T, Object>[] fields) {
		this.type = type;
		this.name = name;
		this.fields = Arrays.copyOf(fields, fields.length);
	}
	
	@SuppressWarnings("unchecked")
	public IndexMetadata(EntityMetadata<T> entityMetadata, String name, String[] fieldNames) {
		this.type = entityMetadata.getEntityType();
		this.name = name;
		
		
		fields = new FieldMetadata[fieldNames.length];
		
		for (int i = 0; i < fieldNames.length; i++) {
			FieldMetadata<T, Object> fmd = entityMetadata.getField(fieldNames[i]);
			if (fmd == null) {
				throw new IllegalArgumentException("Index " + name + " uses unknown field name " + fieldNames[i]);
			}
			fields[i] = fmd;
		}
	}


	public Class<T> getType() {
		return type;
	}


	public String getName() {
		return name;
	}


	public FieldMetadata<T, Object>[] getFields() {
		return fields;
	}

	public String[] getFieldNames() {
		if (fields == null) {
			return null;
		}
		String[] names = new String[fields.length];
		for (int i = 0; i < fields.length; i++) {
			names[i] = fields[i].getName();
		}
		return names;
	}
	
	
}
