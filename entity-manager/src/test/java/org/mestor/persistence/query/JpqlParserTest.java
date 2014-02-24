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

package org.mestor.persistence.query;

import java.util.Collections;
import java.util.HashMap;

import org.junit.Test;
import org.mestor.entities.annotated.Person;
import org.mestor.query.ArgumentInfo;
import org.mestor.query.ClauseInfo;
import org.mestor.query.ClauseInfo.Operand;
import org.mestor.query.OrderByInfo;
import org.mestor.query.OrderByInfo.Order;
import org.mestor.query.QueryInfo;
import org.mestor.query.QueryInfo.QueryType;
import org.mestor.query.QueryInfoAssert;

public class JpqlParserTest {
	@Test
	public void testSelectAllFromPerson() {
		test("SELECT * FROM Person", new QueryInfo(QueryType.SELECT, null, Collections.singletonMap("Person", "Person")));
	}

	@Test
	public void testSelectAllFromPersonWithAlias() {
		test("SELECT * FROM Person p", new QueryInfo(QueryType.SELECT, null, Collections.singletonMap("p", "Person")));
	}

	@Test
	public void testSelectOneFieldFromPerson() {
		test("SELECT name FROM Person",
				new QueryInfo(QueryType.SELECT, Collections.<String, Object>singletonMap("Person", "name"),
				Collections.singletonMap("Person", "Person")));
	}

	@Test
	public void testSelectOneFieldFromPersonWithAlias() {
		test("SELECT name FROM Person p",
				new QueryInfo(QueryType.SELECT, Collections.<String, Object>singletonMap("Person", "name"),
				Collections.singletonMap("p", "Person")));
	}

	@Test
	public void testSelectOneFieldWithAliasFromPersonWithAlias() {
		test("SELECT p.name FROM Person p",
				new QueryInfo(QueryType.SELECT, Collections.<String, Object>singletonMap("p", "name"),
				Collections.singletonMap("p", "Person")));
	}

	@Test
	public void testSelectEntityByAlias() {
		test("SELECT p FROM Person p",
				new QueryInfo(QueryType.SELECT, null,
				Collections.singletonMap("p", "Person")));
	}


	@Test
	public void testSelectEntityObjectByAlias() {
		test("SELECT OBJECT(p) FROM Person p",
				new QueryInfo(QueryType.SELECT, null,
				Collections.singletonMap("p", "Person")));
	}

	@Test
	public void testSelectCountAlias() {
		test("SELECT COUNT(p) FROM Person p",
				new QueryInfo(QueryType.SELECT, Collections.<String, Object>singletonMap("Person", "COUNT(p)"),
				Collections.singletonMap("p", "Person")));
	}

	@SuppressWarnings("serial")
	@Test
	public void testSelectSeveralFieldsFromPerson() {
		test("SELECT p.name, p.lastName FROM Person p",
				new QueryInfo(QueryType.SELECT,
						new HashMap<String, Object>() {{
							put("p", "name");
							put("p", "lastName");
						}},
						Collections.singletonMap("p", "Person")));
	}

	@SuppressWarnings("serial")
	@Test
	public void testSelectSeveralFieldsFromPersonWithAlias() {
		test("SELECT name, lastName FROM Person",
				new QueryInfo(QueryType.SELECT,
						new HashMap<String, Object>() {{
							put("Person", "name");
							put("Person", "lastName");
						}},
						Collections.singletonMap("Person", "Person")));
	}


	@Test
	public void testSelectAllFromPersonWhereIdGt0() {
		test("SELECT * FROM Person WHERE id>0",
				new QueryInfo(
						QueryType.SELECT,
						null,
						Collections.singletonMap("Person", "Person"),
						new ClauseInfo("id", Operand.GT, 0),
						null
				)
		);
	}

	@Test
	public void testSelectAllFromPersonWhereNameEqJohn() {
		test("SELECT * FROM Person WHERE name='John'",
				new QueryInfo(
						QueryType.SELECT,
						null,
						Collections.singletonMap("Person", "Person"),
						new ClauseInfo("name", Operand.EQ, "John"),
						null
				)
		);
	}

	@SuppressWarnings("serial")
	@Test
	public void testSelectSpecificFieldsFromPersonWhereBoolean() {
		test("SELECT id, name, lastName FROM Person p WHERE musician=true",
				new QueryInfo(
						QueryType.SELECT,
						new HashMap<String, Object>() {{
							put("Person", "id");
							put("Person", "name");
							put("Person", "lastName");
						}},
						Collections.singletonMap("p", "Person"),
						new ClauseInfo("musician", Operand.EQ, true),
						null
				)
		);
	}


	@Test
	public void testSelectAllFromPersonComplexWhere() {
		test("SELECT * FROM Person WHERE name= 'John' AND lastName='Lennon'",
				new QueryInfo(
						QueryType.SELECT,
						null,
						Collections.singletonMap("Person", "Person"),
						new ClauseInfo(
								null,
								Operand.AND,
								new ClauseInfo[] {
										new ClauseInfo("name", Operand.EQ, "John"),
										new ClauseInfo("lastName", Operand.EQ, "Lennon"),
								}),
						null
				)
		);
	}

	@Test
	public void testSelectAllFromPersonWhereIn() {
		test("SELECT * FROM Person WHERE name IN ('John', 'Paul', 'George', 'Ringo')",
				new QueryInfo(
						QueryType.SELECT,
						null,
						Collections.singletonMap("Person", "Person"),
						new ClauseInfo("name", Operand.IN, new String[] {"John", "Paul", "George", "Ringo"}),
						null
				)
		);
	}

	@Test
	public void testSelectAllFromPersonWhereEqAndIn() {
		testSelectAllFromPersonWhereEqAndIn("SELECT name, lastName FROM Person WHERE occupation='musician' AND name IN ('John', 'Paul', 'George', 'Ringo')");
	}

	@Test
	public void testSelectAllFromPersonWhereEqAndInMixedCase() {
		testSelectAllFromPersonWhereEqAndIn("select name, lastName From Person wheRe occupation='musician' And name In ('John', 'Paul', 'George', 'Ringo')");
	}


	@SuppressWarnings("serial")
	private void testSelectAllFromPersonWhereEqAndIn(final String jpql) {
		test(jpql,
				new QueryInfo(
						QueryType.SELECT,
						new HashMap<String, Object>() {{
							put("Person", "name");
							put("Person", "lastName");
						}},
						Collections.singletonMap("Person", "Person"),
						new ClauseInfo(
								null,
								Operand.AND,
								new ClauseInfo[] {
										new ClauseInfo("occupation", Operand.EQ, "musician"),
										new ClauseInfo("name", Operand.IN, new String[] {"John", "Paul", "George", "Ringo"}),
								}),
						null
				)
		);
	}



	@Test
	public void testSelectAllFromPersonWhereNamedParameter() {
		test("SELECT * FROM Person WHERE id>:id",
				new QueryInfo(
						QueryType.SELECT,
						null,
						Collections.singletonMap("Person", "Person"),
						new ClauseInfo("id", Operand.GT, new ArgumentInfo<Integer>("id", null)),
						null
				)
		);
	}

	@Test
	public void testSelectAllFromPersonWherePositionedParameter() {
		test("SELECT * FROM Person WHERE name=:givenName",
				new QueryInfo(
						QueryType.SELECT,
						null,
						Collections.singletonMap("Person", "Person"),
						new ClauseInfo("name", Operand.EQ, new ArgumentInfo<String>("givenName", null)),
						null
				)
		);
	}

	@Test
	public void testSelectAllFromPersonWhereNamedParameters() {
		test("SELECT * FROM Person WHERE name=:givenName AND lastName=:surname AND age>=:years",
				new QueryInfo(
						QueryType.SELECT,
						null,
						Collections.singletonMap("Person", "Person"),
						new ClauseInfo(
								null,
								Operand.AND,
								new ClauseInfo[] {
										new ClauseInfo("name", Operand.EQ, new ArgumentInfo<String>("givenName", null)),
										new ClauseInfo("lastName", Operand.EQ, new ArgumentInfo<String>("surname", null)),
										new ClauseInfo("age", Operand.GE, new ArgumentInfo<String>("years", null)),
								}),
						null
				)
		);
	}

	@Test
	public void testSelectAllFromPersonOrderById() {
		testSelectAllFromPersonOrderById("SELECT * FROM Person ORDER BY id", null, Order.ASC);
	}

	@Test
	public void testSelectAllFromPersonOrderByIdAsc() {
		testSelectAllFromPersonOrderById("SELECT * FROM Person ORDER BY id ASC", null, Order.ASC);
	}

	@Test
	public void testSelectAllFromPersonOrderByIdDesc() {
		testSelectAllFromPersonOrderById("SELECT * FROM Person ORDER BY id dsc", null, Order.DSC);
	}

	@Test
	public void testSelectAllFromPersonOrderByIdWithAlias() {
		testSelectAllFromPersonOrderById("SELECT * FROM Person p ORDER BY p.id", "p", Order.ASC);
	}


	private void testSelectAllFromPersonOrderById(final String jpql, final String alias, final Order order) {
		final String aliasValue = alias == null ? "Person" : alias;

		test(jpql,
				new QueryInfo(
						QueryType.SELECT,
						null,
						Collections.singletonMap(aliasValue, "Person"),
						null,
						Collections.<OrderByInfo>singleton(new OrderByInfo("id", order)),
						null)
		);
	}


	@Test
	public void testSelectAllFromPersonWhereNameEqJohnOrderByLastName() {
		test("SELECT * FROM Person p WHERE p.name='John' ORDER BY p.lastName",
				new QueryInfo(
						QueryType.SELECT,
						null,
						Collections.singletonMap("p", "Person"),
						new ClauseInfo("name", Operand.EQ, "John"),
						Collections.<OrderByInfo>singleton(new OrderByInfo("lastName", Order.ASC))
				)
		);
	}




	@Test
	public void testDeleteAllFromPerson() {
		test("DELETE FROM Person", new QueryInfo(QueryType.DELETE, null, Collections.singletonMap("Person", "Person")));
	}


	@Test
	public void testDeleteAllFromPersonWhereIdGtConst() {
		test("DELETE FROM Person WHERE id>5",
				new QueryInfo(
						QueryType.DELETE,
						null,
						Collections.singletonMap("Person", "Person"),
						new ClauseInfo("id", Operand.GT, 5),
						null
				)
		);
	}

	@Test
	public void testDeleteAllFromPersonWhereIdNameEqJohn() {
		test("DELETE FROM Person WHERE name='John'",
				new QueryInfo(
						QueryType.DELETE,
						null,
						Collections.singletonMap("Person", "Person"),
						new ClauseInfo("name", Operand.EQ, "John"),
						null
				)
		);
	}

	@Test
	public void testDeleteAllFromPersonWhereIdGtNamedParameter() {
		test("DELETE FROM Person WHERE id > :id",
				new QueryInfo(
						QueryType.DELETE,
						null,
						Collections.singletonMap("Person", "Person"),
						new ClauseInfo("id", Operand.GT, new ArgumentInfo<Integer>("id", null)),
						null
				)
		);
	}

	@Test
	public void testDeleteAllFromPersonWhereIdGtNamedParameterYodaStyle() {
		test("DELETE FROM Person WHERE :id < id",
				new QueryInfo(
						QueryType.DELETE,
						null,
						Collections.singletonMap("Person", "Person"),
						new ClauseInfo("id", Operand.GT, new ArgumentInfo<Integer>("id", null)),
						null
				)
		);
	}

	/**
	 * Joins are not supported now. Actually there is a very low chance that they will
	 * be ever supported...
	 */
	@Test(expected = UnsupportedOperationException.class)
	public void testSelectWithJoin() {
		test("SELECT OBJECT(lun) from HostCluster hc JOIN hc.luns lun WHERE hc.id = :id AND lun.lun = :lun", null);
	}


	private void test(final String jpql, final QueryInfo expected) {
		final QueryInfo actual = new JpqlParser().createCriteria(jpql, Person.class);
		QueryInfoAssert.assertQueryInfo(expected, actual);
	}



}
