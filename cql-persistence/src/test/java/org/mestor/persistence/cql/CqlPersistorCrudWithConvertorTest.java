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

package org.mestor.persistence.cql;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javassist.util.proxy.ProxyObject;

import javax.persistence.AttributeConverter;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mestor.cassandra.CassandraAwareTestRunner;
import org.mestor.context.Persistor;
import org.mestor.metadata.EntityMetadata;
import org.mestor.metadata.FieldMetadata;
import org.mestor.metadata.jpa.conversion.DummyAttributeConverter;
import org.mestor.metadata.jpa.conversion.EnumNameConverter;
import org.mestor.metadata.jpa.conversion.EnumOrdinalConverter;
import org.mestor.metadata.jpa.conversion.PrimaryKeyConverter;
import org.mestor.metadata.jpa.conversion.ValueAttributeConverter;
import org.mestor.testEntities.Person;
import org.mestor.testEntities.Person.Gender;
import org.mestor.testEntities.Person.ParentRole;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;

@RunWith(CassandraAwareTestRunner.class)
public class CqlPersistorCrudWithConvertorTest {
	private final Persistor persistor;
	private final CqlPersistorTestHelper helper;

	
	public CqlPersistorCrudWithConvertorTest() throws IOException {
		helper = new CqlPersistorTestHelper();
		persistor = helper.getPersistor();
		doReturn(helper.getPersistor()).when(helper.ctx).getPersistor();
	}

	
	@Test
	public void testCrudWithEnumToOrdinalField() {
		testCrudWithEnumField(Integer.class, new EnumOrdinalConverter<Gender>(Gender.class));
	}

	@Test
	public void testCrudWithEnumToNameField() {
		testCrudWithEnumField(String.class, new EnumNameConverter<Gender>(Gender.class));
	}
	
	private <C> void testCrudWithEnumField(Class<C> columnType, AttributeConverter<Gender, C> converter) {
		final String schemaName = "test1";
		try {
			helper.testCreateSchema(schemaName, null, false);
			FieldMetadata<Person, Integer, Integer> pk = helper.createFieldMetadata(Person.class, int.class, "id", "identifier", true);
			
			FieldMetadata<Person, Gender, C> genderField = helper.createFieldMetadata(Person.class, Gender.class, "gender", "gender", false);
			genderField.setColumnType(columnType);
			genderField.setConverter(new ValueAttributeConverter<>(converter));
			
			
			@SuppressWarnings("unchecked")
			FieldMetadata<Person, Object, Object>[] fields = new FieldMetadata[] {
					pk,
					helper.createFieldMetadata(Person.class, String.class, "name", "first_name", false),
					helper.createFieldMetadata(Person.class, String.class, "lastName", "last_name", false),
					genderField,
			};
			
			
			
			EntityMetadata<Person> emd = helper.createMetadata(Person.class, schemaName, "People", pk, fields);
			helper.testEditTable(emd, null, true);
			
			
			doReturn(emd).when(helper.ctx).getEntityMetadata(Person.class);
			
			
			Person person = new Person();
			person.setId(1);
			person.setName("Yaron");
			person.setLastName("Cohen");
			person.setGender(Gender.MALE);
			
			assertNull(persistor.fetch(Person.class, 1));

			persistor.store(person);
			Person person2 = persistor.fetch(Person.class, 1);
			assertNotNull(person2);
			assertEquals(1, person2.getId());
			assertEquals("Yaron", person2.getName());
			assertEquals("Cohen", person2.getLastName());
			assertEquals(Gender.MALE, person2.getGender());
			assertTrue(person2 instanceof ProxyObject);
			
			person2.setName("Sharon");
			person2.setGender(Gender.FEMALE); 
			persistor.store(person2);
			Person person3 = persistor.fetch(Person.class, 1);
			assertEquals("Sharon", person3.getName());
			assertEquals(Gender.FEMALE, person3.getGender());
			
			assertNull(persistor.fetch(Person.class, 2));
			
			person3.setName("Dana");
			person3.setLastName("International");
			
			persistor.store(person3);
			
			Person person4 = persistor.fetch(Person.class, 1);
			assertEquals("Dana", person4.getName());
			assertEquals("International", person4.getLastName());
			assertEquals(Gender.FEMALE, person4.getGender());
			
			
			persistor.remove(person4);
			assertNull(persistor.fetch(Person.class, 1));
		} finally {
			persistor.dropSchema(schemaName);
		}
	}

	@Test
	public void testCrudWithSingleRelationship() {
		final String schemaName = "test1";
		try {
			helper.testCreateSchema(schemaName, null, false);
			FieldMetadata<Person, Integer, Integer> pk = helper.createFieldMetadata(Person.class, int.class, "id", "identifier", true);
			
			FieldMetadata<Person, Gender, String> genderField = helper.createFieldMetadata(Person.class, Gender.class, "gender", "gender", false);
			genderField.setColumnType(String.class);
			genderField.setConverter(new ValueAttributeConverter<>(new EnumNameConverter<Gender>(Gender.class)));

			
			FieldMetadata<Person, Person, Integer> spouseField = helper.createFieldMetadata(Person.class, Person.class, "spouse", "spouse", false);
			spouseField.setColumnType(Integer.class);
			spouseField.setConverter(new ValueAttributeConverter<>(new PrimaryKeyConverter<Person, Integer>(Person.class, helper.ctx)));
			
			
			@SuppressWarnings("unchecked")
			FieldMetadata<Person, Object, Object>[] fields = new FieldMetadata[] {
					pk,
					helper.createFieldMetadata(Person.class, String.class, "name", "first_name", false),
					helper.createFieldMetadata(Person.class, String.class, "lastName", "last_name", false),
					genderField,
					spouseField
			};
			
			
			
			EntityMetadata<Person> emd = helper.createMetadata(Person.class, schemaName, "People", pk, fields);
			helper.testEditTable(emd, null, true);
			
			
			doReturn(emd).when(helper.ctx).getEntityMetadata(Person.class);
			
			
			
			Person barakObama = new Person(1, "Barak", "Obama", Gender.MALE);
			Person michelleObama = new Person(2, "Michelle", "Obama", Gender.FEMALE);
			
			barakObama.setSpouse(michelleObama);
			michelleObama.setSpouse(barakObama);

			assertNull(persistor.fetch(Person.class, 1));
			assertNull(persistor.fetch(Person.class, 2));
			
			persistor.store(barakObama);
			persistor.store(michelleObama);
			
			Person barakObama2 = persistor.fetch(Person.class, 1);
			Person michelleObama2 = persistor.fetch(Person.class, 2);
			
			assertNotNull(barakObama2);
			assertNotNull(michelleObama2);
			
			assertEquals("Barak", barakObama2.getName());
			assertEquals("Obama", barakObama2.getLastName());
			assertEquals(Gender.MALE, barakObama2.getGender());

			assertEquals("Michelle", michelleObama2.getName());
			assertEquals("Obama", michelleObama2.getLastName());
			assertEquals(Gender.FEMALE, michelleObama2.getGender());

			assertNotNull(barakObama2.getSpouse());
			assertNotNull(michelleObama2.getSpouse());
			
			assertEquals("Michelle", barakObama2.getSpouse().getName());
			assertEquals("Barak", michelleObama2.getSpouse().getName());
			
			
		} finally {
			persistor.dropSchema(schemaName);
		}
	}

	@SuppressWarnings("rawtypes")
	@Test
	public void testCrudWithRelationships() {
		final String schemaName = "test1";
		try {
			helper.testCreateSchema(schemaName, null, false);
			FieldMetadata<Person, Integer, Integer> pk = helper.createFieldMetadata(Person.class, int.class, "id", "identifier", true);
			
			FieldMetadata<Person, Gender, String> genderField = helper.createFieldMetadata(Person.class, Gender.class, "gender", "gender", false);
			genderField.setColumnType(String.class);
			genderField.setConverter(new ValueAttributeConverter<>(new EnumNameConverter<Gender>(Gender.class)));

			
			FieldMetadata<Person, Person, Integer> spouseField = helper.createFieldMetadata(Person.class, Person.class, "spouse", "spouse", false);
			spouseField.setColumnType(Integer.class);
			spouseField.setConverter(new ValueAttributeConverter<>(new PrimaryKeyConverter<Person, Integer>(Person.class, helper.ctx)));

			
			FieldMetadata<Person, List, List> childrenField = helper.createFieldMetadata(Person.class, List.class, new Class[]{Person.class}, new Class[]{Integer.class},  "children", "children", false);
			childrenField.setColumnType(List.class);
			childrenField.setConverter(new ValueAttributeConverter<>(new DummyAttributeConverter<List, List>()), new ValueAttributeConverter<>(new PrimaryKeyConverter<Person, Integer>(Person.class, helper.ctx)));
			

			FieldMetadata<Person, Map, Map> parentsField = helper.createFieldMetadata(Person.class, Map.class, new Class[]{ParentRole.class, Person.class}, new Class[]{String.class, Integer.class},  "parents", "parents", false);
			parentsField.setColumnType(Map.class);
			parentsField.setConverter(
					new ValueAttributeConverter<>(new DummyAttributeConverter<Map, Map>()), 
					new ValueAttributeConverter<>(new EnumNameConverter<>(ParentRole.class)), 
					new ValueAttributeConverter<>(new PrimaryKeyConverter<Person, Integer>(Person.class, helper.ctx))
			);
			
			
			
			@SuppressWarnings("unchecked")
			FieldMetadata<Person, Object, Object>[] fields = new FieldMetadata[] {
					pk,
					helper.createFieldMetadata(Person.class, String.class, "name", "first_name", false),
					helper.createFieldMetadata(Person.class, String.class, "lastName", "last_name", false),
					genderField,
					spouseField,
					childrenField,
					parentsField
			};
			
			
			
			EntityMetadata<Person> emd = helper.createMetadata(Person.class, schemaName, "People", pk, fields);
			helper.testEditTable(emd, null, true);
			
			
			doReturn(emd).when(helper.ctx).getEntityMetadata(Person.class);
			
			
			
			
			Person barakObama = new Person(1, "Barak", "Obama", Gender.MALE);
			Person michelleObama = new Person(2, "Michelle", "Obama", Gender.FEMALE);
			
			Person maliaObama = new Person(3, "Malia", "Obama", Gender.FEMALE);
			Person sashaObama = new Person(4, "Sasha", "Obama", Gender.FEMALE);
			List<Person> children = Arrays.asList(maliaObama, sashaObama);
			List<Person> people = Arrays.asList(barakObama, michelleObama, maliaObama, sashaObama);
			Map<ParentRole, Person> parents = new HashMap<>();
			parents.put(ParentRole.MOTHER, michelleObama);
			parents.put(ParentRole.FATHER, barakObama);
			
			barakObama.setSpouse(michelleObama);
			michelleObama.setSpouse(barakObama);
			
			barakObama.setChildren(children);
			michelleObama.setChildren(children);
			
			maliaObama.setParents(parents);
			sashaObama.setParents(parents);

			for (Person p : people) {
				assertNull(persistor.fetch(Person.class, p.getId()));
			}
			
			for (Person p : people) {
				persistor.store(p);
			}
			
			Person barakObama2 = persistor.fetch(Person.class, 1);
			Person michelleObama2 = persistor.fetch(Person.class, 2);
			Person maliaObama2 = persistor.fetch(Person.class, 3);
			Person sashaObama2 = persistor.fetch(Person.class, 4);
			List<Person> people2 = Arrays.asList(barakObama2, michelleObama2, maliaObama2, sashaObama2);
			
			for (Person p : people2) {
				assertNotNull(p);
			}
			
			assertEquals("Barak", barakObama2.getName());
			assertEquals("Obama", barakObama2.getLastName());
			assertEquals(Gender.MALE, barakObama2.getGender());

			assertEquals("Michelle", michelleObama2.getName());
			assertEquals("Obama", michelleObama2.getLastName());
			assertEquals(Gender.FEMALE, michelleObama2.getGender());

			assertNotNull(barakObama2.getSpouse());
			assertNotNull(michelleObama2.getSpouse());
			
			assertEquals("Michelle", barakObama2.getSpouse().getName());
			assertEquals("Barak", michelleObama2.getSpouse().getName());
			
			assertEquals("Malia", maliaObama2.getName());
			assertEquals("Obama", maliaObama2.getLastName());
			assertEquals(Gender.FEMALE, maliaObama2.getGender());

			assertEquals("Sasha", sashaObama2.getName());
			assertEquals("Obama", sashaObama2.getLastName());
			assertEquals(Gender.FEMALE, sashaObama2.getGender());
			
			Collection<String> childrenNames = getPeopleNames(children);
			
			Collection<String> barackChildrenNames = getPeopleNames(barakObama2.getChildren());
			Collection<String> michelleChildrenNames = getPeopleNames(michelleObama2.getChildren());
			
			
			assertArrayEquals(childrenNames.toArray(), barackChildrenNames.toArray());
			assertArrayEquals(childrenNames.toArray(), michelleChildrenNames.toArray());
			
			
			for(Person c : new Person[] {maliaObama2, sashaObama2}) {
				assertEquals("Barak", c.getParents().get(ParentRole.FATHER).getName());
				assertEquals("Michelle", c.getParents().get(ParentRole.MOTHER).getName());
			}
			
		} finally {
			persistor.dropSchema(schemaName);
		}
	}
	
	private Collection<String> getPeopleNames(Collection<Person> people) {
		return Collections2.transform(people, new Function<Person, String>() {
			@Override
			public String apply(Person p) {
				return p.getName();
			}
		});
	}
	
}
