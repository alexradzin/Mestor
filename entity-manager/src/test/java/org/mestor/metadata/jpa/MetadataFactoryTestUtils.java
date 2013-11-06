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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Assert;
import org.mestor.context.EntityContext;
import org.mestor.metadata.EntityMetadata;
import org.mestor.metadata.FieldMetadata;
import org.mestor.metadata.FieldRole;
import org.mestor.metadata.IndexMetadata;
import org.mestor.persistence.query.JpqlParser;
import org.mockito.Mockito;

public class MetadataFactoryTestUtils {

	static Map<Class<?>, EntityMetadata<?>> testJpaAnnotations(final Class<?> ... classes) {
		final EntityContext ctx = Mockito.mock(EntityContext.class);
		final JpaAnnotationsMetadataFactory factory = new JpaAnnotationsMetadataFactory();
		factory.setEntityContext(ctx);

		doReturn(new JpqlParser()).when(ctx).getCriteriaLanguageParser();

		final Map<Class<?>, EntityMetadata<?>> entityClasses = new LinkedHashMap<Class<?>, EntityMetadata<?>>() {
		    @Override
			public EntityMetadata<?> put(final Class<?> clazz, final EntityMetadata<?> emd) {
		    	if (emd == null) {
		    		return null;
		    	}
		    	return super.put(clazz, emd);
		    }

		};
		for (final Class<?> clazz : classes) {
			final EntityMetadata<?>  emd = factory.create(clazz);
			entityClasses.put(clazz, emd);
			doReturn(emd).when(ctx).getEntityMetadata(clazz);
		}


		factory.update(entityClasses);


		return entityClasses;
	}

	static <E> void assertEntityMetadataFields(final Collection<FieldMetadata<E, Object, Object>> fields, final String[] names, final Class<?>[] types, final String[] columns, final Class<?>[] columnTypes) {
		assertEquals(names.length, fields.size());
		assertEquals(names.length, columns.length);
		assertEquals(columns.length, columnTypes.length);

		final int n = fields.size();
		@SuppressWarnings("unchecked")
		final
		FieldMetadata<E, Object, Object>[] fieldsArray = fields.toArray(new FieldMetadata[0]);
		for (int i = 0; i < n; i++) {
			assertEquals(names[i], fieldsArray[i].getName());
			assertEquals("Wrong type of " + names[i], types[i], fieldsArray[i].getType());
			assertEquals("Wrong column name of " + names[i], columns[i], fieldsArray[i].getColumn());
			assertEquals("Wrong column type of " + names[i], columnTypes[i], fieldsArray[i].getColumnType());
		}
	}


	static <E> void assertEntityMetadataFieldsGenerics(final Collection<FieldMetadata<E, Object, Object>> fields,
			final String[] names,
			final Class<?>[][] fieldGenerics,
			final Class<?>[][] columnGenerics) {

		final Map<String, Class<?>[]> fieldNameToGenerics = createNameToGenericsMapping(names, fieldGenerics);
		final Map<String, Class<?>[]> fieldNameToColumnGenerics = createNameToGenericsMapping(names, columnGenerics);

		for (final FieldMetadata<E, Object, Object> fmd : fields) {
			final String name = fmd.getName();
			final Class<?>[] currentFieldGenerics = getGenericsByName(fieldNameToGenerics, name);
			final Class<?>[] currentColumnGenerics = getGenericsByName(fieldNameToColumnGenerics, name);
			assertArrayEquals(currentFieldGenerics, fmd.getGenericTypes().toArray(new Class[0]));
			assertArrayEquals(currentColumnGenerics, fmd.getColumnGenericTypes().toArray(new Class[0]));
		}
	}


	private static Map<String, Class<?>[]> createNameToGenericsMapping(final String[] names, final Class<?>[][] fieldGenerics) {
		final Map<String, Class<?>[]> nameToGenerics = new HashMap<>();
		for (int i = 0; i < names.length; i++) {
			nameToGenerics.put(names[i], fieldGenerics[i]);
		}
		return nameToGenerics;
	}

	private static Class<?>[] getGenericsByName(final Map<String, Class<?>[]> mapping, final String name) {
		final Class<?>[] generics = mapping.get(name);
		return generics == null ? new Class[0] : generics;
	}

	static <E> void assertEntityMetadata(final EntityMetadata<E> emd, final Class<E> expectedType, final String expectedEntityName, final String expectedTableName) {
		assertNotNull(emd);
		assertEquals(expectedType, emd.getEntityType());
		assertEquals(expectedEntityName, emd.getEntityName());
		assertEquals(expectedTableName, emd.getTableName());
	}

	static <E> void assertEntityMetadataIndexes(final Collection<IndexMetadata<E>> indexes, final Map<String, String[]> namesToFieldNamesMap) {
		assertEquals("wrong index count", namesToFieldNamesMap.size(), indexes.size());

		for(final IndexMetadata<E> index : indexes) {
			assertTrue("extra index found: " + index.getName(), namesToFieldNamesMap.containsKey(index.getName()));
			final String[] fieldNames = namesToFieldNamesMap.remove(index.getName());
			Assert.assertArrayEquals("field names don't match", fieldNames, index.getFieldNames());
		}
	}

	@SuppressWarnings("unchecked")
	static <E> EntityMetadata<E> getEntityMetadata(final Map<Class<?>, EntityMetadata<?>> entityClasses, final Class<E> clazz) {
		return (EntityMetadata<E>)entityClasses.get(clazz);
	}


	static <T> Map<String, T[]> buildStringToStringArrayMap(final Object ...objects){
		final Map<String, T[]> res = new HashMap<>();
		for (int i = 0; i < objects.length; i += 2) {
			final String key = (String)objects[i];
			@SuppressWarnings("unchecked")
			final T[] value = (T[])objects[i+1];
			res.put(key, value);
		}

		return res;
	}

	static< T> EntityMetadata<T> testIndexes(final Class<T> clazz, final Map<String, String[]> expected) {
		if(clazz == null) {
			throw new RuntimeException("Class is null");
		}
		final Map<Class<?>, EntityMetadata<?>> entityClasses = testJpaAnnotations(clazz);
		@SuppressWarnings("unchecked")
		final EntityMetadata<T> md = (EntityMetadata<T>) entityClasses.get(clazz);
		final Collection<IndexMetadata<T>> indexes = md.getIndexes();
		assertEntityMetadataIndexes(indexes, expected);
		return md;
	}

	static< T> EntityMetadata<T> testFieldRoles(final Class<T> clazz, final Map<String, FieldRole[]> expected) {
		if(clazz == null) {
			return null;
		}
		final Map<Class<?>, EntityMetadata<?>> entityClasses = testJpaAnnotations(clazz);
		@SuppressWarnings("unchecked")
		final EntityMetadata<T> emd = (EntityMetadata<T>) entityClasses.get(clazz);

		for (final Entry<String, FieldRole[]> expectedEntry : expected.entrySet()) {
			final String fieldName = expectedEntry.getKey();
			final FieldRole[] expectedRoles = expectedEntry.getValue();
			final FieldMetadata<?, ?, ?> fmd = emd.getFieldByName(fieldName);
			for (final FieldRole role : expectedRoles) {
				switch(role) {
					case DISCRIMINATOR:
						assertTrue(fmd.isDiscriminator());
						break;
					case FILTER:
						assertTrue(fmd.isFilter());
						break;
					case JOINER:
						assertTrue(fmd.isJoiner());
						break;
					case PARTITION_KEY:
						assertTrue(fmd.isPartitionKey());
						break;
					case PRIMARY_KEY:
						assertTrue(fmd.isKey());
						break;
					case SORTER:
						assertTrue(fmd.isSorter());
						break;
				}
			}
		}



		return emd;
	}


}