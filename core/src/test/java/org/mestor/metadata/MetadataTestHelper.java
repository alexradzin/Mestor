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

import org.mestor.context.EntityContext;
import org.mestor.util.ReflectiveBean;
import org.mockito.Mockito;

public class MetadataTestHelper {
	public final EntityContext ctx = Mockito.mock(EntityContext.class);
	
	public MetadataTestHelper() {
	}
	

	
	@SuppressWarnings({"unchecked", "rawtypes"})
	public final <T> EntityMetadata<T> createMetadata(Class<T> clazz, String schemaName, String tableName, FieldMetadata<T, ?, ?> pk, FieldMetadata ... fieldsMetadata) {
		EntityMetadata<T> emd = new EntityMetadata<>(clazz);
		emd.setEntityName(clazz.getSimpleName());
		emd.setTableName(tableName);
		emd.setSchemaName(schemaName);
		
		if (pk != null) {
			pk.setKey(true);
		}
		emd.setPrimaryKey(pk);
		
		for (FieldMetadata<T, Object, Object> field : fieldsMetadata) {
			emd.addField(field);
		}
		
		return emd;
	}
	

	public <T, F, C> FieldMetadata<T, F, C> createFieldMetadata(Class<T> classType, Class<F> type, String name) {
		return createFieldMetadata(classType, type, name, name, false);
	}
	
	public <T, F, C> FieldMetadata<T, F, C> createFieldMetadata(Class<T> classType, Class<F> type, Class<?>[] generics, String name) {
		return createFieldMetadata(classType, type, generics, name, name, false);
	}
	
	public <T, F, C> FieldMetadata<T, F, C> createFieldMetadata(Class<T> classType, Class<F> type, String name, String column, boolean key) {
		return createFieldMetadata(classType, type, null, name, column, key);
	}

	
	public <T, F, C> FieldMetadata<T, F, C> createFieldMetadata(Class<T> classType, Class<F> type, Class<?>[] generics, String name, String column, boolean key) {
		return createFieldMetadata(classType, type, generics, generics, name, column, key);
	}
	
	
	public <T, F, C> FieldMetadata<T, F, C> createFieldMetadata(Class<T> classType, Class<F> type, Class<?>[] generics, Class<?>[] columnGenerics, String name, String column, boolean key) {
		FieldMetadata<T, F, C> field = new FieldMetadata<>(
				classType, type, name, 
				ReflectiveBean.getField(classType, name),
				ReflectiveBean.getGetter(classType, name),
				ReflectiveBean.getSetter(classType, type, name)
		);
		field.setColumn(column);
		field.setKey(key);
		if (generics != null) {
			field.setGenericTypes(Arrays.asList(generics));
			field.setColumnGenericTypes(Arrays.asList(columnGenerics));
		}
		return field;
	}
	
}
