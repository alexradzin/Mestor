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
		final IndexAnnotations indexAnnotations = laodIndexAnnotationsXml();

		final Map<Class<? extends Annotation>, IndexAnnotation> indexDefsTemp = new HashMap<>();
		final Map<Class<? extends Annotation>, IndexAnnotationContainer> indexContainerDefsTemp = new HashMap<>();

		if(indexAnnotations != null) {
			for (final Object def : indexAnnotations.getIndexDefs()) {
				// although usage of instanceof is ugly I use it here because it is easier at the moment.
				String className = null;
				try{
					if (def instanceof IndexAnnotation) {
						final IndexAnnotation ia = (IndexAnnotation)def;
						className = ia.getClassName();
						indexDefsTemp.put(ClassAccessor.<Annotation>forNameThrowsChecked(className), ia);
					} else if (def instanceof IndexAnnotationContainer) {
						final IndexAnnotationContainer iac = (IndexAnnotationContainer)def;
						className = iac.getClassName();
						indexContainerDefsTemp.put(ClassAccessor.<Annotation>forNameThrowsChecked(className), iac);
					} else {
						throw new UnsupportedOperationException("Index configuration " + def.getClass() + " is unsupported");
					}
				}catch(final ClassNotFoundException e){
					//xml defines all known annotation classes - not necessarily the will be on classpath
					//ignore
					logger.debug("parsing index-annotations.xml: ", e);
					logger.info("parsing index-annotations.xml: class not found on classpath: {}", className);
				}
			}
		}

		indexDefs = Collections.unmodifiableMap(indexDefsTemp);
		indexContainerDefs = Collections.unmodifiableMap(indexContainerDefsTemp);
	}



	private IndexAnnotations laodIndexAnnotationsXml() {
		final String indexAnnotationPackage = IndexAnnotations.class.getPackage().getName();
		final String indexAnnotationConfigPath = getIndexAnnotationsXmlLocation(indexAnnotationPackage);

		try{
			try(InputStream indexConfig = getClass().getResourceAsStream(indexAnnotationConfigPath)){
				if(indexConfig == null) {
					return null;
				}

				try {
					final JAXBContext jaxbContext = JAXBContext.newInstance(indexAnnotationPackage);
					final Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
					return (IndexAnnotations)jaxbUnmarshaller.unmarshal(indexConfig);
				} catch (final JAXBException e) {
					throw new IllegalArgumentException(e);
				}
			}
		}catch(final IOException e){
			throw new RuntimeException(e);
		}
	}



	private String getIndexAnnotationsXmlLocation(final String indexAnnotationPackage) {
		//TODO make this configurable
		return "/" + indexAnnotationPackage.replace('.', '/') + "/" +  "index-annotations.xml";
	}



	@Override
	public <T> EntityMetadata<T> create(final Class<T> clazz) {
		final EntityMetadata<T> emeta = new EntityMetadata<>(clazz);


		final Map<String, FieldMetadata<T, Object, Object>> fields = new LinkedHashMap<>();

		for (final Field f : FieldAccessor.getFields(clazz)) {
			final String name = getFieldName(f);

			@SuppressWarnings("unchecked")
			final
			FieldMetadata<T, Object, Object> fmeta = create(clazz, (Class<Object>)f.getType(), name);
			fmeta.setField(f);

			fields.put(name, fmeta);
		}


		for (final Method m : clazz.getMethods()) {
			if(!(MethodAccessor.isGetter(m) || MethodAccessor.isSetter(m))) {
				continue;
			}

			final String fieldName = getFieldName(m);
			FieldMetadata<T, Object, Object> fmeta = fields.get(fieldName);

			if (fmeta == null) {
				@SuppressWarnings("unchecked")
				final
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


		for(final FieldMetadata<T, Object, Object> field : fields.values()) {
			emeta.addField(field);
		}


		emeta.addAllIndexes(findIndexes(emeta));

		return emeta;
	}


	protected <T> Collection<IndexMetadata<T>> findIndexes(final EntityMetadata<T> entityMetadata) {
		final Map<String, IndexMetadata<T>> indexes = new LinkedHashMap<>();
		final Class<T> entityType = entityMetadata.getEntityType();

		// process class level index annotations
		findIndexes(entityMetadata, entityType, null, indexes);

		// process field and method level index annotations
		for (final FieldMetadata<T, Object, Object> fmeta : entityMetadata.getFields()) {
			findIndexes(entityMetadata, fmeta.getAccessor().getField(), fmeta.getName(), indexes);
			findIndexes(entityMetadata, fmeta.getAccessor().getGetter(), fmeta.getName(), indexes);
		}

		return indexes.values();
	}


	private <T> void findIndexes(final EntityMetadata<T> entityMetadata, final AnnotatedElement ae, final String fieldName, final Map<String, IndexMetadata<T>> indexes) {
		if (ae == null) {
			return;
		}
		for(final Annotation a : ae.getAnnotations()) {
			processIndexAnnotation(entityMetadata, a, fieldName, indexes);
			processIndexContainerAnnotation(entityMetadata, a, fieldName, indexes);
		}
	}



	private <T> void processIndexAnnotation(final EntityMetadata<T> entityMetadata, final Annotation a, final String fieldName, final Map<String, IndexMetadata<T>> indexes) {
		final Class<? extends Annotation> annotationType = a.annotationType();
		final IndexAnnotation indexDef = indexDefs.get(annotationType);
		if (indexDef == null) {
			return;
		}

		String indexName = invoke(annotationType, indexDef.getName(), null, String.class, a, null);
		final String[] indexedColumnNames;

		if (fieldName != null) {
			final FieldMetadata<T, Object, Object> field = entityMetadata.getFieldByName(fieldName.trim());
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



	private <T> String[] getIndexedColumnsForFieldIndex(final IndexMetadata<T> existingIndex, final String columnName) {
		final String[] indexedColumnNames;
		if (existingIndex != null) {
			indexedColumnNames = addAndRemoveDuplicates(existingIndex.getColumnNames(), columnName);
		} else {
			indexedColumnNames = new String[] {columnName};
		}
		return indexedColumnNames;
	}



	private <T> String[] addAndRemoveDuplicates(final String[] columnNames, final String columnName) {
		final Collection<String> allNames = new LinkedHashSet<String>(Arrays.asList(columnNames));
		if(columnName != null) {
			allNames.add(columnName);
		}
		return allNames.toArray(new String[0]);
	}

	private <T> String[] trimAndRemoveDuplicates(final String[] columnNames) {
		final String [] trimmedColumnNames = new String[columnNames.length];
		for (int i = 0; i < columnNames.length; i++) {
			final String cn = columnNames[i];
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


	private <T> void processIndexContainerAnnotation(final EntityMetadata<T> entityMetadata, final Annotation a, final String fieldName, final Map<String, IndexMetadata<T>> indexes) {
		final Class<? extends Annotation> annotationType = a.annotationType();
		final IndexAnnotationContainer indexContainerDef = indexContainerDefs.get(annotationType);
		if(indexContainerDef == null) {
			return;
		}

		final Class<?> indexAnnotationClass = ClassAccessor.forName(indexContainerDef.getIndexAnnotationClassName());
				//invoke(annotationType, indexContainerDef.getIndexAnnotationClassName(), null, String.class, a, null));

		final Object indexAnnotations = invoke(annotationType, indexContainerDef.getCollection(), null, Array.newInstance(indexAnnotationClass, 0).getClass(), a, null);
		final int n = Array.getLength(indexAnnotations);
		for (int i = 0; i < n; i++) {
			final Annotation indexAnnotation = (Annotation)Array.get(indexAnnotations, i);
			processIndexAnnotation(entityMetadata, indexAnnotation, fieldName, indexes);
		}
	}



	@Override
	public <T, F, C> FieldMetadata<T, F, C> create(final Class<T> clazz, final Class<F> fieldClass, final String fieldName) {
		return new FieldMetadata<>(clazz, fieldClass, fieldName);
	}

	@Override
	public <T> IndexMetadata<T> create(final EntityMetadata<T> entityMetadata, final String name, final String... indexedFields) {
		return new IndexMetadata<>(entityMetadata, name, indexedFields);
	}


	protected String getFieldName(final AccessibleObject ao) {
		if (ao instanceof Field) {
			return ((Field) ao).getName();
		}

		if (ao instanceof Method) {
			final String methodName = ((Method) ao).getName();
			final Matcher m = methodNamePattern.matcher(methodName);
			if (!m.find()) {
				throw new IllegalArgumentException("Method " + ao + " is neither getter nor setter: ");
			}
			final String name = m.replaceFirst("");
			return name.substring(0, 1).toLowerCase() + name.substring(1);
		}

		throw new IllegalArgumentException("Given accessible object is neither field nor method: " + ao);
	}

	@Override
	public void setSchema(final String schema) {
		this.schema = schema;
	}

	protected String getSchema() {
		return this.schema;
	}



	@Override
	public void update(final Map<Class<?>, EntityMetadata<?>> metadata) {
		// empty implementation here
	}
}
