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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Assert;
import org.mestor.context.EntityContext;
import org.mestor.metadata.EntityMetadata;
import org.mestor.metadata.FieldMetadata;
import org.mestor.metadata.IndexMetadata;
import org.mockito.Mockito;

public class MetadataFactoryTestUtils {

	static Map<Class<?>, EntityMetadata<?>> testJpaAnnotations(Class<?> ... classes) {
		final EntityContext ctx = Mockito.mock(EntityContext.class);
		JpaAnnotationsMetadataFactory factory = new JpaAnnotationsMetadataFactory();
		factory.setEntityContext(ctx);
		
		Map<Class<?>, EntityMetadata<?>> entityClasses = new LinkedHashMap<Class<?>, EntityMetadata<?>>() {
		    @Override
			public EntityMetadata<?> put(Class<?> clazz, EntityMetadata<?> emd) {
		    	if (emd == null) {
		    		return null;
		    	}
		    	return super.put(clazz, emd);
		    }
			
		};
		for (Class<?> clazz : classes) {
			EntityMetadata<?>  emd = factory.create(clazz);
			entityClasses.put(clazz, emd);
			doReturn(emd).when(ctx).getEntityMetadata(clazz);
		}
		
		
		factory.update(entityClasses);
		
		
		return entityClasses;
	}

	static <E> void assertEntityMetadataFields(Collection<FieldMetadata<E, Object, Object>> fields, String[] names, Class<?>[] types, String[] columns, Class<?>[] columnTypes) {
		assertEquals(names.length, fields.size()); 
		assertEquals(names.length, columns.length);
		assertEquals(columns.length, columnTypes.length);
		
		int n = fields.size();
		@SuppressWarnings("unchecked")
		FieldMetadata<E, Object, Object>[] fieldsArray = fields.toArray(new FieldMetadata[0]);
		for (int i = 0; i < n; i++) {
			assertEquals(names[i], fieldsArray[i].getName());
			assertEquals("Wrong type of " + names[i], types[i], fieldsArray[i].getType());
			assertEquals("Wrong column name of " + names[i], columns[i], fieldsArray[i].getColumn());
			assertEquals("Wrong column type of " + names[i], columnTypes[i], fieldsArray[i].getColumnType());
		}
	}

	static <E> void assertEntityMetadata(EntityMetadata<E> emd, Class<E> expectedType, String expectedEntityName, String expectedTableName) {
		assertNotNull(emd);
		assertEquals(expectedType, emd.getEntityType());
		assertEquals(expectedEntityName, emd.getEntityName()); 
		assertEquals(expectedTableName, emd.getTableName());
	}

	static <E> void assertEntityMetadataIndexes(Collection<IndexMetadata<E>> indexes, Map<String, String[]> namesToFieldNamesMap) {
		assertEquals("wrong index count", namesToFieldNamesMap.size(), indexes.size()); 
		
		for(IndexMetadata<E> index : indexes) {
			assertTrue("extra index found: " + index.getName(), namesToFieldNamesMap.containsKey(index.getName()));
			String[] fieldNames = namesToFieldNamesMap.remove(index.getName());
			Assert.assertArrayEquals("field names don't match", fieldNames, index.getFieldNames());
		}
	}

	@SuppressWarnings("unchecked")
	static <E> EntityMetadata<E> getEntityMetadata(Map<Class<?>, EntityMetadata<?>> entityClasses, Class<E> clazz) {
		return (EntityMetadata<E>)entityClasses.get(clazz);
	}

	
	static Map<String, String[]> buildStringToStringArrayMap(Object ...objects){
		Map<String, String[]> res = new HashMap<>();
		for (int i = 0; i < objects.length; i += 2) {
			String key = (String)objects[i];
			String[] value = (String[])objects[i+1];
			res.put(key, value);
		}
		
		return res;
	}

	static< T> void testIndexes(Class<T> clazz, Map<String, String[]> expected) {
		Map<Class<?>, EntityMetadata<?>> entityClasses = testJpaAnnotations(clazz);
		@SuppressWarnings("unchecked")
		EntityMetadata<T> md = (EntityMetadata<T>) entityClasses.get(clazz);
		Collection<IndexMetadata<T>> indexes = md.getIndexes();
		assertEntityMetadataIndexes(indexes, expected);
	}
}