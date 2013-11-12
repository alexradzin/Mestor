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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doReturn;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mestor.entities.Person;
import org.mestor.entities.Person.Gender;
import org.mestor.entities.Person.ParentRole;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.base.Function;

@RunWith(MockitoJUnitRunner.class)
public class EntityTraverserTest {
	private static class TraceFunction implements Function<Object, Object> {
		final Set<Integer> visitedEntities = new HashSet<>();

		@Override
		public Object apply(@Nullable Object input) {
			assertNotNull(input);
			return visitedEntities.add(System.identityHashCode(input));
		}
	}



	@Test
	public <E> void testNotAnEntity() {
		test("", CascadeOption.FETCH, new Integer[0]);
	}

	@Test
	public <E> void testNotInitializedEntity() {
		Person p = new Person();
		test(p, CascadeOption.FETCH, getIdentities(p));
	}

	@Test
	public <E> void testEntityWithoutRelationships() {
		Person bill = new Person(1, "Bill", "Gates", Gender.MALE);
		test(bill, CascadeOption.FETCH, getIdentities(bill));
	}

	@Test
	public <E> void testEntityWithRelationships() {
		Person bill = new Person(1, "Bill", "Gates", Gender.MALE);
		Person melinda = new Person(2, "Melinda", "Gates", Gender.FEMALE);

		bill.setSpouse(melinda);
		melinda.setSpouse(bill);

		test(bill, CascadeOption.FETCH, getIdentities(bill, melinda));
		test(bill, CascadeOption.PERSIST, getIdentities(bill));
	}

	@Test
	public <E> void testWithCollectionsRelationships() {
		int id = 0;
		Person bill = new Person(++id, "Bill", "Gates", Gender.MALE);
		Person melinda = new Person(++id, "Melinda", "Gates", Gender.FEMALE);

		Person jennifer = new Person(++id, "Jennifer", "Gates", Gender.FEMALE);
		Person rory = new Person(++id, "Rory", "Gates", Gender.FEMALE);
		Person phoebe = new Person(++id, "Phoebe", "Gates", Gender.MALE);
		List<Person> children = Arrays.asList(jennifer, rory, phoebe);

		bill.setSpouse(melinda);
		melinda.setSpouse(bill);

		bill.setChildren(children);
		melinda.setChildren(children);

		test(bill, CascadeOption.FETCH, getIdentities(bill, melinda, jennifer, rory, phoebe));
		test(bill, CascadeOption.PERSIST, getIdentities(bill));
	}


	@Test
	public <E> void testWithMapRelationships() {
		int id = 0;
		Person bill = new Person(++id, "Bill", "Gates", Gender.MALE);

		Person william = new Person(++id, "William", "Gates", Gender.MALE);
		Person mary = new Person(++id, "Mary", "Gates", Gender.FEMALE);

		Map<ParentRole, Person> parents = new HashMap<>();
		parents.put(ParentRole.FATHER, william);
		parents.put(ParentRole.MOTHER, mary);

		bill.setParents(parents);

		test(bill, CascadeOption.FETCH, getIdentities(bill));
		test(bill, CascadeOption.PERSIST, getIdentities(bill, william, mary));
	}

	@Test
	public <E> void testWithCollectionsAndMapsBidirectionalRelationships() {
		int id = 0;
		Person bill = new Person(++id, "Bill", "Gates", Gender.MALE);
		Person melinda = new Person(++id, "Melinda", "Gates", Gender.FEMALE);

		Person jennifer = new Person(++id, "Jennifer", "Gates", Gender.FEMALE);
		Person rory = new Person(++id, "Rory", "Gates", Gender.FEMALE);
		Person phoebe = new Person(++id, "Phoebe", "Gates", Gender.MALE);
		List<Person> children = Arrays.asList(jennifer, rory, phoebe);

		bill.setSpouse(melinda);
		melinda.setSpouse(bill);

		bill.setChildren(children);
		melinda.setChildren(children);

		Map<ParentRole, Person> parents = new HashMap<>();
		parents.put(ParentRole.FATHER, bill);
		parents.put(ParentRole.MOTHER, melinda);

		jennifer.setParents(parents);
		rory.setParents(parents);
		phoebe.setParents(parents);

		test(bill, CascadeOption.FETCH, getIdentities(bill, melinda, jennifer, rory, phoebe));
		test(bill, CascadeOption.PERSIST, getIdentities(bill));

		test(jennifer, CascadeOption.PERSIST, getIdentities(jennifer, bill, melinda));
		test(rory, CascadeOption.PERSIST, getIdentities(rory, bill, melinda));
		test(phoebe, CascadeOption.PERSIST, getIdentities(phoebe, bill, melinda));
	}


	private <E> void test(E entity, CascadeOption currentCascade, Integer[] expectedVisitedEntityIdentities) {
		MetadataTestHelper helper = new MetadataTestHelper();
//		Persistor persistor = Mockito.mock(Persistor.class);
//		doReturn(persistor).when(helper.ctx).getPersistor();



		FieldMetadata<Person, Integer, Integer> pk = helper.createFieldMetadata(Person.class, int.class, "id", "identifier", true);


		FieldMetadata<Person, Person, Person> spouseField = helper.createFieldMetadata(Person.class, Person.class, "spouse", "spouse", false);
		spouseField.setCascade(CascadeOption.FETCH, true);

		@SuppressWarnings("rawtypes")
		FieldMetadata<Person, List, List> childrenField = helper.createFieldMetadata(Person.class, List.class, new Class[]{Person.class}, new Class[]{Integer.class},  "children", "children", false);
		childrenField.setCascade(CascadeOption.FETCH, true);

		@SuppressWarnings("rawtypes")
		FieldMetadata<Person, Map, Map> parentsField = helper.createFieldMetadata(Person.class, Map.class, new Class[]{ParentRole.class, Person.class}, new Class[]{String.class, Integer.class},  "parents", "parents", false);
		parentsField.setCascade(CascadeOption.PERSIST, true);

		@SuppressWarnings("unchecked")
		FieldMetadata<Person, Object, Object>[] fields = new FieldMetadata[] {
				pk,
				helper.createFieldMetadata(Person.class, String.class, "name", "first_name", false),
				helper.createFieldMetadata(Person.class, String.class, "lastName", "last_name", false),
				helper.createFieldMetadata(Person.class, Gender.class, "gender", "gender", false),
				spouseField,
				childrenField,
				parentsField
		};


		EntityMetadata<Person> emd = helper.createMetadata(Person.class, "traverse-test", "People", pk, fields);
		doReturn(null).when(helper.ctx).getEntityMetadata(Person.class);


		EntityTraverser entityTraverser = new EntityTraverser(helper.ctx);

		doReturn(emd).when(helper.ctx).getEntityMetadata(Person.class);

		TraceFunction action = new TraceFunction();
		entityTraverser.traverse(entity, currentCascade, action);
		assertEquals(new HashSet<Integer>(Arrays.asList(expectedVisitedEntityIdentities)), action.visitedEntities);
	}

	private static Integer[] getIdentities(Object ... objects) {
		if (objects == null) {
			return null;
		}
		Integer[] identities = new Integer[objects.length];
		for (int i = 0; i < objects.length; i++) {
			identities[i] = System.identityHashCode(objects[i]);
		}
		return identities;
	}
}
