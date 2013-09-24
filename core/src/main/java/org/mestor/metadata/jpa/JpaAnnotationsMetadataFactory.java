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

package org.mestor.metadata.jpa;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.mestor.metadata.EntityMetadata;
import org.mestor.metadata.FieldMetadata;
import org.mestor.reflection.CompositePropertyAccessor;
import org.mestor.reflection.FieldAccessor;
import org.mestor.reflection.MethodAccessor;
import org.mestor.reflection.PropertyAccessor;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;

public class JpaAnnotationsMetadataFactory extends BeanMetadataFactory {
	private NamingStrategy namingStrategy;

//	public JpaAnnotationsMetadataFactory(final NamingStrategy namingStrategy) {
//		this.namingStrategy = namingStrategy;
//	}
	
	public JpaAnnotationsMetadataFactory() {
	}

	public void setNamingStrategy(NamingStrategy namingStrategy) {
		this.namingStrategy = namingStrategy;
	}
	

	@Override
	public <T> EntityMetadata<T> create(final Class<T> clazz) {
		final Entity entity = clazz.getAnnotation(Entity.class);
		if (entity == null) {
			return null;
		}
		
		EntityMetadata<T> emeta = new EntityMetadata<>(clazz);
		
		// discover entity name
		emeta.setEntityName(extractName(entity, namingStrategy.getEntityName(clazz)));
		
		Table table = clazz.getAnnotation(Table.class);
		if (table != null) {
			emeta.setTableName(extractName(table, namingStrategy.getTableName(clazz)));
			String schema = table.schema();
			if (Strings.isNullOrEmpty(schema)) {
				schema = this.getSchema();
			}
			
			emeta.setSchemaName(schema); 
		}

		// name to metadata
		Map<String, FieldMetadata<T, Object, Object>> fields = new LinkedHashMap<>();
		
		for (Field f : FieldAccessor.getFields(clazz)) {
			if (isTransient(f)) {
				continue;
			}
			
			@SuppressWarnings("unchecked")
			Class<Object> type = (Class<Object>)f.getType();
			final String name = getFieldName(f);
			FieldMetadata<T, Object, Object> fmeta = create(clazz, type, name);
			
			fmeta.setField(f);
			initMeta(fmeta, f, name, clazz, type);
			
			fields.put(name, fmeta);
			
		}
		
		for (Method m : clazz.getMethods()) {
			if(!MethodAccessor.isGetter(m)) {
				continue;
			}

			String fieldName = getFieldName(m);
			FieldMetadata<T, Object, Object> fmeta = fields.get(fieldName);
			
			
			if (isTransient(m)) {
				if (fmeta != null) {
					fields.remove(fieldName);
				}
				continue;
			}
			
			if (fmeta == null) {
				@SuppressWarnings("unchecked")
				Class<Object> type = (Class<Object>)m.getReturnType();
				fmeta = create(clazz, type, fieldName);
				initMeta(fmeta, m, fieldName, clazz, type);
			}
			fmeta.setGetter(m);
		}
		
		for (Method m : clazz.getMethods()) {
			if(!MethodAccessor.isSetter(m)) {
				continue;
			}
			String fieldName = getFieldName(m);
			FieldMetadata<T, Object, Object> fmeta = fields.get(fieldName);
			
			if (fmeta == null) {
				continue;
			}
			fmeta.setSetter(m);
		}

		
		Collection<String> primaryKeyFields = new ArrayList<>();
		Collection<PropertyAccessor<T, ? extends Object>> primaryKeyAccessors = new ArrayList<>();
		
		for (Entry<String, FieldMetadata<T, Object, Object>> entry : fields.entrySet()) {
			FieldMetadata<T, ? extends Object, ? extends Object> fmeta = entry.getValue();
					
			if (fmeta.isKey()) {
				primaryKeyFields.add(entry.getKey());
				primaryKeyAccessors.add(fmeta.getAccessor());
			}
		}

		
		if (primaryKeyFields.isEmpty()) {
			throw new IllegalArgumentException("Entity " + clazz + " does not have primary key");
		}
		
		
		for (FieldMetadata<T, ?, ?> fmd : fields.values()) {
			emeta.addField(fmd);
		}


		emeta.setPrimaryKey(fields.get(primaryKeyFields.iterator().next()));
		
		if (primaryKeyFields.size() > 1) {
			final IdClass idClass = clazz.getAnnotation(IdClass.class);
			if (idClass == null) {
				throw new IllegalArgumentException("Entity " + clazz + " has ");
			}
			
			@SuppressWarnings("unchecked")
			Class<Object> idClazz = idClass.value();
			
			String name = Joiner.on("_").join(primaryKeyFields);
			
			@SuppressWarnings("unchecked")
			FieldMetadata<T, Object, Object> keyMetadata = new FieldMetadata<>(clazz, idClass.value(), name);
			
			@SuppressWarnings("unchecked")
			PropertyAccessor<T, Object>[] primaryKeyAccessorsArr = primaryKeyAccessors.toArray(new PropertyAccessor[primaryKeyAccessors.size()]); 
			keyMetadata.setAccessor(new CompositePropertyAccessor<T, Object>(clazz, idClazz, name, primaryKeyAccessorsArr));
			
			emeta.setPrimaryKey(keyMetadata);
		}
		
		// TODO indexes
		
		
		return emeta;
	}

	

	
	private String extractName(Object nameHolder, String defaultValue) {
		String name;
		try {
			name = nameHolder == null ? null : (String)nameHolder.getClass().getMethod("name").invoke(nameHolder);
			return extractParameter(name, defaultValue);
		} catch (ReflectiveOperationException e) {
			throw new IllegalArgumentException(nameHolder.getClass() + " does not provide method name()");
		}
	}
	
	
	
	
	private String extractParameter(String value, String defaultValue) {
		return Strings.isNullOrEmpty(value) ? defaultValue : value;
	}
	

	
	
	private <T, F, C> void initMeta(FieldMetadata<T, F, C> fmeta, AccessibleObject ao, String memberName, Class<T> clazz, Class<F> memberType) {
		fmeta.setColumn(extractName(ao.getAnnotation(Column.class), namingStrategy.getColumnName(ao)));
		fmeta.setKey(ao.getAnnotation(Id.class) != null);
		
		boolean nullable = ao.getAnnotation(Nullable.class) != null;
		
		if (nullable && memberType.isPrimitive()) {
			throw new IllegalArgumentException("Primitive field " + memberName + " cannot be nullable");
		}
		
		fmeta.setNullable(nullable || !memberType.isPrimitive());
	}
	
	
	private boolean isTransient(AccessibleObject ao) {
		int mod = ((Member)ao).getModifiers();
		Transient tr = ao.getAnnotation(Transient.class);
		return Modifier.isTransient(mod) || tr != null;
	}
}
