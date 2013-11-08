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
import static org.mockito.Mockito.doReturn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.mestor.context.EntityContext;
import org.mestor.entities.Person;
import org.mestor.metadata.EntityMetadata;
import org.mestor.metadata.FieldMetadata;
import org.mestor.query.ArgumentInfo;
import org.mestor.query.ClauseInfo;
import org.mestor.query.ClauseInfo.Operand;
import org.mestor.query.OrderByInfo;
import org.mestor.query.QueryInfo;
import org.mestor.query.OrderByInfo.Order;
import org.mestor.query.QueryInfo.QueryType;
import org.mestor.util.Pair;
import org.mockito.Mockito;

public class CqlQueryFactoryTest {
	private final static String KEYSPACE = "TestKeySpace";

	final EntityContext ctx = Mockito.mock(EntityContext.class);

	public CqlQueryFactoryTest() {
		final EntityMetadata<Person> emd1 = createEmd();
		emd1.setTableName("person");
		doReturn(emd1).when(ctx).getEntityMetadata("person");

		final EntityMetadata<Person> emd2 = createEmd();
		emd2.setTableName("Person");
		doReturn(emd2).when(ctx).getEntityMetadata("Person");
	}


	private EntityMetadata<Person> createEmd() {
		final EntityMetadata<Person> emd = new EntityMetadata<Person>();
		emd.setSchemaName(KEYSPACE);


		final FieldMetadata<Person, String, String> fieldName = new FieldMetadata<Person, String, String>(Person.class, String.class, "name");
		fieldName.setColumn("name");

		final FieldMetadata<Person, String, String> pkName = new FieldMetadata<Person, String, String>(Person.class, String.class, "name_pk");
		pkName.setColumn("Name_PK");
		pkName.setKey(true);

		final FieldMetadata<Person, String, String> fieldFirstName = new FieldMetadata<Person, String, String>(Person.class, String.class, "firstName");
		fieldFirstName.setColumn("FirstName");

		final FieldMetadata<Person, String, String> fieldLastName = new FieldMetadata<Person, String, String>(Person.class, String.class, "lastName");
		fieldLastName.setColumn("LastName");

		emd.addAllFields(Arrays.<FieldMetadata<Person, ?, ?>>asList(pkName, fieldName, fieldFirstName, fieldLastName));
		return emd;
	}


	@Test
	public void testSimpleSelectAll() {
		testCreateQuery(new QueryInfo(QueryType.SELECT, null, "person"), "SELECT * FROM \"TestKeySpace\".person;");
	}

	@Test
	public void testSimpleSelectAllMixedCaseTable() {
		testCreateQuery(new QueryInfo(QueryType.SELECT, null, "Person"), "SELECT * FROM \"TestKeySpace\".\"Person\";");
	}

	@Test
	public void testSimpleSelectOneField() {
		testCreateQuery(new QueryInfo(QueryType.SELECT, Collections.singletonMap("name", null), "person"), "SELECT name FROM \"TestKeySpace\".person;");
	}

	@Test(expected = IllegalArgumentException.class) // Field and class names are case sensitive
	public void testSimpleSelectOneFieldMixedCase() {
		testCreateQuery(new QueryInfo(QueryType.SELECT, Collections.singletonMap("FirstName", null), "person"), "SELECT \"FirstName\" FROM \"TestKeySpace\".person;");
	}

	@Test(expected = IllegalArgumentException.class) // Field and class names are case sensitive
	public void testSimpleSelectOneWrongField() {
		testCreateQuery(new QueryInfo(QueryType.SELECT, Collections.singletonMap("DoesNotExist", null), "person"), "SELECT \"DoesNotExist\" FROM \"TestKeySpace\".person;");
	}

	@Test
	public void testSimpleSelectSeveralFields() {
		testCreateQuery(new QueryInfo(QueryType.SELECT, Arrays.asList("name", "lastName"), "person", null, null, null), "SELECT name,\"LastName\" FROM \"TestKeySpace\".person;");
	}

	@Test
	public void testSimpleSelectOneField1() {
		testCreateQuery(new QueryInfo(QueryType.SELECT, Collections.singletonList("name"), "person", null, null, null), "SELECT name FROM \"TestKeySpace\".person;");
	}

	@Test
	public void testSimpleSelectOneFieldWithLimit() {
		testCreateQuery(new QueryInfo(QueryType.SELECT, Collections.singletonList("name"), "person", null, null, 345), "SELECT name FROM \"TestKeySpace\".person LIMIT 345;");
	}

	@Test
	public void testCountSelectAsterisk() {
		testCountSelect("count(*)", "count(*)");
	}

	@Test
	public void testCountSelect1() {
		testCountSelect("count(1)", "count(*)");
	}

	@Test
	public void testCountSelect2() {
		testCountSelect("count(2)", "count(*)");
	}

	@Test
	public void testCountSelectName() {
		testCountSelect("count(last_name)", "count(*)");
	}

	@Test
	public void testCapitalCountSelectName() {
		testCountSelect("COUNT(v)", "count(*)");
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
		testWhereLike("=", "John");
	}

	@Test
	public void testSimpleSelectOneFieldWhereLikeStartsWith() {
		testWhereLike(">=", "John%");
	}

	//TODO: this case can be solved using column with reversed value
	@Test(expected=UnsupportedOperationException.class)
	public void testSimpleSelectOneFieldWhereLikeEndsWith() {
		testWhereLike(">=", "%John");
	}

	@Test(expected=UnsupportedOperationException.class)
	public void testSimpleSelectOneFieldWhereLikeContains() {
		testWhereLike(">=", "%John%");
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
		testCreateQuery(new QueryInfo(QueryType.SELECT,
				null, "person",
				new ClauseInfo(null, Operand.AND, new ClauseInfo[] {
						new ClauseInfo("firstName", Operand.EQ, "John"),
						new ClauseInfo("lastName", Operand.EQ, "Lennon")
				}),
				null, null),
				"SELECT * FROM \"TestKeySpace\".person WHERE \"FirstName\"='John' AND \"LastName\"='Lennon' ALLOW FILTERING;");
	}

	@Test(expected=UnsupportedOperationException.class)
	public void testSelectWhere2FieldOr() {
		testCreateQuery(new QueryInfo(QueryType.SELECT,
				null, "person",
				new ClauseInfo(null, Operand.OR, new ClauseInfo[] {
						new ClauseInfo("first_name", Operand.EQ, "John"),
						new ClauseInfo("last_name", Operand.EQ, "Lennon")
				}),
				null, null),
				"SELECT * FROM \"TestKeySpace\".person WHERE first_name='John' AND last_name='Lennon' ALLOW FILTERING;");
	}



	private void testSimpleSelectOneFieldWhereIn(final Object values, final String exectedIn) {
		testCreateQuery(
				new QueryInfo(QueryType.SELECT, Collections.singletonList("name"), "person",
				new ClauseInfo("name", Operand.IN, values),
				null,
				null),
				"SELECT name FROM \"TestKeySpace\".person WHERE name IN (" + exectedIn + ") ALLOW FILTERING;");
	}



	private void testSimpleSelectOneFieldWhere(final Operand operand, final String op) {
		testSimpleSelectOneFieldWhere(operand, "John", "SELECT name FROM \"TestKeySpace\".person WHERE name" + op + "'John' ALLOW FILTERING;");
	}

	private void testWhereLike(final String op, final String value) {
		testSimpleSelectOneFieldWhere(Operand.LIKE, value, "SELECT name FROM \"TestKeySpace\".person WHERE name" + op + "'" + value + "' ALLOW FILTERING;");
	}

	@Test(expected=IllegalArgumentException.class)
	public void testWhereParameterNotBound() {
		testSimpleSelectOneFieldWhere(
				Operand.EQ,
				new ArgumentInfo<String>("name", null),
				"SELECT name FROM \"TestKeySpace\".person WHERE name='';");
	}

	@Test
	public void testWhereParameterBound() {
		testCreateQuery(new QueryInfo(
				QueryType.SELECT,
				Collections.singletonList("name"),
				"person",
				new ClauseInfo("name", Operand.EQ, new ArgumentInfo<String>("name", null)),
			null,
			null),
			Arrays.asList(new String[]{"SELECT name FROM \"TestKeySpace\".person WHERE name='John' ALLOW FILTERING;"}),
			Collections.<String, Object>singletonMap("name", "John"));
	}

	private void testSimpleSelectOneFieldWhere(final Operand operand, final Object value, final String expectedQl) {
		testCreateQuery(
				new QueryInfo(QueryType.SELECT,
						Collections.singletonList("name"),
						"person",
						new ClauseInfo("name", operand, value),
				null,
				null),
				expectedQl);
	}

	private void testCreateQuery(final QueryInfo query, final String ... expected) {
		testCreateQuery(query, Arrays.asList(expected));
	}

	private void testCreateQuery(final QueryInfo query, final Collection<String> expected) {
		testCreateQuery(query, expected, null);
	}

	private void testCreateQuery(final QueryInfo query, final Collection<String> expected, final Map<String, Object> paramValues) {
		final CqlQueryFactory factory = new CqlQueryFactory(ctx);
		final Collection<CompiledQuery> queries = factory.createQuery(query, paramValues);
		final Collection<String> queryStatements = new ArrayList<String>();
		for (final CompiledQuery q : queries) {
			queryStatements.add(q.getCqlQuery());
		}
		assertEquals(expected, queryStatements);
	}


	@Test
	public void testGetSubqueryIndexesNoSubquery() {
		testGetSubqueryIndexes("select identifier from \"People\" where year=1940;", Collections.<Pair<String, Integer>>emptyList());
	}

	@Test
	public void testGetSubqueryIndexesOneSubqueryInLike() {
		for (final String in : new String[] {"in", "IN", "In", "iN", "like", "LIKE", "LiKe"}) {
			final String query = "select * from \"People\" where identifier " + in + " (subquery(0))";
			testGetSubqueryIndexes(query, Arrays.asList(new Pair<String, Integer>("identifier", 0)));
		}
	}

	@Test
	public void testGetSubqueryIndexesOneSubqueryGtLt() {
		for (final String op : new String[] {">", ">=", "<", "<="}) {
			final String query = "select * from \"People\" where identifier " + op + " (subquery(0))";
			testGetSubqueryIndexes(query, Arrays.asList(new Pair<String, Integer>("identifier", 0)));
		}
	}


	@Test
	public void testGetSubqueryIndexesManySubqueries() {
		final String query =
				"select * from \"People\" where " +
				"id in (subquery(0)) and " +
				"name=subquery(1) and " +
				"lastName>=subquery(2) " +
				"and age>subquery(3) and " +
				"age<subquery(4) and " +
				"year<=subquery(5)" +
				"bigindex < subquery(12345)"
				;

		testGetSubqueryIndexes(query, Arrays.asList(
				new Pair<String, Integer>("id", 0),
				new Pair<String, Integer>("name", 1),
				new Pair<String, Integer>("lastName", 2),
				new Pair<String, Integer>("age", 3),
				new Pair<String, Integer>("age", 4),
				new Pair<String, Integer>("year", 5),
				new Pair<String, Integer>("bigindex", 12345)
			)
		);
	}

	@Test
	public void testSelectAllOrderBy() {
		testSelectAllOrderBy(Order.ASC, "ASC");
		testSelectAllOrderBy(Order.DSC, "DESC");
	}

	@Test
	public void testSelectWhereOrderBy() {
		testCreateQuery(
				new QueryInfo(
						QueryType.SELECT,
						null,
						Collections.<String, String>singletonMap("person", "person"),
						new ClauseInfo("name", Operand.GE, "John"),
						Collections.singleton(new OrderByInfo("name", Order.DSC))),
				"SELECT * FROM \"TestKeySpace\".person WHERE partition=0 AND \"Name_PK\">='John' ORDER BY \"Name_PK\" DESC ALLOW FILTERING;");
	}

	@Test
	public void testSelectWhereCaseSensitive() {
		testCreateQuery(new QueryInfo(QueryType.SELECT,
				null, "person",
				new ClauseInfo(null, Operand.AND, new ClauseInfo[] {
						new ClauseInfo("firstName", Operand.EQ, "John"),
				}),
				null, null),
				"SELECT * FROM \"TestKeySpace\".person WHERE \"FirstName\"='John' ALLOW FILTERING;");
	}

	@Test
	public void testSelectOrderBySensitive() {
		testCreateQuery(new QueryInfo(QueryType.SELECT,
				null, "person",
				null, Collections.singleton(new OrderByInfo("name", Order.ASC)), null),
				"SELECT * FROM \"TestKeySpace\".person WHERE partition=0 ORDER BY \"Name_PK\" ASC;");
	}

	private void testGetSubqueryIndexes(final String query, final List<Pair<String, Integer>> expected) {
		final CqlQueryFactory factory = new CqlQueryFactory(ctx);
		final List<Pair<String, Integer>> actual = factory.getSubqueryIndexes(query);
		assertEquals("Query: " + query, expected, actual);
	}

	private void testSelectAllOrderBy(final Order order, final String queryOrder) {
		testCreateQuery(
				new QueryInfo(QueryType.SELECT, null, Collections.<String, String>singletonMap("person", "person"), null, Collections.singleton(new OrderByInfo("name", order))),
				"SELECT * FROM \"TestKeySpace\".person WHERE partition=0 ORDER BY \"Name_PK\" " + queryOrder + ";");
	}

	private void testCountSelect(final String count, final String expectedCount) {
		testCreateQuery(new QueryInfo(QueryType.SELECT,
				Collections.<String, Object>singletonMap(count, count),
				Collections.singletonMap("person", "person"),
				null, null, null),
				"SELECT " + expectedCount + " FROM \"TestKeySpace\".person;");
	}


}
