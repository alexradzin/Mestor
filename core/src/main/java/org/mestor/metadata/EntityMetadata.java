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
	private FieldMetadata<E, Object, Object> primaryKey;
	private FieldMetadata<E, Object, Object> discrimintor;
	private FieldMetadata<E, Object, Object> joiner;

	private String tableName;
	private String schemaName;
	
	private Collection<FieldMetadata<E, Object, Object>> fields = new ArrayList<>();
	private Map<String, FieldMetadata<E, ?, ?>> fieldColumns = new LinkedHashMap<>();
	private Map<String, FieldMetadata<E, ?, ?>> fieldNames = new LinkedHashMap<>();
	private Map<String, Class<?>[]> fieldTypes = new LinkedHashMap<>();
	private Map<String, Class<?>[]> columnTypes = new LinkedHashMap<>();
	private Collection<IndexMetadata<E>> indexes = new ArrayList<>();
	
	private Map<Method, FieldMetadata<E, ?, ?>> getter2field = new HashMap<>();
	private Map<Method, FieldMetadata<E, ?, ?>> setter2field = new HashMap<>();

	
	
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


	@SuppressWarnings("unchecked")
	public <F,C> void setPrimaryKey(FieldMetadata<E, F, C> primaryKey) {
		this.primaryKey = (FieldMetadata<E, Object, Object>)primaryKey;
	}

	public FieldMetadata<E, Object, Object> getPrimaryKey() {
		return this.primaryKey;
	}

	public FieldMetadata<E, Object, Object> getDiscrimintor() {
		return this.discrimintor;
	}

	public FieldMetadata<E, Object, Object> getJoiner() {
		return this.joiner;
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
	
	
	public Collection<FieldMetadata<E, Object, Object>> getFields() {
		return Collections.unmodifiableCollection(fields);
	}

	@SuppressWarnings("unchecked")
	public <C> Class<C>[] getFieldTypes(String columnName) {
		return (Class<C>[])fieldTypes.get(columnName);
	}

	/**
	 * Retrieves type of specified column. The type is typically identified by 
	 * one class. However to identify list of set we need 2 classes (list and the list element).
	 * For maps we need 3 classes (map itself, key and value). 
	 * This is the reason that this method returns an array of {@link Class}.
	 * @param columnName
	 * @return array of {@link Class} objects that identify the column type
	 */
	@SuppressWarnings("unchecked")
	public <C> Class<C>[] getColumnTypes(String columnName) {
		return (Class<C>[])columnTypes.get(columnName);
	}

	public <F, C> void addField(FieldMetadata<E, F, C> fmeta) {
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
		
		List<Class<?>> cTypes = new ArrayList<>();
		cTypes.add(fmeta.getColumnType());
		cTypes.addAll(fmeta.getColumnGenericTypes());
		columnTypes.put(column, cTypes.toArray(new Class[0]));
		
		fieldColumns.put(column, fmeta);
		fieldNames.put(fmeta.getName(), fmeta);
		
		@SuppressWarnings("unchecked")
		FieldMetadata<E, Object, Object> fmd = (FieldMetadata<E, Object, Object>)fmeta;
		fields.add(fmd);
		
		if (fmd.isDiscriminator()) {
			discrimintor = fmd;
		}
		if (fmd.isJoiner()) {
			joiner = fmd;
		}
	}
	
	
	@SuppressWarnings("unchecked")
	public <F, C> FieldMetadata<E, F, C> getFieldByGetter(Method getter) {
		return (FieldMetadata<E, F, C>)getter2field.get(getter);
	}

	@SuppressWarnings("unchecked")
	public <F, C> FieldMetadata<E, F, C> getFieldBySetter(Method setter) {
		return (FieldMetadata<E, F, C>)setter2field.get(setter);
	}


	public Collection<String> getFieldNamesByType(Class<?> type) {
		Collection<String> fieldNames = new ArrayList<>();
		for(FieldMetadata<E, ? extends Object, ? extends Object> fmd : fields) {
			if(type.isAssignableFrom(fmd.getClassType())) {
				fieldNames.add(fmd.getName());
			}
		}

		return fieldNames;
	}
	
	
	@SuppressWarnings("unchecked")
	public <F, C> FieldMetadata<E, F, C> getField(String column) {
		return (FieldMetadata<E, F, C>)this.fieldColumns.get(column);
	}

	@SuppressWarnings("unchecked")
	public <F, C> FieldMetadata<E, F, C> getFieldByName(String name) {
		return (FieldMetadata<E, F, C>)this.fieldNames.get(name);
	}

	public Collection<IndexMetadata<E>> getIndexes() {
		return Collections.unmodifiableCollection(indexes);
	}

	public void addAllIndexes(Collection<IndexMetadata<E>> indexes) {
		for(IndexMetadata<E> index : indexes) {
			addIndex(index);
		}
	}
	
	public void addIndex(IndexMetadata<E> index) {
		this.indexes.add(index);
	}
	
	
	public <F, C> void addIndex(FieldMetadata<E, F, C> fmd) {
		String indexName = getTableName() + "_" + fmd.getColumn() + "_index";
		addIndex(new IndexMetadata<>(fmd.getClassType(), indexName, fmd));
	}

	public void copy(E from, E to) {
		for (FieldMetadata<E, ? extends Object, ? extends Object> fmd : getFields()) {
			@SuppressWarnings("unchecked")
			PropertyAccessor<E, Object> pa = (PropertyAccessor<E, Object>)fmd.getAccessor();
			Object value = fmd.getAccessor().getValue(from);
			pa.setValue(to, value);
		}
	}
	
}
