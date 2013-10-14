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

import java.io.Serializable;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
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

/**
 * This test case contains tests that use annotations to extract metadata of given entities
 * and verify that metadata is as expected. The tests do not actually perform persistence 
 * operations because core module does not depend on any persistence implementation.  
 * @author alexr
 */
public class MetadataFactoryTest {
	@Test
	public void testNoClasses() {
		assertTrue(MetadataFactoryTestUtils.testJpaAnnotations().isEmpty());
	}
	
	@Test
	public void testNotEntityClass() {
		assertTrue(MetadataFactoryTestUtils.testJpaAnnotations(String.class).isEmpty());
		assertTrue(MetadataFactoryTestUtils.testJpaAnnotations(AbstractEntity.class).isEmpty());
	}

	
	@Test
	public void testOneSimpleEntityClass() {
		Map<Class<?>, EntityMetadata<?>> entityClasses = MetadataFactoryTestUtils.testJpaAnnotations(SimpleProperty.class);

		assertFalse(entityClasses.isEmpty());
		assertEquals(1,  entityClasses.size());
		
		@SuppressWarnings("unchecked")
		EntityMetadata<SimpleProperty> emd = (EntityMetadata<SimpleProperty>)entityClasses.get(SimpleProperty.class);
		assertNotNull(emd);
		
		MetadataFactoryTestUtils.assertEntityMetadata(emd, SimpleProperty.class, "simple_property", "simple_property");
		MetadataFactoryTestUtils.assertEntityMetadataFields(
				emd.getFields(), 
				new String[] {"name", "type", "value"}, 
				new Class[] {String.class, Class.class, Serializable.class}, 
				new String[] {"name", "type", "value"}, 
				new Class[] {String.class, String.class, ByteBuffer.class});
	}
	
	@Test
	public void testPersonIndexes() {
		Map<String, String[]> expected = MetadataFactoryTestUtils.buildStringToStringArrayMap(
				"name", new String[] { "name" },
				"age", new String[] { "age" },
				"full_name", new String[] { "name", "lastName" });
		MetadataFactoryTestUtils.testIndexes(Person.class, expected);
	}

	@Test
	public void testUserClass() {
		Map<Class<?>, EntityMetadata<?>> entityClasses = MetadataFactoryTestUtils.testJpaAnnotations(User.class, Person.class);

		assertFalse(entityClasses.isEmpty());
		assertEquals(2,  entityClasses.size());
		
		MetadataFactoryTest.assertUserMetadata(entityClasses);
		MetadataFactoryTest.assertPersonMetadata(entityClasses);
	}
	
	
	
	@Test
	public void testPersonClass() {
		Map<Class<?>, EntityMetadata<?>> entityClasses = MetadataFactoryTestUtils.testJpaAnnotations(Person.class, User.class, Address.class, StreetAddress.class, EmailAddress.class);

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
		
		
		MetadataFactoryTest.assertUserMetadata(entityClasses);
		MetadataFactoryTest.assertPersonMetadata(entityClasses);
		assertAddressMetadata(entityClasses);
		assertEmailMetadata(entityClasses);
		assertStreetAddressMetadata(entityClasses);
	}
	
	private void assertAddressMetadata(Map<Class<?>, EntityMetadata<?>> entityClasses) {
		EntityMetadata<Address> addressMeta = MetadataFactoryTestUtils.getEntityMetadata(entityClasses, Address.class);
		assertNotNull(addressMeta);
		
		MetadataFactoryTestUtils.assertEntityMetadata(addressMeta, Address.class, "address", null);
		MetadataFactoryTestUtils.assertEntityMetadataFields(
				addressMeta.getFields(), 
				new String[] {"identifier", "lastModified", "people"}, 
				new Class[] {int.class, long.class, List.class}, 
				new String[] {"identifier", "last_modified", "people"}, 
				new Class[] {int.class, long.class, List.class});
	}

	
	private void assertEmailMetadata(Map<Class<?>, EntityMetadata<?>> entityClasses) {
		EntityMetadata<EmailAddress> emailMeta = MetadataFactoryTestUtils.getEntityMetadata(entityClasses, EmailAddress.class);
		assertNotNull(emailMeta);
		
		MetadataFactoryTestUtils.assertEntityMetadata(emailMeta, EmailAddress.class, "email", "email");
		MetadataFactoryTestUtils.assertEntityMetadataFields(
				emailMeta.getFields(), 
				new String[] {"identifier", "lastModified", "people", "name", "email"}, 
				new Class[] {int.class, long.class, List.class, String.class, String.class}, 
				new String[] {"identifier", "last_modified", "people", "name", "email"}, 
				new Class[] {int.class, long.class, List.class, String.class, String.class}); 
	}


	private void assertStreetAddressMetadata(Map<Class<?>, EntityMetadata<?>> entityClasses) {
		EntityMetadata<StreetAddress> streetAddressMeta = MetadataFactoryTestUtils.getEntityMetadata(entityClasses, StreetAddress.class);
		assertNotNull(streetAddressMeta);
		
		MetadataFactoryTestUtils.assertEntityMetadata(streetAddressMeta, StreetAddress.class, "street_address", "address");
		MetadataFactoryTestUtils.assertEntityMetadataFields(
				streetAddressMeta.getFields(), 
				new String[] {"identifier", "lastModified", "people", "streetNumber", "street", "zip", "country"}, 
				new Class[] {int.class, long.class, List.class, String.class, String.class, int.class, Country.class}, 
				new String[] {"identifier", "last_modified", "people", "number", "street", "zipcode", "country"}, 
				new Class[] {int.class, long.class, List.class, String.class, String.class, int.class, Integer.class}); 
	}

	// Assert utilities for individual entity types
	static void assertUserMetadata(Map<Class<?>, EntityMetadata<?>> entityClasses) {
		EntityMetadata<User> userMeta = MetadataFactoryTestUtils.getEntityMetadata(entityClasses, User.class);
		assertNotNull(userMeta);
		
		MetadataFactoryTestUtils.assertEntityMetadata(userMeta, User.class, "user", "user");
		MetadataFactoryTestUtils.assertEntityMetadataFields(
				userMeta.getFields(), 
				new String[] {"identifier", "lastModified", "site", "username", "password", "roles", "person"}, 
				new Class[] {int.class, long.class, URL.class, String.class, String.class, Set.class, Person.class}, 
				new String[] {"identifier", "last_modified", "site", "username", "password", "roles", "person_identifier"},  
				new Class[] {int.class, long.class,   String.class, String.class, String.class, Set.class, int.class});
	}

	static void assertPersonMetadata(Map<Class<?>, EntityMetadata<?>> entityClasses) {
		EntityMetadata<Person> personMeta = MetadataFactoryTestUtils.getEntityMetadata(entityClasses, Person.class);
		assertNotNull(personMeta);
		
		MetadataFactoryTestUtils.assertEntityMetadata(personMeta, Person.class, "person", "person");
		MetadataFactoryTestUtils.assertEntityMetadataFields(
				personMeta.getFields(), 
				new String[] {"identifier", "lastModified", "name", "lastName", "age", "gender", "addresses", "accounts"}, 
				new Class[] {int.class, long.class, String.class, String.class, int.class, Gender.class, List.class, List.class}, 
				new String[] {"identifier", "last_modified", "name", "last_name", "age", "gender", "addresses", "accounts"}, 
				new Class[] {int.class, long.class, String.class, String.class, int.class, String.class, List.class, List.class});
	}
}
