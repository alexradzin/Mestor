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

package org.mestor.em;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;

import java.util.Arrays;
import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;
import org.mestor.context.DirtyEntityManager;
import org.mestor.context.EntityContext;
import org.mestor.context.Persistor;
import org.mestor.entities.Person;
import org.mestor.entities.Person.Gender;
import org.mestor.metadata.EntityMetadata;
import org.mestor.metadata.FieldMetadata;
import org.mestor.util.ReflectiveBean;
import org.mestor.wrap.ObjectWrapperFactory;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class DirtyEntityManagerTest {
	@Mock private EntityContext ctx;
	@Mock private Persistor persistor;
	@Mock private ObjectWrapperFactory<Person> owf;
	
	private DirtyEntityManager dem; 

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		doReturn(persistor).when(ctx).getPersistor();
		doReturn(owf).when(persistor).getObjectWrapperFactory(Person.class);
		
		dem = new EntityTransactionImpl(ctx);
		assertFalse(dem.getDirtyEntities().iterator().hasNext());
	}
	
	@Test
	public void testSetEachFieldsSameValue() {
		final EntityMetadata<Person> emd = createPersonMetadata();
		final Person person = new Person(1, "Yaron", "Cohen", Gender.MALE);
		doReturn(false).when(owf).isWrapped(person);
		doReturn(emd).when(ctx).getEntityMetadata(Person.class);

		assertFalse(dem.getDirtyEntities().iterator().hasNext());
		
		for (FieldMetadata<Person, Object, Object> fmd : emd.getFields()) {
			dem.addDirtyEntity(person, fmd);
			assertPerson(person);
		}
		
		dem.removeDirtyEntity(person);
		assertFalse(dem.getDirtyEntities().iterator().hasNext());
	}

	@Test
	public void testSetOneFieldOtherValue() {
		final EntityMetadata<Person> emd = createPersonMetadata();
		final Person person = new Person(1, "Yaron", "Cohen", Gender.MALE);
		doReturn(false).when(owf).isWrapped(person);
		doReturn(emd).when(ctx).getEntityMetadata(Person.class);

		assertFalse(dem.getDirtyEntities().iterator().hasNext());
		
		person.setGender(Gender.FEMALE);
		dem.addDirtyEntity(person, emd.getFieldByName("gender"));
		
		assertPerson(person);
	}
	

	@Test
	public void testSetSeveralFieldsOtherValue() {
		final EntityMetadata<Person> emd = createPersonMetadata();
		final Person person = new Person(1, "Yaron", "Cohen", Gender.MALE);
		doReturn(false).when(owf).isWrapped(person);
		doReturn(emd).when(ctx).getEntityMetadata(Person.class);

		assertFalse(dem.getDirtyEntities().iterator().hasNext());
		
		person.setGender(Gender.FEMALE);
		dem.addDirtyEntity(person, emd.getFieldByName("gender"));

		person.setName("Sharon");
		dem.addDirtyEntity(person, emd.getFieldByName("name"));
		
		
		assertPerson(person);
	}
	
	@Test
	public void testSetSeveralFieldsOtherValueSeveralTimes() {
		final EntityMetadata<Person> emd = createPersonMetadata();
		final Person person = new Person(1, "Yaron", "Cohen", Gender.MALE);
		doReturn(false).when(owf).isWrapped(person);
		doReturn(emd).when(ctx).getEntityMetadata(Person.class);

		assertFalse(dem.getDirtyEntities().iterator().hasNext());
		
		person.setGender(Gender.FEMALE);
		dem.addDirtyEntity(person, emd.getFieldByName("gender"));

		person.setName("Sharon");
		dem.addDirtyEntity(person, emd.getFieldByName("name"));

		// Scenic name
		person.setName("Dana");
		dem.addDirtyEntity(person, emd.getFieldByName("name"));
		
		person.setLastName("International");
		dem.addDirtyEntity(person, emd.getFieldByName("lastName"));
		
		assertPerson(person);
	}


	/**
	 * Create 2 instances of the same class that represent the same entity because 
	 * their ids are equal. Make different changes in both objects. 
	 * See that there is only 1 dirty entities that contains all changes. 
	 */
	@Test
	public void testSetSeveralFieldsUsingDifferentInstances() {
		final EntityMetadata<Person> emd = createPersonMetadata();
		doReturn(emd).when(ctx).getEntityMetadata(Person.class);

		final Person real = new Person(1, "Yaron", "Cohen", Gender.MALE);
		doReturn(false).when(owf).isWrapped(real);

		final Person singer = new Person(1, "Sharon", "Cohen", Gender.FEMALE);
		doReturn(false).when(owf).isWrapped(singer);

		
		assertFalse(dem.getDirtyEntities().iterator().hasNext());
		
		real.setGender(Gender.FEMALE);
		dem.addDirtyEntity(real, emd.getFieldByName("gender"));

		real.setName("Sharon");
		dem.addDirtyEntity(real, emd.getFieldByName("name"));

		// Scenic name
		singer.setName("Dana");
		dem.addDirtyEntity(singer, emd.getFieldByName("name"));
		
		singer.setLastName("International");
		dem.addDirtyEntity(singer, emd.getFieldByName("lastName"));
		
		assertPerson(singer);
		
		
		assertPerson(real, 1, "Sharon", "Cohen", Gender.FEMALE);
		assertPerson(singer, 1, "Dana", "International", Gender.FEMALE);
	}
	
	
	
	private EntityMetadata<Person> createPersonMetadata() {
		ReflectiveBean.getField(Person.class, "id");
		
		final EntityMetadata<Person> emd = new EntityMetadata<Person>(Person.class);
		final FieldMetadata<Person, Integer, Integer> metaId = createFieldMetadata(Person.class, Integer.class, "id");
		metaId.setKey(true);
		final FieldMetadata<Person, String, String> metaName = createFieldMetadata(Person.class, String.class, "name");
		final FieldMetadata<Person, String, String> metaLastName = createFieldMetadata(Person.class, String.class, "lastName");
		final FieldMetadata<Person, Gender, String> metaGender = createFieldMetadata(Person.class, Gender.class, "gender");
		emd.addAllFields(Arrays.<FieldMetadata<Person, ?, ?>>asList(metaId, metaName, metaLastName, metaGender));
		emd.setPrimaryKey(metaId);
		
		return emd;
	}
	
	private <E, F, C> FieldMetadata<E, F, C> createFieldMetadata(Class<E> clazz, Class<F> fieldType, String name) {
		return new FieldMetadata<E, F, C>(
				clazz, 
				fieldType, 
				name, 
				ReflectiveBean.getField(Person.class, name),
				ReflectiveBean.getGetter(Person.class, name),
				ReflectiveBean.getSetter(Person.class, fieldType, name)
		);
	}

	
	private void assertPerson(Person person) {
		Iterator<?> itDirty = dem.getDirtyEntities().iterator();
		assertTrue(itDirty.hasNext());
		Person dirtyPerson = (Person)itDirty.next();
		assertPerson(dirtyPerson, person.getId(), person.getName(), person.getLastName(), person.getGender());
		assertFalse(itDirty.hasNext());
	}
	
	
	
	private void assertPerson(Person person, int expectedId, String expectedName, String expectedLastName, Gender expectedGender) {
		assertEquals(expectedId, person.getId());
		assertEquals(expectedName, person.getName());
		assertEquals(expectedLastName, person.getLastName());
		assertEquals(expectedGender, person.getGender());
	}
}
