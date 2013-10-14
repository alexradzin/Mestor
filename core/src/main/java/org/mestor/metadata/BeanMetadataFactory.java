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

import static org.mestor.reflection.ClassAccessor.invoke;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.mestor.metadata.exceptions.DuplicateIndexName;
import org.mestor.metadata.index.IndexAnnotation;
import org.mestor.metadata.index.IndexAnnotationContainer;
import org.mestor.metadata.index.IndexAnnotations;
import org.mestor.reflection.ClassAccessor;
import org.mestor.reflection.FieldAccessor;
import org.mestor.reflection.MethodAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BeanMetadataFactory implements MetadataFactory {
	
	private final static Logger logger = LoggerFactory.getLogger(BeanMetadataFactory.class);
	
	private final static Pattern methodNamePattern = Pattern.compile("(set|get|is)");
	private final Map<Class<? extends Annotation>, IndexAnnotation> indexDefs;
	private final Map<Class<? extends Annotation>, IndexAnnotationContainer> indexContainerDefs;
	private String schema;
	
	public BeanMetadataFactory() {
		final String indexAnnotationPackage = IndexAnnotations.class.getPackage().getName();
		
		final IndexAnnotations indexAnnotations = laodIndexAnnotationsXml(indexAnnotationPackage);
		
		final Map<Class<? extends Annotation>, IndexAnnotation> indexDefsTemp = new HashMap<>();
		final Map<Class<? extends Annotation>, IndexAnnotationContainer> indexContainerDefsTemp = new HashMap<>();
		
		if(indexAnnotations != null) {
			for (Object def : indexAnnotations.getIndexDefs()) {
				// although usage of instanceof is ugly I use it here because it is easier at the moment.
				try{
					if (def instanceof IndexAnnotation) {
						IndexAnnotation ia = (IndexAnnotation)def;
						indexDefsTemp.put(ClassAccessor.<Annotation>forNameThrowsChecked(ia.getClassName()), ia);
					} else if (def instanceof IndexAnnotationContainer) {
						IndexAnnotationContainer iac = (IndexAnnotationContainer)def;
						indexContainerDefsTemp.put(ClassAccessor.<Annotation>forNameThrowsChecked(iac.getClassName()), iac);
					} else {
						throw new UnsupportedOperationException("Index configuration " + def.getClass() + " is unsupported");
					}
				}catch(ClassNotFoundException e){
					//xml defines all known annotation classes - not necessarily the will be on classpath
					//ignore
					logger.debug("parsing index-annotations.xml", e);
				}
			}
		}
		
		indexDefs = Collections.unmodifiableMap(indexDefsTemp);
		indexContainerDefs = Collections.unmodifiableMap(indexContainerDefsTemp);
	}



	private IndexAnnotations laodIndexAnnotationsXml(final String indexAnnotationPackage) {
		
		final String indexAnnotationConfigPath = getIndexAnnotationsXmlLocation(indexAnnotationPackage);

		try{
			try(InputStream indexConfig = getClass().getResourceAsStream(indexAnnotationConfigPath)){
				if(indexConfig == null) {
					return null;
				}
				
				try {
					JAXBContext jaxbContext = JAXBContext.newInstance(indexAnnotationPackage);
					Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
					return (IndexAnnotations)jaxbUnmarshaller.unmarshal(indexConfig);
				} catch (JAXBException e) {
					throw new IllegalArgumentException(e);
				}
			}
		}catch(IOException e){
			throw new RuntimeException(e);
		}
	}



	private String getIndexAnnotationsXmlLocation(final String indexAnnotationPackage) {
		//TODO make this configurable
		return "/" + indexAnnotationPackage.replace('.', '/') + "/" +  "index-annotations.xml";
	}
	
	
	
	@Override
	public <T> EntityMetadata<T> create(Class<T> clazz) {
		EntityMetadata<T> emeta = new EntityMetadata<>(clazz);
		
		
		Map<String, FieldMetadata<T, Object, Object>> fields = new LinkedHashMap<>();
		
		for (Field f : FieldAccessor.getFields(clazz)) {
			String name = getFieldName(f);
			
			@SuppressWarnings("unchecked")
			FieldMetadata<T, Object, Object> fmeta = create(clazz, (Class<Object>)f.getType(), name);
			fmeta.setField(f);
			
			fields.put(name, fmeta);
		}
		
		
		for (Method m : clazz.getMethods()) {
			if(!(MethodAccessor.isGetter(m) || MethodAccessor.isSetter(m))) {
				continue;
			}
			
			String fieldName = getFieldName(m);
			FieldMetadata<T, Object, Object> fmeta = fields.get(fieldName);
			
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
		

		for(FieldMetadata<T, Object, Object> field : fields.values()) {
			emeta.addField(field);
		}
		
		
		emeta.addAllIndexes(findIndexes(emeta));
		
		return emeta;
	}

	
	protected <T> Collection<IndexMetadata<T>> findIndexes(EntityMetadata<T> entityMetadata) {
		Map<String, IndexMetadata<T>> indexes = new LinkedHashMap<>();
		Class<T> entityType = entityMetadata.getEntityType();
		
		// process class level index annotations
		findIndexes(entityMetadata, entityType, null, indexes);
		
		// process field and method level index annotations
		for (FieldMetadata<T, Object, Object> fmeta : entityMetadata.getFields()) {
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
		
		String indexName = invoke(annotationType, indexDef.getName(), null, String.class, a, null);
		final String[] indexedColumnNames;
		
		if (fieldName != null) {
			FieldMetadata<T, Object, Object> field = entityMetadata.getFieldByName(fieldName.trim());
			if(field == null) {
				throw new IllegalArgumentException("Field not found");
			}
			
			final String columnName = field.getColumn();
			
			if (indexName == null || "".equals(indexName)) {
				indexName = columnName;
			}
			
			indexedColumnNames = getIndexedColumnsForFieldIndex(indexes.get(indexName), columnName);
		} else {
			if(indexes.containsKey(indexName)){
				throw new DuplicateIndexName(entityMetadata.getEntityType(), indexName);
			}
			final Object indexColumnNamesFromAnnotation = invoke(annotationType, indexDef.getColumnNames(), null, Object.class, a, null);
			indexedColumnNames = getIndexedColumnNames(indexColumnNamesFromAnnotation);
		}
		
		if(indexName == null){
			throw new NullPointerException("Index name is null");
		}

		indexes.put(indexName, create(entityMetadata, indexName, indexedColumnNames));
	}



	private <T> String[] getIndexedColumnsForFieldIndex(IndexMetadata<T> existingIndex, final String columnName) {
		final String[] indexedColumnNames;
		if (existingIndex != null) {
			indexedColumnNames = addAndRemoveDuplicates(existingIndex.getColumnNames(), columnName);
		} else {
			indexedColumnNames = new String[] {columnName};
		}
		return indexedColumnNames;
	}



	private <T> String[] addAndRemoveDuplicates(String[] columnNames, final String columnName) {		
		Collection<String> allNames = new LinkedHashSet<String>(Arrays.asList(columnNames));
		if(columnName != null) {
			allNames.add(columnName);
		}
		return allNames.toArray(new String[0]);
	}
	
	private <T> String[] trimAndRemoveDuplicates(String[] columnNames) {
		String [] trimmedColumnNames = new String[columnNames.length];
		for (int i = 0; i < columnNames.length; i++) {
			String cn = columnNames[i];
			trimmedColumnNames[i] = cn.trim();
		}
		return addAndRemoveDuplicates(trimmedColumnNames, null);
	}

	private String[] getIndexedColumnNames(final Object indexColumnNames) {
		
		String[] indexedColumnNames = null;
		
		if (indexColumnNames.getClass().isArray()) {
			indexedColumnNames = (String[]) indexColumnNames;
		} else if (indexColumnNames instanceof String) {
			final String tempIndexFieldNames = (String) indexColumnNames;
			indexedColumnNames = tempIndexFieldNames.split(",");
		} else {
			throw new IllegalArgumentException("Don't know column name");
		}

		return trimAndRemoveDuplicates(indexedColumnNames);
	}
	
	
	private <T> void processIndexContainerAnnotation(EntityMetadata<T> entityMetadata, Annotation a, String fieldName, Map<String, IndexMetadata<T>> indexes) {
		Class<? extends Annotation> annotationType = a.annotationType();
		IndexAnnotationContainer indexContainerDef = indexContainerDefs.get(annotationType);
		if(indexContainerDef == null) {
			return;
		}
		
		Class<?> indexAnnotationClass = ClassAccessor.forName(indexContainerDef.getIndexAnnotationClassName());
				//invoke(annotationType, indexContainerDef.getIndexAnnotationClassName(), null, String.class, a, null));
		
		Object indexAnnotations = invoke(annotationType, indexContainerDef.getCollection(), null, Array.newInstance(indexAnnotationClass, 0).getClass(), a, null);
		int n = Array.getLength(indexAnnotations);
		for (int i = 0; i < n; i++) {
			Annotation indexAnnotation = (Annotation)Array.get(indexAnnotations, i);
			processIndexAnnotation(entityMetadata, indexAnnotation, fieldName, indexes);
		}
	}
	
	

	@Override
	public <T, F, C> FieldMetadata<T, F, C> create(Class<T> clazz, Class<F> fieldClass, String fieldName) {
		return new FieldMetadata<>(clazz, fieldClass, fieldName);
	}
	
	@Override
	public <T> IndexMetadata<T> create(EntityMetadata<T> entityMetadata, String name, String... indexedFields) {
		return new IndexMetadata<>(entityMetadata, name, indexedFields);
	}
	
	
	protected String getFieldName(AccessibleObject ao) {
		if (ao instanceof Field) {
			return ((Field) ao).getName();
		}
		
		if (ao instanceof Method) {
			String methodName = ((Method) ao).getName();
			Matcher m = methodNamePattern.matcher(methodName);
			if (!m.find()) {
				throw new IllegalArgumentException("Method " + ao + " is neither getter nor setter: ");
			}
			String name = m.replaceFirst("");
			return name.substring(0, 1).toLowerCase() + name.substring(1);
		}
		
		throw new IllegalArgumentException("Given accessible object is neither field nor method: " + ao);
	}
	
	@Override
	public void setSchema(String schema) {
		this.schema = schema;
	}

	protected String getSchema() {
		return this.schema;
	}



	@Override
	public void update(Map<Class<?>, EntityMetadata<?>> metadata) {
		// empty implementation here
	}
}
