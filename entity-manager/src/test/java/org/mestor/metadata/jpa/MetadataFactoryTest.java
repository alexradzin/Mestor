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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;

import java.io.Serializable;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.mestor.context.EntityContext;
import org.mestor.entities.Country;
import org.mestor.entities.annotated.AbstractEntity;
import org.mestor.entities.annotated.Address;
import org.mestor.entities.annotated.EmailAddress;
import org.mestor.entities.annotated.Person;
import org.mestor.entities.annotated.Person.Gender;
import org.mestor.entities.annotated.SimpleProperty;
import org.mestor.entities.annotated.StreetAddress;
import org.mestor.entities.annotated.User;
import org.mestor.metadata.EntityMetadata;
import org.mestor.metadata.FieldMetadata;
import org.mestor.metadata.IndexMetadata;
import org.mockito.Mockito;

/**
 * This test case contains tests that use annotations to extract metadata of given entities
 * and verify that metadata is as expected. The tests do not actually perform persistence 
 * operations because core module does not depend on any persistence implementation.  
 * @author alexr
 */
public class MetadataFactoryTest {
	@Test
	public void testNoClasses() {
		assertTrue(testJpaAnnotations().isEmpty());
	}
	
	@Test
	public void testNotEntityClass() {
		assertTrue(testJpaAnnotations(String.class).isEmpty());
		assertTrue(testJpaAnnotations(AbstractEntity.class).isEmpty());
	}

	
	@Test
	public void testOneSimpleEntityClass() {
		Map<Class<?>, EntityMetadata<?>> entityClasses = testJpaAnnotations(SimpleProperty.class);

		assertFalse(entityClasses.isEmpty());
		assertEquals(1,  entityClasses.size());
		
		@SuppressWarnings("unchecked")
		EntityMetadata<SimpleProperty> emd = (EntityMetadata<SimpleProperty>)entityClasses.get(SimpleProperty.class);
		assertNotNull(emd);
		
		assertEntityMetadata(emd, SimpleProperty.class, "simple_property", "simple_property");
		assertEnttityMetadataFields(
				emd.getFields(), 
				new String[] {"name", "type", "value"}, 
				new Class[] {String.class, Class.class, Serializable.class}, 
				new String[] {"name", "type", "value"}, 
				new Class[] {String.class, String.class, ByteBuffer.class});
	}
	
	@Test
	public void testPersonIndexes() {
		Map<Class<?>, EntityMetadata<?>> entityClasses = testJpaAnnotations(Person.class);

		@SuppressWarnings("unchecked")
		EntityMetadata<Person> md = (EntityMetadata<Person>) entityClasses.get(Person.class);
		Collection<IndexMetadata<Person>> indexes = md.getIndexes();
		Map<String, String[]> expected = new HashMap<String, String[]>() {
			{
				this.put("name", new String[] { "name" });
				this.put("age", new String[] { "age" });
				this.put("full_name", new String[] { "name", "lastName" });
			}
		};
		assertEnttityMetadataIndexes(indexes, expected);
	}

	
	
	@Test
	public void testUserClass() {
		Map<Class<?>, EntityMetadata<?>> entityClasses = testJpaAnnotations(User.class, Person.class);

		assertFalse(entityClasses.isEmpty());
		assertEquals(2,  entityClasses.size());

		
		assertUserMetadata(entityClasses);
		assertPersonMetadata(entityClasses);
	}
	
	
	
	@Test
	public void testPersonClass() {
		Map<Class<?>, EntityMetadata<?>> entityClasses = testJpaAnnotations(Person.class, User.class, Address.class, StreetAddress.class, EmailAddress.class);

		assertFalse(entityClasses.isEmpty());
		assertEquals(5,  entityClasses.size());
		
		Set<String> tables = new HashSet<>();
		for (EntityMetadata<?> emd : entityClasses.values()) {
			String tableName = emd.getTableName();
			if (tableName != null) {
				tables.add(emd.getTableName());
			}
		}
		
		assertEquals(new HashSet<String>(Arrays.asList("person", "user", "address", "email")), tables);
		
		
		assertUserMetadata(entityClasses);
		assertPersonMetadata(entityClasses);
		assertAddressMetadata(entityClasses);
		assertEmailMetadata(entityClasses);
		assertStreetAddressMetadata(entityClasses);
	}
	
	
	
	
	private Map<Class<?>, EntityMetadata<?>> testJpaAnnotations(Class<?> ... classes) {
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
	

	// Assert and access utilities
	
	private <E> void assertEntityMetadata(EntityMetadata<E> emd, Class<E> expectedType, String expectedEntityName, String expectedTableName) {
		assertNotNull(emd);
		assertEquals(expectedType, emd.getEntityType());
		assertEquals(expectedEntityName, emd.getEntityName()); 
		assertEquals(expectedTableName, emd.getTableName());
	}

	private <E> void assertEnttityMetadataFields(Collection<FieldMetadata<E, Object, Object>> fields, String[] names, Class<?>[] types, String[] columns, Class<?>[] columnTypes) {
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
	
	private <E> void assertEnttityMetadataIndexes(Collection<IndexMetadata<E>> indexes, Map<String, String[]> namesToFieldNamesMap) {
		assertEquals("wrong indexe count", namesToFieldNamesMap.size(), indexes.size()); 
		
		for(IndexMetadata<E> index : indexes) {
			assertTrue("extra index found", namesToFieldNamesMap.containsKey(index.getName()));
			String[] fieldNames = namesToFieldNamesMap.remove(index.getName());
			Assert.assertArrayEquals("field names don't match", fieldNames, index.getFieldNames());
		}
	}


	
	@SuppressWarnings("unchecked")
	private <E> EntityMetadata<E> getEntityMetadata(Map<Class<?>, EntityMetadata<?>> entityClasses, Class<E> clazz) {
		return (EntityMetadata<E>)entityClasses.get(clazz);
	}
	
	
	// Assert utilities for individual entity types
	
	private void assertUserMetadata(Map<Class<?>, EntityMetadata<?>> entityClasses) {
		EntityMetadata<User> userMeta = getEntityMetadata(entityClasses, User.class);
		assertNotNull(userMeta);
		
		assertEntityMetadata(userMeta, User.class, "user", "user");
		assertEnttityMetadataFields(
				userMeta.getFields(), 
				new String[] {"identifier", "lastModified", "site", "username", "password", "roles", "person"}, 
				new Class[] {int.class, long.class, URL.class, String.class, String.class, Set.class, Person.class}, 
				new String[] {"identifier", "last_modified", "site", "username", "password", "roles", "person_identifier"},  
				new Class[] {int.class, long.class,   String.class, String.class, String.class, Set.class, int.class});
	}
	
	private void assertPersonMetadata(Map<Class<?>, EntityMetadata<?>> entityClasses) {
		EntityMetadata<Person> personMeta = getEntityMetadata(entityClasses, Person.class);
		assertNotNull(personMeta);
		
		assertEntityMetadata(personMeta, Person.class, "person", "person");
		assertEnttityMetadataFields(
				personMeta.getFields(), 
				new String[] {"identifier", "lastModified", "name", "lastName", "age", "gender", "addresses", "accounts"}, 
				new Class[] {int.class, long.class, String.class, String.class, int.class, Gender.class, List.class, List.class}, 
				new String[] {"identifier", "last_modified", "name", "last_name", "age", "gender", "addresses", "accounts"}, 
				new Class[] {int.class, long.class, String.class, String.class, int.class, String.class, List.class, List.class});
	}

	
	private void assertAddressMetadata(Map<Class<?>, EntityMetadata<?>> entityClasses) {
		EntityMetadata<Address> addressMeta = getEntityMetadata(entityClasses, Address.class);
		assertNotNull(addressMeta);
		
		assertEntityMetadata(addressMeta, Address.class, "address", null);
		assertEnttityMetadataFields(
				addressMeta.getFields(), 
				new String[] {"identifier", "lastModified", "people"}, 
				new Class[] {int.class, long.class, List.class}, 
				new String[] {"identifier", "last_modified", "people"}, 
				new Class[] {int.class, long.class, List.class});
	}

	
	private void assertEmailMetadata(Map<Class<?>, EntityMetadata<?>> entityClasses) {
		EntityMetadata<EmailAddress> emailMeta = getEntityMetadata(entityClasses, EmailAddress.class);
		assertNotNull(emailMeta);
		
		assertEntityMetadata(emailMeta, EmailAddress.class, "email", "email");
		assertEnttityMetadataFields(
				emailMeta.getFields(), 
				new String[] {"identifier", "lastModified", "people", "name", "email"}, 
				new Class[] {int.class, long.class, List.class, String.class, String.class}, 
				new String[] {"identifier", "last_modified", "people", "name", "email"}, 
				new Class[] {int.class, long.class, List.class, String.class, String.class}); 
	}


	private void assertStreetAddressMetadata(Map<Class<?>, EntityMetadata<?>> entityClasses) {
		EntityMetadata<StreetAddress> streetAddressMeta = getEntityMetadata(entityClasses, StreetAddress.class);
		assertNotNull(streetAddressMeta);
		
		assertEntityMetadata(streetAddressMeta, StreetAddress.class, "street_address", "address");
		assertEnttityMetadataFields(
				streetAddressMeta.getFields(), 
				new String[] {"identifier", "lastModified", "people", "streetNumber", "street", "zip", "country"}, 
				new Class[] {int.class, long.class, List.class, String.class, String.class, int.class, Country.class}, 
				new String[] {"identifier", "last_modified", "people", "number", "street", "zipcode", "country"}, 
				new Class[] {int.class, long.class, List.class, String.class, String.class, int.class, Integer.class}); 
	}
}
