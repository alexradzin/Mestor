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
	
	private final Collection<FieldMetadata<E, Object, Object>> fields = new ArrayList<>();
	private final Map<String, FieldMetadata<E, ?, ?>> fieldColumns = new LinkedHashMap<>();
	private final Map<String, FieldMetadata<E, ?, ?>> fieldNames = new LinkedHashMap<>();
	private final Map<String, Class<?>[]> fieldTypes = new LinkedHashMap<>();
	private final Map<String, Class<?>[]> columnTypes = new LinkedHashMap<>();
	private final Collection<IndexMetadata<E>> indexes = new ArrayList<>();
	
	private final Map<Method, FieldMetadata<E, ?, ?>> getter2field = new HashMap<>();
	private final Map<Method, FieldMetadata<E, ?, ?>> setter2field = new HashMap<>();

	
	
	public EntityMetadata(final Class<E> entityClass) {
		this.entityType = entityClass;
	}

	public EntityMetadata() {
		// default constructor
	}

	
	public Class<E> getEntityType() {
		return entityType;
	}


	public void setEntityType(final Class<E> entityType) {
		this.entityType = entityType;
	}


	
	
	public String getEntityName() {
		return entityName;
	}

	public void setEntityName(final String entityName) {
		this.entityName = entityName;
	}


	@SuppressWarnings("unchecked")
	public <F,C> void setPrimaryKey(final FieldMetadata<E, F, C> primaryKey) {
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


	public void setTableName(final String tableName) {
		this.tableName = tableName;
	}


	public String getSchemaName() {
		return schemaName;
	}


	public void setSchemaName(final String schemaName) {
		this.schemaName = schemaName;
	}
	
	
	public Collection<FieldMetadata<E, Object, Object>> getFields() {
		return Collections.unmodifiableCollection(fields);
	}

	@SuppressWarnings("unchecked")
	public <C> Class<C>[] getFieldTypes(final String columnName) {
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
	public <C> Class<C>[] getColumnTypes(final String columnName) {
		return (Class<C>[])columnTypes.get(columnName);
	}

	public <F, C> void addField(final FieldMetadata<E, F, C> fmeta) {
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
		if(column != null){
			final List<Class<?>> types = new ArrayList<>();
			types.add(fmeta.getType());
			types.addAll(fmeta.getGenericTypes());
			fieldTypes.put(column, types.toArray(new Class[0]));
			
			final List<Class<?>> cTypes = new ArrayList<>();
			cTypes.add(fmeta.getColumnType());
			cTypes.addAll(fmeta.getColumnGenericTypes());
			columnTypes.put(column, cTypes.toArray(new Class[0]));
			
			fieldColumns.put(column, fmeta);
		}
		fieldNames.put(fmeta.getName(), fmeta);
		
		@SuppressWarnings("unchecked")
		final FieldMetadata<E, Object, Object> fmd = (FieldMetadata<E, Object, Object>)fmeta;
		fields.add(fmd);
		
		if (fmd.isDiscriminator()) {
			discrimintor = fmd;
		}
		if (fmd.isJoiner()) {
			joiner = fmd;
		}
	}
	
	
	public void addAllFields(final Collection<FieldMetadata<E, ?, ?>> fmetas) {
		for(final FieldMetadata<E, ?, ?> fmeta : fmetas) {
			addField(fmeta);
		}
	}
	
	
	@SuppressWarnings("unchecked")
	public <F, C> FieldMetadata<E, F, C> getFieldByGetter(final Method getter) {
		return (FieldMetadata<E, F, C>)getter2field.get(getter);
	}

	@SuppressWarnings("unchecked")
	public <F, C> FieldMetadata<E, F, C> getFieldBySetter(final Method setter) {
		return (FieldMetadata<E, F, C>)setter2field.get(setter);
	}


	public Collection<String> getFieldNamesByType(final Class<?> type) {
		final Collection<String> fieldNames = new ArrayList<>();
		for(final FieldMetadata<E, ? extends Object, ? extends Object> fmd : fields) {
			if(type.isAssignableFrom(fmd.getType())) {
				fieldNames.add(fmd.getName());
			}
		}

		return fieldNames;
	}
	
	
	@SuppressWarnings("unchecked")
	public <F, C> FieldMetadata<E, F, C> getField(final String column) {
		return (FieldMetadata<E, F, C>)this.fieldColumns.get(column);
	}

	@SuppressWarnings("unchecked")
	public <F, C> FieldMetadata<E, F, C> getFieldByName(final String name) {
		return (FieldMetadata<E, F, C>)this.fieldNames.get(name);
	}

	public Collection<IndexMetadata<E>> getIndexes() {
		return Collections.unmodifiableCollection(indexes);
	}

	public void addAllIndexes(final Collection<IndexMetadata<E>> indexes) {
		for(final IndexMetadata<E> index : indexes) {
			addIndex(index);
		}
	}
	
	public void addIndex(final IndexMetadata<E> index) {
		this.indexes.add(index);
	}
	
	
	public <F, C> void addIndex(final FieldMetadata<E, F, C> fmd) {
		final String indexName = getTableName() + "_" + fmd.getColumn() + "_index";
		addIndex(new IndexMetadata<>(fmd.getClassType(), indexName, fmd));
	}

	public void copy(final E from, final E to) {
		for (final FieldMetadata<E, ? extends Object, ? extends Object> fmd : getFields()) {
			@SuppressWarnings("unchecked")
			final
			PropertyAccessor<E, Object> pa = (PropertyAccessor<E, Object>)fmd.getAccessor();
			final Object value = fmd.getAccessor().getValue(from);
			pa.setValue(to, value);
		}
	}
	
}
