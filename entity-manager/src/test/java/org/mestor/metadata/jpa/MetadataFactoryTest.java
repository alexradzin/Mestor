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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.mestor.entities.Country;
import org.mestor.entities.annotated.AbstractEntity;
import org.mestor.entities.annotated.Address;
import org.mestor.entities.annotated.DifferentTypesHolder;
import org.mestor.entities.annotated.EmailAddress;
import org.mestor.entities.annotated.NoFieldAttribute;
import org.mestor.entities.annotated.Person;
import org.mestor.entities.annotated.Person.Gender;
import org.mestor.entities.annotated.SimpleProperty;
import org.mestor.entities.annotated.StreetAddress;
import org.mestor.entities.annotated.User;
import org.mestor.entities.annotated.UserRole;
import org.mestor.entities.queries.DuplicateNamedQueriesEntity;
import org.mestor.entities.queries.DuplicateNamedQueriesEntity_2;
import org.mestor.entities.queries.NamedQueriesEntity;
import org.mestor.metadata.EntityMetadata;
import org.mestor.metadata.FieldMetadata;
import org.mestor.metadata.FieldRole;

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
		final Map<Class<?>, EntityMetadata<?>> entityClasses = MetadataFactoryTestUtils.testJpaAnnotations(SimpleProperty.class);

		assertFalse(entityClasses.isEmpty());
		assertEquals(1,  entityClasses.size());

		@SuppressWarnings("unchecked")
		final
		EntityMetadata<SimpleProperty> emd = (EntityMetadata<SimpleProperty>)entityClasses.get(SimpleProperty.class);
		assertNotNull(emd);

		MetadataFactoryTestUtils.assertEntityMetadata(emd, SimpleProperty.class, SimpleProperty.class.getSimpleName(), "simple_property");

		MetadataFactoryTestUtils.assertEntityMetadataFields(
				emd.getFields(),
				new String[] {"name", "type", "value"},
				new Class[] {String.class, Class.class, Serializable.class},
				new String[] {"name", "type", "value"},
				new Class[] {String.class, String.class, ByteBuffer.class});
	}


	@Test
	public void testOneEntityClassWithAttributeWithoutField() {
		final Map<Class<?>, EntityMetadata<?>> entityClasses = MetadataFactoryTestUtils.testJpaAnnotations(NoFieldAttribute.class);

		assertFalse(entityClasses.isEmpty());
		assertEquals(1,  entityClasses.size());

		@SuppressWarnings("unchecked")
		final EntityMetadata<NoFieldAttribute> emd = (EntityMetadata<NoFieldAttribute>)entityClasses.get(NoFieldAttribute.class);
		assertNotNull(emd);

		MetadataFactoryTestUtils.assertEntityMetadata(emd, NoFieldAttribute.class, NoFieldAttribute.class.getSimpleName(), "no_field_attribute");

		MetadataFactoryTestUtils.assertEntityMetadataFields(
				emd.getFields(),
				new String[] {"id", "generic"},
				new Class[] {int.class, String.class},
				new String[] {"id", "generic"},
				new Class[] {int.class, String.class});
	}


	@Test
	public void testEntityWithAttributesOfDifferentTypes() {
		final Map<Class<?>, EntityMetadata<?>> entityClasses = MetadataFactoryTestUtils.testJpaAnnotations(DifferentTypesHolder.class);

		assertFalse(entityClasses.isEmpty());
		assertEquals(1,  entityClasses.size());

		@SuppressWarnings("unchecked")
		final EntityMetadata<DifferentTypesHolder> emd = (EntityMetadata<DifferentTypesHolder>)entityClasses.get(DifferentTypesHolder.class);
		assertNotNull(emd);

		MetadataFactoryTestUtils.assertEntityMetadata(emd, DifferentTypesHolder.class, DifferentTypesHolder.class.getSimpleName(), "different_types_holder");

		MetadataFactoryTestUtils.assertEntityMetadataFields(
				emd.getFields(),
				new String[] {"id", "name", "properties", "integers"},
				new Class[] {int.class, String.class, Map.class, List.class},
				new String[] {"id", "name", "properties", "integers"},
				new Class[] {int.class, String.class, Map.class, List.class});


		MetadataFactoryTestUtils.assertEntityMetadataFieldsGenerics(
				emd.getFields(),
				new String[] {"integers", "properties"},
				new Class<?>[][] {{Integer.class}, {String.class, String.class}},
				new Class<?>[][] {{Integer.class}, {String.class, String.class}}
		);
	}



	@Test
	public void testPersonIndexes() {
		final Map<String, String[]> expected = MetadataFactoryTestUtils.buildStringToStringArrayMap(
				"name", new String[] { "name" },
				"age", new String[] { "age" },
				"full_name", new String[] { "name", "lastName" });
		MetadataFactoryTestUtils.testIndexes(Person.class, expected);
	}

	@Test
	public void testUserClass() {
		final Map<Class<?>, EntityMetadata<?>> entityClasses = MetadataFactoryTestUtils.testJpaAnnotations(User.class, Person.class);

		assertFalse(entityClasses.isEmpty());
		assertEquals(2,  entityClasses.size());

		MetadataFactoryTest.assertUserMetadata(entityClasses);
		MetadataFactoryTest.assertPersonMetadata(entityClasses);
	}



	@Test
	public void testPersonClass() {
		final Map<Class<?>, EntityMetadata<?>> entityClasses = MetadataFactoryTestUtils.testJpaAnnotations(Person.class, User.class, Address.class, StreetAddress.class, EmailAddress.class);

		assertFalse(entityClasses.isEmpty());
		assertEquals(5,  entityClasses.size());

		final Set<String> tables = new HashSet<>();
		for (final EntityMetadata<?> emd : entityClasses.values()) {
			final String tableName = emd.getTableName();
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


	private void assertAddressMetadata(final Map<Class<?>, EntityMetadata<?>> entityClasses) {
		final EntityMetadata<Address> addressMeta = MetadataFactoryTestUtils.getEntityMetadata(entityClasses, Address.class);
		assertNotNull(addressMeta);

		MetadataFactoryTestUtils.assertEntityMetadata(addressMeta, Address.class, Address.class.getSimpleName(), null);
		MetadataFactoryTestUtils.assertEntityMetadataFields(
				addressMeta.getFields(),
				new String[] {"identifier", "lastModified", "people"},
				new Class[] {int.class, long.class, List.class},
				new String[] {"identifier", "last_modified", "people"},
				new Class[] {int.class, long.class, List.class});
	}


	private void assertEmailMetadata(final Map<Class<?>, EntityMetadata<?>> entityClasses) {
		final EntityMetadata<EmailAddress> emailMeta = MetadataFactoryTestUtils.getEntityMetadata(entityClasses, EmailAddress.class);
		assertNotNull(emailMeta);

		MetadataFactoryTestUtils.assertEntityMetadata(emailMeta, EmailAddress.class, "email", "email");
		MetadataFactoryTestUtils.assertEntityMetadataFields(
				emailMeta.getFields(),
				new String[] {"identifier", "lastModified", "people", "name", "email"},
				new Class[] {int.class, long.class, List.class, String.class, String.class},
				new String[] {"identifier", "last_modified", "people", "name", "email"},
				new Class[] {int.class, long.class, List.class, String.class, String.class});
	}


	private void assertStreetAddressMetadata(final Map<Class<?>, EntityMetadata<?>> entityClasses) {
		final EntityMetadata<StreetAddress> streetAddressMeta = MetadataFactoryTestUtils.getEntityMetadata(entityClasses, StreetAddress.class);
		assertNotNull(streetAddressMeta);

		MetadataFactoryTestUtils.assertEntityMetadata(streetAddressMeta, StreetAddress.class, StreetAddress.class.getSimpleName(), "address");

		MetadataFactoryTestUtils.assertEntityMetadataFields(
				streetAddressMeta.getFields(),
				new String[] {"identifier", "lastModified", "people", "streetNumber", "street", "city", "zip", "country"},
				new Class[] {int.class, long.class, List.class, String.class, String.class, String.class, int.class, Country.class},
				new String[] {"identifier", "last_modified", "people", "number", "street", "city", "zipcode", "country"},
				new Class[] {int.class, long.class, List.class, String.class, String.class, String.class, int.class, Integer.class});
	}

	// Assert utilities for individual entity types
	static void assertUserMetadata(final Map<Class<?>, EntityMetadata<?>> entityClasses) {
		final EntityMetadata<User> userMeta = MetadataFactoryTestUtils.getEntityMetadata(entityClasses, User.class);
		assertNotNull(userMeta);

		MetadataFactoryTestUtils.assertEntityMetadata(userMeta, User.class, User.class.getSimpleName(), "user");

		MetadataFactoryTestUtils.assertEntityMetadataFields(
				userMeta.getFields(),
				new String[] {"identifier", "lastModified", "site", "username", "password", "roles", "person"},
				new Class[] {int.class, long.class, URL.class, String.class, String.class, Set.class, Person.class},
				new String[] {"identifier", "last_modified", "site", "username", "password", "roles", "person_identifier"},
				new Class[] {int.class, long.class,   String.class, String.class, String.class, Set.class, int.class});


		MetadataFactoryTestUtils.assertEntityMetadataFieldsGenerics(userMeta.getFields(), new String[] {"roles"}, new Class<?>[][] {{UserRole.class}}, new Class<?>[][] {{Integer.class}});
	}

	static void assertPersonMetadata(final Map<Class<?>, EntityMetadata<?>> entityClasses) {
		final EntityMetadata<Person> personMeta = MetadataFactoryTestUtils.getEntityMetadata(entityClasses, Person.class);
		assertNotNull(personMeta);

		MetadataFactoryTestUtils.assertEntityMetadata(personMeta, Person.class,  Person.class.getSimpleName(), "person");

		MetadataFactoryTestUtils.assertEntityMetadataFields(
				personMeta.getFields(),
				new String[] {"identifier", "lastModified", "name", "lastName", "age", "gender", "addresses", "accounts"},
				new Class[] {int.class, long.class, String.class, String.class, int.class, Gender.class, List.class, List.class},
				new String[] {"identifier", "last_modified", "name", "last_name", "age", "gender", "addresses", "accounts"},
				new Class[] {int.class, long.class, String.class, String.class, int.class, String.class, List.class, List.class});

		MetadataFactoryTestUtils.assertEntityMetadataFieldsGenerics(personMeta.getFields(), new String[] {"addresses", "accounts"}, new Class<?>[][] {{Address.class}, {User.class}}, new Class<?>[][] {{Integer.class}, {Integer.class}});

	}

	@Test
	public void testNoNamedQueries() {
		final Map<Class<?>, EntityMetadata<?>> entityClasses = MetadataFactoryTestUtils.testJpaAnnotations(SimpleProperty.class);
		final EntityMetadata<SimpleProperty> md = MetadataFactoryTestUtils.getEntityMetadata(entityClasses, SimpleProperty.class);
		assertTrue(md.getNamedQueries().isEmpty());
	}

	@Test
	public void testNamedQueries() {
		final Map<Class<?>, EntityMetadata<?>> entityClasses = MetadataFactoryTestUtils.testJpaAnnotations(NamedQueriesEntity.class);
		final EntityMetadata<NamedQueriesEntity> md = MetadataFactoryTestUtils.getEntityMetadata(entityClasses, NamedQueriesEntity.class);
		final Map<String, String> mapQueries = md.getNamedQueries();
		final Map<String, String> expectedMapQueries = new HashMap<>();
		expectedMapQueries.put("selectSorted", "SELECT OBJECT(e) FROM NamedQueriesEntity e ORDER BY e.identifier ASC");
		expectedMapQueries.put("selectAfterId", "SELECT OBJECT(e) FROM NamedQueriesEntity e where e.identifier > :identifier ORDER BY e.identifier ASC");
		expectedMapQueries.put("selectOlderThan", "SELECT OBJECT(e) FROM NamedQueriesEntity e where e.lastModified > ?1 ORDER BY e.identifier ASC");
		expectedMapQueries.put("selectById", "SELECT OBJECT(e) FROM NamedQueriesEntity e where e.identifier = ?1 ORDER BY e.identifier ASC");
		expectedMapQueries.put("selectCount", "SELECT COUNT(e) FROM NamedQueriesEntity e");
		expectedMapQueries.put("selectBySecondaryIndex", "SELECT OBJECT(e) FROM NamedQueriesEntity e where e.lastModified > :date AND name=:name");
		assertEquals(expectedMapQueries, mapQueries);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testDuplicateNamedQueries() {
		MetadataFactoryTestUtils.testJpaAnnotations(DuplicateNamedQueriesEntity.class);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testDuplicateNamedQueries_2() {
		MetadataFactoryTestUtils.testJpaAnnotations(DuplicateNamedQueriesEntity_2.class);
	}


	@Test
	public void testNamedQueriesEntity() {
		final Map<String, FieldRole[]> expected = MetadataFactoryTestUtils.buildStringToStringArrayMap(
				"identifier", new FieldRole[] { FieldRole.PRIMARY_KEY, FieldRole.FILTER, FieldRole.SORTER },
				"lastModified", new FieldRole[] { FieldRole.FILTER }
		);

		MetadataFactoryTestUtils.testFieldRoles(NamedQueriesEntity.class, expected);
	}

	@Test
	public void testIndexesExtractedFromNamedQueries() {
		final Map<Class<?>, EntityMetadata<?>> entityClasses = MetadataFactoryTestUtils.testJpaAnnotations(NamedQueriesEntity.class);
		final EntityMetadata<NamedQueriesEntity> emd = MetadataFactoryTestUtils.getEntityMetadata(entityClasses, NamedQueriesEntity.class);

		final Set<String> filterFields = new HashSet<>();
		for (final FieldMetadata<NamedQueriesEntity, Object, Object> fmd : emd.getFields()) {
			if (fmd.isFilter()) {
				filterFields.add(fmd.getName());
			}
		}

		assertEquals(new HashSet<>(Arrays.asList("identifier", "lastModified", "name")), filterFields);
	}


}
