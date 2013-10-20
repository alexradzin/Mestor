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

package org.mestor.persistence.cql.query;

import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.Collections;
import java.util.Arrays;


import org.junit.Test;
import org.mestor.query.ClauseInfo;
import org.mestor.query.ClauseInfo.Operand;
import org.mestor.query.QueryInfo;
import org.mestor.query.QueryInfo.QueryType;

public class CqlQueryFactoryTest {
	private final static String KEYSPACE = "TestKeySpace";

	@Test
	public void testSimpleSelectAll() {
		test(new QueryInfo(QueryType.SELECT, null, "person"), "SELECT * FROM \"TestKeySpace\".person;");
	}

	@Test
	public void testSimpleSelectAllMixedCaseTable() {
		test(new QueryInfo(QueryType.SELECT, null, "Person"), "SELECT * FROM \"TestKeySpace\".\"Person\";");
	}

	@Test
	public void testSimpleSelectOneField() {
		test(new QueryInfo(QueryType.SELECT, Collections.singletonMap("name", null), "person"), "SELECT name FROM \"TestKeySpace\".person;");
	}

	@Test
	public void testSimpleSelectOneFieldMixedCase() {
		test(new QueryInfo(QueryType.SELECT, Collections.singletonMap("FirstName", null), "person"), "SELECT \"FirstName\" FROM \"TestKeySpace\".person;");
	}

	@Test
	public void testSimpleSelectSeveralFields() {
		test(new QueryInfo(QueryType.SELECT, Arrays.asList("first_name", "last_name"), "person", null, null, null), "SELECT first_name,last_name FROM \"TestKeySpace\".person;");
	}

	@Test
	public void testSimpleSelectOneField1() {
		test(new QueryInfo(QueryType.SELECT, Collections.singletonList("name"), "person", null, null, null), "SELECT name FROM \"TestKeySpace\".person;");
	}

	@Test
	public void testSimpleSelectOneFieldWithLimit() {
		test(new QueryInfo(QueryType.SELECT, Collections.singletonList("name"), "person", null, null, 345), "SELECT name FROM \"TestKeySpace\".person LIMIT 345;");
	}

	@Test
	public void testCountSelect() {
		test(new QueryInfo(QueryType.SELECT, Collections.singletonList("count(*)"), "person", null, null, null), "SELECT count(*) FROM \"TestKeySpace\".person;");
		test(new QueryInfo(QueryType.SELECT, Collections.singletonList("count(1)"), "person", null, null, null), "SELECT count(1) FROM \"TestKeySpace\".person;");
	}


	@Test
	public void testSimpleSelectOneFieldWhere1() {
		testSimpleSelectOneFieldWhere(Operand.EQ, "=");
		testSimpleSelectOneFieldWhere(Operand.GT, ">");
		testSimpleSelectOneFieldWhere(Operand.GE, ">=");
		testSimpleSelectOneFieldWhere(Operand.LT, "<");
		testSimpleSelectOneFieldWhere(Operand.LE, "<=");
	}


	@Test
	public void testSimpleSelectOneFieldWhereLikeExact() {
		testWhereLike("=", "John", "SELECT name FROM \"TestKeySpace\".person WHERE name='John';");
	}

	@Test
	public void testSimpleSelectOneFieldWhereLikeStartsWith() {
		testWhereLike(">=", "John%", "SELECT name FROM \"TestKeySpace\".person WHERE name>='John';");
	}

	//TODO: this case can be solved using column with reversed value
	@Test(expected=UnsupportedOperationException.class)
	public void testSimpleSelectOneFieldWhereLikeEndsWith() {
		testWhereLike(">=", "%John", "SELECT name FROM \"TestKeySpace\".person WHERE name like '%John';");
	}

	@Test(expected=UnsupportedOperationException.class)
	public void testSimpleSelectOneFieldWhereLikeContains() {
		testWhereLike(">=", "%John%", "SELECT name FROM \"TestKeySpace\".person WHERE name like '%John%';");
	}

	@Test(expected=UnsupportedOperationException.class)
	public void testSimpleSelectOneFieldWhereNotLike() {
		testSimpleSelectOneFieldWhere(Operand.NOT_LIKE, "NOT LIKE");
	}

	@Test(expected=UnsupportedOperationException.class)
	public void testSimpleSelectOneFieldWhereNot() {
		testSimpleSelectOneFieldWhere(Operand.NOT, "NOT");
	}

	@Test
	public void testSimpleSelectOneFieldWhereIn1() {
		testSimpleSelectOneFieldWhereIn("John", "'John'");
	}

	@Test
	public void testSimpleSelectOneFieldWhereIn2() {
		testSimpleSelectOneFieldWhereIn(new String[] {"John", "Bill"}, "'John','Bill'");
		testSimpleSelectOneFieldWhereIn(Arrays.asList("John", "Bill"), "'John','Bill'");
	}


	@Test
	public void testSelectWhere2FieldAnd() {
		test(new QueryInfo(QueryType.SELECT,
				null, "person",
				new ClauseInfo(null, Operand.AND, new ClauseInfo[] {
						new ClauseInfo("first_name", Operand.EQ, "John"),
						new ClauseInfo("last_name", Operand.EQ, "Lennon")
				}),
				null, null),
				"SELECT * FROM \"TestKeySpace\".person WHERE first_name='John' AND last_name='Lennon';");
	}

	@Test(expected=UnsupportedOperationException.class)
	public void testSelectWhere2FieldOr() {
		test(new QueryInfo(QueryType.SELECT,
				null, "person",
				new ClauseInfo(null, Operand.OR, new ClauseInfo[] {
						new ClauseInfo("first_name", Operand.EQ, "John"),
						new ClauseInfo("last_name", Operand.EQ, "Lennon")
				}),
				null, null),
				"SELECT * FROM \"TestKeySpace\".person WHERE first_name='John' AND last_name='Lennon';");
	}



	private void testSimpleSelectOneFieldWhereIn(final Object values, final String exectedIn) {
		test(
				new QueryInfo(QueryType.SELECT, Collections.singletonList("name"), "person",
				new ClauseInfo("name", Operand.IN, values),
				null,
				null),
				"SELECT name FROM \"TestKeySpace\".person WHERE name IN (" + exectedIn + ");");
	}



	private void testSimpleSelectOneFieldWhere(final Operand operand, final String op) {
		testSimpleSelectOneFieldWhere(operand, op, "John", "SELECT name FROM \"TestKeySpace\".person WHERE name" + op + "'John';");
	}

	private void testWhereLike(final String op, final String value, final String expectedQl) {
		testSimpleSelectOneFieldWhere(Operand.LIKE, op, value, "SELECT name FROM \"TestKeySpace\".person WHERE name" + op + "'" + value + "';");
	}



	private void testSimpleSelectOneFieldWhere(final Operand operand, final String op, final String value, final String expectedQl) {
		test(
				new QueryInfo(QueryType.SELECT, Collections.singletonList("name"), "person",
				new ClauseInfo("name", operand, value),
				null,
				null),
				expectedQl);
	}


	private void test(final QueryInfo query, final String ... expected) {
		test(query, Arrays.asList(expected));
	}

	private void test(final QueryInfo query, final Collection<String> expected) {
		final CqlQueryFactory factory = new CqlQueryFactory(KEYSPACE);
		final Collection<String> actual = factory.createQuery(query);
		assertEquals(expected, actual);
	}
}
