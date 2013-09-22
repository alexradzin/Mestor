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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.mestor.reflection.PropertyAccessor;

public class EntityMetadata<E> {
	private Class<E> entityType;
	private String entityName;
	private FieldMetadata<E, ? extends Object> primaryKey;

	private String tableName;
	private String schemaName;
	
	private Collection<FieldMetadata<E, Object>> fields = new ArrayList<>();
	private Map<String, FieldMetadata<E, ?>> fieldColumns = new LinkedHashMap<>();
	private Map<String, Class<?>[]> fieldTypes = new LinkedHashMap<>();
	private Collection<IndexMetadata<E>> indexes = new ArrayList<>();
	
	private Map<Method, FieldMetadata<E, ?>> getter2field = new HashMap<>();
	private Map<Method, FieldMetadata<E, ?>> setter2field = new HashMap<>();

	
	
	public EntityMetadata(Class<E> entityClass) {
		this.entityType = entityClass;
	}

	public EntityMetadata() {
		// default constructor
	}

	
	public Class<E> getEntityType() {
		return entityType;
	}


	public void setEntityType(Class<E> entityType) {
		this.entityType = entityType;
	}


	
	
	public String getEntityName() {
		return entityName;
	}

	public void setEntityName(String entityName) {
		this.entityName = entityName;
	}


	public void setPrimaryKey(FieldMetadata<E, ? extends Object> primaryKey) {
		this.primaryKey = primaryKey;
	}

	public FieldMetadata<E, ? extends Object> getPrimaryKey() {
		return this.primaryKey;
	}

	public String getTableName() {
		return tableName;
	}


	public void setTableName(String tableName) {
		this.tableName = tableName;
	}


	public String getSchemaName() {
		return schemaName;
	}


	public void setSchemaName(String schemaName) {
		this.schemaName = schemaName;
	}
	
	
	public Collection<FieldMetadata<E, Object>> getFields() {
		return Collections.unmodifiableCollection(fields);
	}

	public Map<String, Class<?>[]> getFieldTypes() {
		return Collections.unmodifiableMap(fieldTypes);
	}

	public <F> void addField(FieldMetadata<E, F> fmeta) {
		final PropertyAccessor<E, F> accessor = fmeta.getAccessor();
		final Method getter = accessor.getGetter();
		if (getter != null) {
			getter2field.put(getter, fmeta);
		}
		final Method setter = accessor.getSetter();
		if (setter != null) {
			setter2field.put(setter, fmeta);
		}
		
		final String column = fmeta.getColumn();
		
		List<Class<?>> types = new ArrayList<>();
		types.add(fmeta.getType());
		types.addAll(fmeta.getGenericTypes());
		fieldTypes.put(column, types.toArray(new Class[0]));
		fieldColumns.put(column, fmeta);
		
		@SuppressWarnings("unchecked")
		FieldMetadata<E, Object> fmd = (FieldMetadata<E, Object>)fmeta;
		fields.add(fmd);
	}
	
	
	@SuppressWarnings("unchecked")
	public <F> FieldMetadata<E, F> getFieldByGetter(Method getter) {
		return (FieldMetadata<E, F>)getter2field.get(getter);
	}

	@SuppressWarnings("unchecked")
	public <F> FieldMetadata<E, F> getFieldBySetter(Method setter) {
		return (FieldMetadata<E, F>)setter2field.get(setter);
	}


	public Collection<String> getFieldNamesByType(Class<?> type) {
		Collection<String> fieldNames = new ArrayList<>();
		for(FieldMetadata<E, ? extends Object> fmd : fields) {
			if(type.isAssignableFrom(fmd.getClassType())) {
				fieldNames.add(fmd.getName());
			}
		}

		return fieldNames;
	}
	
	
	@SuppressWarnings("unchecked")
	public <F> FieldMetadata<E, F> getField(String column) {
		return (FieldMetadata<E, F>)this.fieldColumns.get(column);
	}

	public Collection<IndexMetadata<E>> getIndexes() {
		return Collections.unmodifiableCollection(indexes);
	}

	public void setIndexes(Collection<IndexMetadata<E>> indexes) {
		this.indexes = indexes;
	}


	public void copy(E from, E to) {
		for (FieldMetadata<E, ? extends Object> fmd : getFields()) {
			@SuppressWarnings("unchecked")
			PropertyAccessor<E, Object> pa = (PropertyAccessor<E, Object>)fmd.getAccessor();
			Object value = fmd.getAccessor().getValue(from);
			pa.setValue(to, value);
		}
	}
	
}
