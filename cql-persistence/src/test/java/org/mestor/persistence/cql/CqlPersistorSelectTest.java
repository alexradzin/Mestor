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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mestor.cassandra.CassandraAwareTestRunner;
import org.mestor.context.Persistor;
import org.mestor.entities.Person;
import org.mestor.entities.Person.Gender;
import org.mestor.metadata.EntityMetadata;
import org.mestor.metadata.FieldMetadata;
import org.mestor.query.ClauseInfo;
import org.mestor.query.ClauseInfo.Operand;
import org.mestor.query.QueryInfo;
import org.mestor.query.QueryInfo.QueryType;
import org.mestor.util.ReflectiveBean;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

@RunWith(CassandraAwareTestRunner.class)
public class CqlPersistorSelectTest {
	private final Persistor persistor;
	private final CqlPersistorTestHelper helper;

	public CqlPersistorSelectTest() throws IOException {
		helper = new CqlPersistorTestHelper();
		persistor = helper.getPersistor();
		doReturn(helper.getPersistor()).when(helper.ctx).getPersistor();
	}

	@Test
	public void testSelectNoData() {
		final String schemaName = "test1";
		try {
			initPersonMetadata(schemaName);
			final List<Person> people = persistor.selectQuery(new QueryInfo(QueryType.SELECT, null, Collections.singletonMap("People", "People")), null);
			assertNotNull(people);
			assertTrue(people.isEmpty());
		} finally {
			persistor.dropSchema(schemaName);
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSelectWrongEntityName() {
		final String schemaName = "test1";
		try {
			initPersonMetadata(schemaName);
			persistor.selectQuery(new QueryInfo(QueryType.SELECT, null, Collections.singletonMap("DoesNotExist", "DoesNotExist")), null);
		} finally {
			persistor.dropSchema(schemaName);
		}
	}

	@Test
	public void testSingleSelectNoCriteria() {
		final String schemaName = "test1";
		try {
			initPersonMetadata(schemaName);

			final Person person = new Person();
			person.setId(1);

			persistor.store(person);

			final List<Person> people = persistor.selectQuery(new QueryInfo(QueryType.SELECT, null, Collections.singletonMap("People", "People")), null);
			assertNotNull(people);
			assertFalse(people.isEmpty());

			assertEquals(1, people.size());
			assertEquals(1, people.get(0).getId());

		} finally {
			persistor.dropSchema(schemaName);
		}
	}

	@Test
	public void testMultipleSelectNoCriteria() {
		final String schemaName = "test1";
		final int n = 10;
		try {
			initPersonMetadata(schemaName);

			for (int i = 0; i < n; i++) {
				final Person person = new Person();
				person.setId(i+1);
				persistor.store(person);
			}


			final List<Person> people = persistor.selectQuery(new QueryInfo(QueryType.SELECT, null, Collections.singletonMap("People", "People")), null);
			assertNotNull(people);
			assertFalse(people.isEmpty());

			assertEquals(n, people.size());

			final List<Integer> actualIds = new ArrayList<>();
			for (int i = 0; i < n; i++) {
				actualIds.add(people.get(i).getId());
			}
			Collections.sort(actualIds);
			for (int i = 0; i < n; i++) {
				assertEquals(i + 1, actualIds.get(i).intValue());
			}

		} finally {
			persistor.dropSchema(schemaName);
		}
	}

	@Test
	public void testMultipleSelectIdCriteria() {
		final String schemaName = "test1";
		final int n = 5;
		try {
			initPersonMetadata(schemaName);

			int id = 1;
			for (final int year : new int[] {1996, 2003}) {
				for (int i = 0; i < n; i++) {
					final Person person = new Person();
					person.setId(id);
					person.setYear(year);
					persistor.store(person);
					id++;
				}
			}


			final List<Person> people = persistor.selectQuery(
					new QueryInfo(
							QueryType.SELECT,
							null,
							Collections.singletonMap("People", "People"),
							new ClauseInfo("year", Operand.EQ, 1996),
							null
					), null
			);
			assertNotNull(people);
			assertFalse(people.isEmpty());

			assertEquals(5, people.size());

			final List<Integer> actualIds = new ArrayList<>();
			for (int i = 0; i < n; i++) {
				actualIds.add(people.get(i).getId());
			}
			Collections.sort(actualIds);
			for (int i = 0; i < n; i++) {
				assertEquals(i + 1, actualIds.get(i).intValue());
			}

		} finally {
			persistor.dropSchema(schemaName);
		}
	}

	@Test
	public void testMultipleSelectInnerQuery() {
		final String schemaName = "test1";
		try {
			initPersonMetadata(schemaName);


			final Person jl = new Person(1, "John", "Lennon", Gender.MALE);
			jl.setYear(1940);
			final Person pmc = new Person(2, "Paul", "McCartney", Gender.MALE);
			pmc.setYear(1942);
			final Person gh = new Person(3, "George", "Harrison", Gender.MALE);
			gh.setYear(1943);
			final Person rs = new Person(4, "Ringo", "Starr", Gender.MALE);
			rs.setYear(1940);


			for (final Person person : new Person[] {jl, pmc, gh, rs}) {
				persistor.store(person);
			}

			// select * from "People" where identifier in (select identifier from "People" where year=1940);
			final List<Person> people = persistor.selectQuery(
					new QueryInfo(
							QueryType.SELECT,
							null,
							Collections.singletonMap("People", "People"),
							new ClauseInfo(
									"id",
									Operand.IN,
									new QueryInfo(
											QueryType.SELECT,
											Collections.singleton("id"),
											"People", new ClauseInfo("year", Operand.EQ, 1940),
											null, // orders
											null  // limit
									)
							),
							null
					), null
			);
			assertNotNull(people);
			assertFalse(people.isEmpty());

			assertEquals(2, people.size());

			final Set<String> actualNames = new HashSet<>();
			for (final Person p : people) {
				actualNames.add(p.getName());
			}

			assertEquals(new HashSet<String>(Arrays.asList("John", "Ringo")),  actualNames);
		} finally {
			persistor.dropSchema(schemaName);
		}
	}


	/**
	 * CQL supports {@code count(*)} function natively. However only 2 forms are
	 * supported: {@code count(*)} and {@code count(1)}. Unlike this SQL and JPQL
	 * support other arguments, e.g. {@code count(2)} or {@code count(field_name)} that
	 * however do not affect the result.
	 *
	 * The following tests verify that any of this syntaxes is supported by Mestor.
	 */
	@Test
	public void testSelectCountAsterisk() {
		testSelectCount("count(*)", 0);
		testSelectCount("count(*)", 1);
		testSelectCount("count(*)", 10);
	}

	@Test
	public void testSelectCount1() {
		testSelectCount("count(1)", 0);
		testSelectCount("count(1)", 1);
		testSelectCount("count(1)", 10);
	}

	@Test
	public void testSelectCount2() {
		testSelectCount("count(2)", 0);
		testSelectCount("count(2)", 1);
		testSelectCount("count(2)", 10);
	}

	@Test
	public void testSelectCountFieldName() {
		testSelectCount("count(name)", 0);
		testSelectCount("count(name)", 1);
		testSelectCount("count(name)", 10);
	}


	@Test
	public void testSelectNameNoFunction() {
		testSelectFunction("name", Arrays.asList("Adam", "Seth", "Enosh", "Cainan", "Mahalalel", "Jared", "Enoch", "Methuselah", "Lamech", "Noah"));
	}

	@Test
	public void testSelectNameToUpperCase() {
		testSelectFunction("upper(name)", Arrays.asList("ADAM", "SETH", "ENOSH", "CAINAN", "MAHALALEL", "JARED", "ENOCH", "METHUSELAH", "LAMECH", "NOAH"));
	}

	@Test
	public void testSelectNameToLowerCase() {
		testSelectFunction("lower(name)", Arrays.asList("adam", "seth", "enosh", "cainan", "mahalalel", "jared", "enoch", "methuselah", "lamech", "noah"));
	}


	@Test
	public void testSelectNameToTrimUpperLowerCase() {
		testSelectFunction("lower(upper(trim(name)))", Arrays.asList("adam", "seth", "enosh", "cainan", "mahalalel", "jared", "enoch", "methuselah", "lamech", "noah"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSelectNameUsingUndefinedFunction() {
		testSelectFunction("unknown(name)", Arrays.asList("adam", "seth", "enosh", "cainan", "mahalalel", "jared", "enoch", "methuselah", "lamech", "noah"));
	}


	@Test
	public void testMaxAge() {
		testSelectAggregationFunction("age", "max", 969);
	}

	@Test
	public void testMinAge() {
		testSelectAggregationFunction("age", "min", 365);
	}

	@Test
	public void testSumAge() {
		testSelectAggregationFunction("age", "sum", 8575);
	}

	@Test
	public void testAvgAge() {
		testSelectAggregationFunction("age", "avg", 857);
	}

	private void initPersonMetadata(final String schemaName) {
		helper.testCreateSchema(schemaName, null, false);
		final FieldMetadata<Person, Integer, Integer> pk = helper.createFieldMetadata(Person.class, Integer.class, "id", "identifier", true);
		final EntityMetadata<Person> emd = helper.createMetadata(Person.class, schemaName, "People", pk, pk);

		final String yearField = "year";
		final FieldMetadata<Person, Integer, Integer> year = new FieldMetadata<Person, Integer, Integer>(Person.class, Integer.class, yearField,
				ReflectiveBean.getField(Person.class, yearField),
				ReflectiveBean.getGetter(Person.class, yearField),
				ReflectiveBean.getSetter(Person.class, int.class, yearField)
		);

		year.setColumn("year");
		emd.addField(year);
		emd.addIndex(year);

		final String ageField = "age";
		final FieldMetadata<Person, Integer, Integer> age = new FieldMetadata<Person, Integer, Integer>(Person.class, Integer.class, ageField,
				ReflectiveBean.getField(Person.class, ageField),
				ReflectiveBean.getGetter(Person.class, ageField),
				ReflectiveBean.getSetter(Person.class, int.class, ageField)
		);

		age.setColumn("age");
		emd.addField(age);
		emd.addIndex(age);



		final String firstNameField = "name";
		final FieldMetadata<Person, String, String> firstName = new FieldMetadata<Person, String, String>(Person.class, String.class, firstNameField,
				ReflectiveBean.getField(Person.class, firstNameField),
				ReflectiveBean.getGetter(Person.class, firstNameField),
				ReflectiveBean.getSetter(Person.class, String.class, firstNameField)
		);
		firstName.setColumn("first_name");
		emd.addField(firstName);


		final String lastNameField = "lastName";
		final FieldMetadata<Person, String, String> lastName = new FieldMetadata<Person, String, String>(Person.class, String.class, lastNameField,
				ReflectiveBean.getField(Person.class, lastNameField),
				ReflectiveBean.getGetter(Person.class, lastNameField),
				ReflectiveBean.getSetter(Person.class, String.class, lastNameField)
		);
		lastName.setColumn("last_name");
		emd.addField(lastName);


		helper.testEditTable(emd, null, true);
		doReturn(emd).when(helper.ctx).getEntityMetadata(Person.class);
		doReturn(emd).when(helper.ctx).getEntityMetadata("People");
	}


	private void testSelectCount(final String countFunction, final int n) {
		final String schemaName = "test1";
		try {
			initPersonMetadata(schemaName);

			for (int i = 0; i < n; i++) {
				final Person person = new Person();
				person.setId(i+1);
				persistor.store(person);
			}

			final List<Long> countResult = persistor.selectQuery(
					new QueryInfo(
						QueryType.SELECT,
						Collections.<String, Object>singletonMap(countFunction, countFunction),
						Collections.singletonMap("People", "People")),
					null);

			assertNotNull(countResult);
			assertEquals(1, countResult.size());

			final long count = countResult.get(0);
			assertEquals(n,  count);
		} finally {
			persistor.dropSchema(schemaName);
		}
	}

	private void testSelectFunction(final String functionExpr, final List<String> expectedNames) {
		final String schemaName = "test1";
		try {
			initPersonMetadata(schemaName);

			int id = 1;
			final Person adam = createPerson(id++, "Adam", 930);
			final Person seth = createPerson(id++, "Seth", 912);
			final Person enosh = createPerson(id++, "Enosh", 905);
			final Person cainan = createPerson(id++, "Cainan", 910);
			final Person mahalalel = createPerson(id++, "Mahalalel", 895);
			final Person jared = createPerson(id++, "Jared", 962);
			final Person enoch = createPerson(id++, "Enoch", 365);
			final Person methuselah = createPerson(id++, "Methuselah", 969);
			final Person lamech = createPerson(id++, "Lamech", 777);
			final Person noah = createPerson(id++, "Noah", 950);

			for (final Person person : new Person[] {adam, seth, enosh, cainan, mahalalel, jared, enoch, methuselah, lamech, noah}) {
				persistor.store(person);
			}

			final List<Person> result = persistor.selectQuery(
					new QueryInfo(
						QueryType.SELECT,
						Collections.<String, Object>singletonMap("name", functionExpr),
						Collections.singletonMap("People", "People")),
					null);

			assertNotNull(result);

			final List<String> names = Lists.transform(result, new Function<Person, String>() {
					@Override
					public String apply(final Person input) {
						return input.getName();
					}
				}
			);

			assertEquals(expectedNames, names);
		} finally {
			persistor.dropSchema(schemaName);
		}
	}


	private void testSelectAggregationFunction(final String field, final String function, final Number expected) {
		final String schemaName = "test1";
		try {
			initPersonMetadata(schemaName);

			// http://www.answersingenesis.org/articles/nab2/adam-and-noah-live
//			1	 Adam	 930	Genesis 5:4
//			2	 Seth	 912	Genesis 5:8
//			3	 Enosh	 905	Genesis 5:11
//			4	 Cainan	 910	Genesis 5:14
//			5	 Mahalalel	 895	Genesis 5:17
//			6	 Jared	 962	Genesis 5:20
//			7	 Enoch	 365 (translated)	Genesis 5:23
//			8	 Methuselah	 969	Genesis 5:27
//			9	 Lamech	 777	Genesis 5:31
//			10	 Noah	 950	Genesis 9:29

			int id = 1;
			final Person adam = createPerson(id++, "Adam", 930);
			final Person seth = createPerson(id++, "Seth", 912);
			final Person enosh = createPerson(id++, "Enosh", 905);
			final Person cainan = createPerson(id++, "Cainan", 910);
			final Person mahalalel = createPerson(id++, "Mahalalel", 895);
			final Person jared = createPerson(id++, "Jared", 962);
			final Person enoch = createPerson(id++, "Enoch", 365);
			final Person methuselah = createPerson(id++, "Methuselah", 969);
			final Person lamech = createPerson(id++, "Lamech", 777);
			final Person noah = createPerson(id++, "Noah", 950);

			for (final Person person : new Person[] {adam, seth, enosh, cainan, mahalalel, jared, enoch, methuselah, lamech, noah}) {
				persistor.store(person);
			}

			final List<Long> countResult = persistor.selectQuery(
					new QueryInfo(
						QueryType.SELECT,
						Collections.<String, Object>singletonMap(field, function + "(" + field + ")"),
						Collections.singletonMap("People", "People")),
					null);

			assertNotNull(countResult);
			assertEquals(1, countResult.size());

			final Number actual = countResult.get(0);
			if (Double.class.equals(actual.getClass()) || Float.class.equals(actual.getClass())) {
				assertEquals(expected.doubleValue(),  actual.doubleValue(), 0.5);
			} else {
				assertEquals(expected.longValue(),  actual.longValue());
			}
		} finally {
			persistor.dropSchema(schemaName);
		}
	}



	private Person createPerson(final int id, final String name, final int age) {
		final Person person = new Person(id, name, null, Gender.MALE);
		person.setAge(age);
		return person;
	}
}
