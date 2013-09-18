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

import static org.mestor.reflection.ClassAccessor.invoke;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.mestor.metadata.EntityMetadata;
import org.mestor.metadata.FieldMetadata;
import org.mestor.metadata.IndexMetadata;
import org.mestor.metadata.MetadataFactory;
import org.mestor.metadata.index.IndexAnnotation;
import org.mestor.metadata.index.IndexAnnotationContainer;
import org.mestor.metadata.index.IndexAnnotations;
import org.mestor.reflection.ClassAccessor;
import org.mestor.reflection.FieldAccessor;
import org.mestor.reflection.MethodAccessor;

public class BeanMetadataFactory implements MetadataFactory {
	private final IndexAnnotations indexAnnotations;
	private final Map<Class<? extends Annotation>, IndexAnnotation> indexDefs = new HashMap<>();
	private final Map<Class<? extends Annotation>, IndexAnnotationContainer> indexContainerDefs = new HashMap<>();
	private String schema;
	
	public BeanMetadataFactory() {
		String indexAnnotationPackage = IndexAnnotations.class.getPackage().getName();
		//TODO make this configurable
		String indexAnnotationConfigPath = indexAnnotationPackage.replace('.', '/') + "/" +  "index-annotations.xml";
		InputStream indexConfig = getClass().getResourceAsStream(indexAnnotationConfigPath);
		if(indexConfig == null) {
			indexAnnotations = null;
			return;
		}
		
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(indexAnnotationPackage);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			indexAnnotations = (IndexAnnotations)jaxbUnmarshaller.unmarshal(indexConfig);
		} catch (JAXBException e) {
			throw new IllegalArgumentException(e);
		}
		
		for (Object def : indexAnnotations.getIndexDefs()) {
			// although usage of instanceof is ugly I use it here because it is easier at the moment.
			if (def instanceof IndexAnnotation) {
				IndexAnnotation ia = (IndexAnnotation)def;
				indexDefs.put(ClassAccessor.<Annotation>forName(ia.getClassName()), ia);
			} else if (def instanceof IndexAnnotationContainer) {
				IndexAnnotationContainer iac = (IndexAnnotationContainer)def;
				indexContainerDefs.put(ClassAccessor.<Annotation>forName(iac.getClassName()), iac);
			} else {
				throw new UnsupportedOperationException("Index configuration " + def.getClass() + " is unsupported");
			}
		}
		
		
	}
	
	
	
	@Override
	public <T> EntityMetadata<T> create(Class<T> clazz) {
		EntityMetadata<T> emeta = new EntityMetadata<>(clazz);
		
		Map<String, FieldMetadata<T, Object>> fields = new LinkedHashMap<>();
		
		for (Field f : FieldAccessor.getFields(clazz)) {
			String name = getFieldName(f);
			
			@SuppressWarnings("unchecked")
			FieldMetadata<T, Object> fmeta = create(clazz, (Class<Object>)f.getType(), name);
			fmeta.setField(f);
			
			fields.put(name, fmeta);
		}
		
		
		for (Method m : clazz.getMethods()) {
			if(!(MethodAccessor.isGetter(m) || MethodAccessor.isGetter(m))) {
				continue;
			}
			
			String fieldName = getFieldName(m);
			FieldMetadata<T, Object> fmeta = fields.get(fieldName);
			
			if (fmeta == null) {
				@SuppressWarnings("unchecked")
				Class<Object> type = (Class<Object>)m.getReturnType();
				fmeta = create(clazz, type, fieldName);
			}
			
			if(MethodAccessor.isGetter(m)) {
				fmeta.setGetter(m);
			}
			if(MethodAccessor.isSetter(m)) {
				fmeta.setSetter(m);
			}
		}
		

		emeta.setIndexes(findIndexes(emeta));
		
		return emeta;
	}

	
	private <T> Collection<IndexMetadata<T>> findIndexes(EntityMetadata<T> entityMetadata) {
		Map<String, IndexMetadata<T>> indexes = new LinkedHashMap<>();
		Class<T> entityType = entityMetadata.getEntityType();
		
		// process class level index annotations
		findIndexes(entityMetadata, entityType, null, indexes);
		
		// process field and method level index annotations
		
		for (FieldMetadata<T, Object> fmeta : entityMetadata.getFields()) {
			findIndexes(entityMetadata, fmeta.getAccessor().getField(), fmeta.getName(), indexes);
			findIndexes(entityMetadata, fmeta.getAccessor().getGetter(), fmeta.getName(), indexes);
		}
		
		return indexes.values();
	}


	private <T> void findIndexes(EntityMetadata<T> entityMetadata, AnnotatedElement ae, String fieldName, Map<String, IndexMetadata<T>> indexes) {
		if (ae == null) {
			return;
		}
		for(Annotation a : ae.getAnnotations()) {
			processIndexAnnotation(entityMetadata, a, fieldName, indexes);
			processIndexContainerAnnotation(entityMetadata, a, fieldName, indexes);
		}
	}
	
	

	private <T> void processIndexAnnotation(EntityMetadata<T> entityMetadata, Annotation a, String fieldName, Map<String, IndexMetadata<T>> indexes) {
		Class<? extends Annotation> annotationType = a.annotationType();
		IndexAnnotation indexDef = indexDefs.get(annotationType);
		if (indexDef == null) {
			return;
		}
		
		String name = invoke(annotationType, indexDef.getName(), null, String.class, a, null);
		String[] indexedFieldNames = invoke(annotationType, indexDef.getColumnNames(), null, String[].class, a, null);
		
		if (fieldName != null) {
			if (name == null || "".equals(name)) {
				name = fieldName;
			}
			if (indexedFieldNames == null || indexedFieldNames.length == 0) {
				indexedFieldNames = new String[] {name};
			}
			
			IndexMetadata<T> existingIndex = indexes.get(name);
			if (existingIndex != null) {
				Collection<String> allNames = new LinkedHashSet<String>(Arrays.asList(existingIndex.getFieldNames()));
				allNames.addAll(Arrays.asList(indexedFieldNames));
				IndexMetadata<T> updatedIndex = create(entityMetadata, name, allNames.toArray(new String[0]));
				indexes.put(name, updatedIndex);
			}
		}
		indexes.put(name, create(entityMetadata, name, indexedFieldNames));
	}
	
	
	private <T> void processIndexContainerAnnotation(EntityMetadata<T> entityMetadata, Annotation a, String fieldName, Map<String, IndexMetadata<T>> indexes) {
		Class<? extends Annotation> annotationType = a.annotationType();
		IndexAnnotationContainer indexContainerDef = indexContainerDefs.get(annotationType);
		if(indexContainerDef == null) {
			return;
		}
		
		Class<?> indexAnnotationClass = ClassAccessor.forName(invoke(annotationType, indexContainerDef.getIndexAnnotationClassName(), null, String.class, a, null));
		
		Object indexAnnotations = invoke(annotationType, indexContainerDef.getCollection(), null, Array.newInstance(indexAnnotationClass, 0).getClass(), a, null);
		int n = Array.getLength(indexAnnotations);
		for (int i = 0; i < n; i++) {
			Annotation indexAnnotation = (Annotation)Array.get(indexAnnotations, i);
			processIndexAnnotation(entityMetadata, indexAnnotation, fieldName, indexes);
		}
	}
	
	

	@Override
	public <T, F> FieldMetadata<T, F> create(Class<T> clazz, Class<F> fieldClass, String fieldName) {
		return new FieldMetadata<>(clazz, fieldClass, fieldName);
	}
	
	@Override
	public <T> IndexMetadata<T> create(EntityMetadata<T> entityMetadata, String name, String[] indexedFields) {
		return new IndexMetadata<>(entityMetadata, name, indexedFields);
	}
	
	
	protected String getFieldName(AccessibleObject ao) {
		return StandardNamingStrategy.LOWER_CAMEL_CASE.getColumnName(ao);
	}
	
	@Override
	public void setSchema(String schema) {
		this.schema = schema;
	}

	protected String getSchema() {
		return this.schema;
	}
}
