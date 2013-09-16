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
import java.util.Map;
import java.util.Map.Entry;

import org.mestor.reflection.PropertyAccessor;

public class EntityMetadata<E> {
	private Class<E> entityType;
	private String entityName;
//	private Class<E> primaryKeyType;
	private FieldMetadata<E, ? extends Object> primaryKey;

	private String tableName;
	private String schemaName;
	
	private Map<String, FieldMetadata<E, Object>> fields = new LinkedHashMap<>();
	private Map<String, Class<?>[]> fieldTypes = new LinkedHashMap<>();
	private Collection<IndexMetadata<E>> indexes = new ArrayList<>();
	
	private Map<Method, String> getter2fieldName = new HashMap<>();
	private Map<Method, String> setter2fieldName = new HashMap<>();

	
	public EntityMetadata(Class<E> entityClass) {
		this.entityType = entityClass;
	}

	public EntityMetadata() {
		// default constructor
	}

	
//	public E create() {
//		return entityType.newInstance();
//	}
	
	

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
	
	
	public Map<String, FieldMetadata<E, Object>> getFields() {
		return Collections.unmodifiableMap(fields);
	}

	public Map<String, Class<?>[]> getFieldTypes() {
		return Collections.unmodifiableMap(fieldTypes);
	}

	public void setFields(Map<String, FieldMetadata<E, Object>> fields) {
		this.fields = fields;
		for (Entry<String, FieldMetadata<E, Object>> e : fields.entrySet()) {
			FieldMetadata<E, ? extends Object> fmeta = e.getValue();
			PropertyAccessor<E, ?> accessor = fmeta.getAccessor();
			String name = fmeta.getName();
			putAccessorToFieldName(getter2fieldName, accessor.getGetter(), name);
			putAccessorToFieldName(setter2fieldName, accessor.getSetter(), name);
			
			//TODO: add support of collection element type defined in OneToMany and ManyToMany annotations
			fieldTypes.put(name, new Class<?>[] {fmeta.getType()});
		}
	}
	
	public String getFieldNameByGetter(Method getter) {
		return getter2fieldName.get(getter);
	}

	public String getFieldNameBySetter(Method setter) {
		return setter2fieldName.get(setter);
	}

	public Collection<String> getFieldNamesByType(Class<?> type) {
		Collection<String> fieldNames = new ArrayList<>();
		for(FieldMetadata<E, ? extends Object> fmd : fields.values()) {
			if(type.isAssignableFrom(fmd.getClassType())) {
				fieldNames.add(fmd.getName());
			}
		}

		return fieldNames;
	}
	
	
	private void putAccessorToFieldName(Map<Method, String> map, Method accessor, String name) {
		if (accessor != null) {
			map.put(accessor, name);
		}
	}
	
	public FieldMetadata<E, Object> getField(String name) {
		return fields.get(name);
	}

	public Collection<IndexMetadata<E>> getIndexes() {
		return Collections.unmodifiableCollection(indexes);
	}

	public void setIndexes(Collection<IndexMetadata<E>> indexes) {
		this.indexes = indexes;
	}


	public void copy(E from, E to) {
		for (FieldMetadata<E, ? extends Object> fmd : getFields().values()) {
			@SuppressWarnings("unchecked")
			PropertyAccessor<E, Object> pa = (PropertyAccessor<E, Object>)fmd.getAccessor();
			Object value = fmd.getAccessor().getValue(from);
			pa.setValue(to, value);
		}
	}
	
}
