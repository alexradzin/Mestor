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
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import javax.annotation.Nullable;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.SingularAttribute;

import org.junit.Assert;
import org.junit.Test;
import org.mestor.entities.annotated.Person;
import org.mestor.metadata.EntityMetadata;
import org.mestor.persistence.query.CommonAbstractCriteriaBase;
import org.mestor.persistence.query.QueryImpl;
import org.mestor.query.ClauseInfo;
import org.mestor.query.ClauseInfo.Operand;
import org.mestor.query.OrderByInfo;
import org.mestor.query.OrderByInfo.Order;
import org.mestor.query.QueryInfo;
import org.mestor.query.QueryInfo.QueryType;
import org.mestor.query.QueryInfoAssert;

import com.google.common.base.Function;

public class CriteriaBuilderTest {
	private final String MESTOR_PERSISTOR_CLASS = "org.mestor.persistor.class";
	private final String MESTOR_ENTITY_PACKAGE = "org.mestor.managed.package";
	private final static Map<String, String> fromPerson = Collections.<String, String> singletonMap("person", "Person");

	private final EntityManager em;

	public CriteriaBuilderTest() {
		em = createEntityManager(DummyPersistor.class.getName());
		assertNotNull(em);
	}

	@Test
	public void testGetCriteriaBuilder() {
		getCriteriaBuilder();
	}

	@Test
	public void testCreateEmptyQuery() {
		testCreateQuery(Person.class, new QueryInfo(QueryType.SELECT, null, Collections.<String, String> emptyMap()), new Function<CriteriaQuery<Person>, Void>() {
			@Override
			public Void apply(@Nullable final CriteriaQuery<Person> input) {
				return null;
			}
		});
	}

	@Test
	public void testCreateQueryWithFrom() {
		testCreateQuery(Person.class, new QueryInfo(QueryType.SELECT, null, fromPerson), new Function<CriteriaQuery<Person>, Void>() {
			@Override
			public Void apply(@Nullable final CriteriaQuery<Person> criteria) {
				criteria.from(Person.class);
				return null;
			}
		});
	}

	@Test
	public void testCreateQueryWithFromAndWhereIdEqConst() {
		testCreateQueryWithFromAndWhere1("identifier", Integer.class, Operand.EQ, 123);
	}

	@Test
	public void testCreateQueryWithFromAndWhereIdNotEqConst() {
		testCreateQueryWithFromAndWhere1("identifier", Integer.class, Operand.NE, 123);
	}

	@Test
	public void testCreateQueryWithFromAndWhereIdGtConst() {
		testCreateQueryWithFromAndWhere1("identifier", Integer.class, Operand.GT, 456);
	}

	@Test
	public void testCreateQueryWithFromAndWhereIdGeConst() {
		testCreateQueryWithFromAndWhere1("identifier", Integer.class, Operand.GE, 456);
	}

	@Test
	public void testCreateQueryWithFromAndWhereIdLeConst() {
		testCreateQueryWithFromAndWhere1("identifier", Integer.class, Operand.LE, 456);
	}

	@Test
	public void testCreateQueryWithFromAndWhereIdLtConst() {
		testCreateQueryWithFromAndWhere1("identifier", Integer.class, Operand.LT, 456);
	}

	@Test
	public void testCreateQueryWithFromAndWhereNameEqConst() {
		testCreateQueryWithFromAndWhere1("name", String.class, Operand.EQ, "John");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCreateQueryWithFromAndWhereNotExistingField() {
		testCreateQueryWithFromAndWhere1("doesnotexist", String.class, Operand.EQ, "Foobar");
	}

	@Test
	public void testCreateQueryWithFromAndWhereWith2FieldsByAnd() {
		twoFieldsByOperand(Operand.AND);
	}

	@Test
	public void testCreateQueryWithFromAndWhereWith2FieldsByOr() {
		twoFieldsByOperand(Operand.OR);
	}

	@Test
	public void testCreateQueryWithFromAndWhereWithLikeNoPercent() {
		testCreateQueryWithFromAndWhere1("name", String.class, Operand.LIKE, "John");
	}

	private void twoFieldsByOperand(final Operand op) {
		Assert.assertTrue(op == Operand.AND || op == Operand.OR);
		testCreateQuery(Person.class,
		new QueryInfo(QueryType.SELECT, null,
				fromPerson,
				new ClauseInfo(null, op, new ClauseInfo[] {
						new ClauseInfo("name", Operand.EQ, "John"),
						new ClauseInfo("lastName", Operand.EQ, "Lennon") }
				),
				null),

		new Function<CriteriaQuery<Person>, Void>() {
			@Override
			public Void apply(@Nullable final CriteriaQuery<Person> criteria) {
				final Root<Person> root = notNull(criteria.from(Person.class));
				final SingularAttribute<? super Person, String> personNameAttr = getAttribute(Person.class, "name", String.class);
				final SingularAttribute<? super Person, String> personLastNameAttr = getAttribute(Person.class, "lastName", String.class);
				final CriteriaBuilder builder = getBuilder(criteria);
				final Predicate op1 = builder.equal(root.get(personNameAttr), "John");
				final Predicate op2 = builder.equal(root.get(personLastNameAttr), "Lennon");
				final Predicate where = op == Operand.AND ? builder.and(op1, op2) : builder.or(op1, op2);
				criteria.where(where);
				return null;
			}
		});
	}

	@Test
	public void testCreateQueryWithFromAndWhereWithEmptyConjunction() {
		testCreateQuery(Person.class,

		new QueryInfo(QueryType.SELECT, null, fromPerson, null, null),

		new Function<CriteriaQuery<Person>, Void>() {
			@Override
			public Void apply(@Nullable final CriteriaQuery<Person> criteria) {
				criteria.from(Person.class);
				final CriteriaBuilder builder = getBuilder(criteria);
				criteria.where(builder.conjunction());
				return null;
			}
		});
	}

	@Test
	public void testCreateQueryWithFromAndWhereWithConjunctionAndField() {
		testCreateQuery(Person.class,

		new QueryInfo(QueryType.SELECT, null, fromPerson, new ClauseInfo("name", Operand.EQ, "John"), null),

		new Function<CriteriaQuery<Person>, Void>() {
			@Override
			public Void apply(@Nullable final CriteriaQuery<Person> criteria) {
				final Root<Person> root = notNull(criteria.from(Person.class));
				final SingularAttribute<? super Person, String> personNameAttr = getAttribute(Person.class, "name", String.class);
				final CriteriaBuilder builder = getBuilder(criteria);
				criteria.where(builder.and(builder.conjunction(), builder.equal(root.get(personNameAttr), "John")));
				return null;
			}
		});
	}

	@Test
	public void testCreateQueryWithFromAndWhereWithIsNull() {
		testCreateQuery(
			Person.class,
			new QueryInfo(
					QueryType.SELECT,
					null,
					fromPerson,
					new ClauseInfo("name", Operand.EQ, null),
					null),
			new Function<CriteriaQuery<Person>, Void>() {
				@Override
				public Void apply(@Nullable final CriteriaQuery<Person> criteria) {
					final Root<Person> root = notNull(criteria.from(Person.class));
					final CriteriaBuilder builder = getBuilder(criteria);
					final SingularAttribute<? super Person, String> name = getAttribute(Person.class, "name", String.class);
					criteria.where(builder.isNull(root.get(name)));
					return null;
				}
			});
	}

	private CriteriaBuilder getBuilder(@Nullable final CriteriaQuery<?> criteria) {
		// this is ugly casting but it is good enough for tests.
		@SuppressWarnings("rawtypes")
		final CriteriaBuilder builder = ((CommonAbstractCriteriaBase) criteria).getCriteriaBuilder();
		return builder;
	}

	private <E, F> SingularAttribute<? super E, F> getAttribute(final Class<E> entityClass, final String attrName, final Class<F> attrClass) {
		final EntityType<E> entityType = notNull(em.getMetamodel().entity(entityClass));
		final SingularAttribute<? super E, F> singularAttribute = entityType.getSingularAttribute(attrName, attrClass);
		return notNull(singularAttribute);
	}

	@Test
	public void testCreateQueryWithFromAndWhereIn() {
		testCreateQuery(Person.class, new QueryInfo(QueryType.SELECT, null, fromPerson, new ClauseInfo("name", Operand.IN, new String[] { "John",
				"Paul", "George", "Ringo" }), null),

		new Function<CriteriaQuery<Person>, Void>() {
			@Override
			public Void apply(@Nullable final CriteriaQuery<Person> criteria) {
				final Root<Person> root = notNull(criteria.from(Person.class));
				final SingularAttribute<? super Person, String> personNameAttr = getAttribute(Person.class, "name", String.class);
				criteria.where(root.get(personNameAttr).in(Arrays.asList("John", "Paul", "George", "Ringo")));
				return null;
			}
		});
	}

	@Test
	public void testCreateQueryWithFromAndWhereAgeGEConst() {
		testCreateQuery(Person.class, new QueryInfo(QueryType.SELECT, null, fromPerson, new ClauseInfo("age", Operand.GE, new Integer(0)), null),

		new Function<CriteriaQuery<Person>, Void>() {
			@Override
			public Void apply(@Nullable final CriteriaQuery<Person> criteria) {
				final Root<Person> root = notNull(criteria.from(Person.class));
				final SingularAttribute<? super Person, Integer> personAgeAttr = getAttribute(Person.class, "age", int.class);
				final CriteriaBuilder builder = getBuilder(criteria);
				criteria.where(builder.greaterThanOrEqualTo(root.get(personAgeAttr), 0));
				return null;
			}
		});
	}

	@Test
	public void testCreateQueryWithFromAndOrderAgeAsc() {
		testOrderBy(Order.ASC);
	}

	@Test
	public void testCreateQueryWithFromAndOrderAgeDesc() {
		testOrderBy(Order.DSC);
	}

	private void testOrderBy(final Order order) {
		testCreateQuery(Person.class,
				new QueryInfo(QueryType.SELECT, null, fromPerson, null, Collections.<OrderByInfo> singletonList(new OrderByInfo("age", order))),

				new Function<CriteriaQuery<Person>, Void>() {
					@Override
					public Void apply(@Nullable final CriteriaQuery<Person> criteria) {
						final Root<Person> root = notNull(criteria.from(Person.class));
						final SingularAttribute<? super Person, Integer> personAgeAttr = getAttribute(Person.class, "age", int.class);
						final CriteriaBuilder builder = getBuilder(criteria);
						if (order == Order.ASC) {
							criteria.orderBy(builder.asc(root.get(personAgeAttr)));
						} else {
							criteria.orderBy(builder.desc(root.get(personAgeAttr)));
						}
						return null;
					}
				});
	}

	private abstract class FunctionExpression<T> implements Function<CriteriaQuery<T>, Void>{
		@Override
		public final Void apply(@Nullable final CriteriaQuery<T> criteria) {
			final CriteriaBuilder builder = getBuilder(criteria);
			final Root<Person> root = notNull(criteria.from(Person.class));
			apply(criteria, root, builder);
			return null;
		}

		protected abstract void apply(final CriteriaQuery<T> criteria, final Root<Person> root, final CriteriaBuilder builder);
	}

	@Test
	public void testCreateQueryWithFromCount() {
		testQueryWithFunction(
			Long.class,
			"count(*)",
			"age",
			new FunctionExpression<Long>() {
				@Override
				protected void apply(final CriteriaQuery<Long> criteria, final Root<Person> root, final CriteriaBuilder builder) {
					final SingularAttribute<? super Person, Integer> personAgeAttr = getAttribute(Person.class, "age", int.class);
					criteria.select(builder.count(root.get(personAgeAttr)));
				}
			});
	}

	private <T> void testQueryWithFunction(final Class<T> resultClass, final Object value, final String alias, final FunctionExpression<T> func) {
		testCreateQuery(resultClass,
				new QueryInfo(
						QueryType.SELECT,
						Collections.<String, Object> singletonMap(alias, value),
						fromPerson,
						null,
						null),

				func);
	}

	@Test
	public void testCreateQueryWithFromLiteral() {
		testQueryWithFunction(
				Long.class,
				1l,
				"1",
				new FunctionExpression<Long>() {
					@Override
					protected void apply(final CriteriaQuery<Long> criteria, final Root<Person> root, final CriteriaBuilder builder) {
						criteria.select(builder.literal(1L));
					}
				});
	}

	@Test
	public void testCreateQueryWithFromMax() {
		testQueryWithFunction(
				Integer.class,
				"max(age)",
				"age",
				new FunctionExpression<Integer>() {
					@Override
					protected void apply(final CriteriaQuery<Integer> criteria, final Root<Person> root, final CriteriaBuilder builder) {
						final SingularAttribute<? super Person, Integer> personAgeAttr = getAttribute(Person.class, "age", int.class);
						criteria.select(builder.max(root.get(personAgeAttr)));
					}
				});
	}

	@Test
	public void testCreateQueryWithFromMin() {
		testQueryWithFunction(
				Integer.class,
				"min(age)",
				"age",
				new FunctionExpression<Integer>() {
					@Override
					protected void apply(final CriteriaQuery<Integer> criteria, final Root<Person> root, final CriteriaBuilder builder) {
						final SingularAttribute<? super Person, Integer> personAgeAttr = getAttribute(Person.class, "age", int.class);
						criteria.select(builder.min(root.get(personAgeAttr)));
					}
				});
	}

	@Test
	public void testCreateQueryWithFromSum() {
		testQueryWithFunction(
				Integer.class,
				"sum(age)",
				"age",
				new FunctionExpression<Integer>() {
					@Override
					protected void apply(final CriteriaQuery<Integer> criteria, final Root<Person> root, final CriteriaBuilder builder) {
						final SingularAttribute<? super Person, Integer> personAgeAttr = getAttribute(Person.class, "age", int.class);
						criteria.select(builder.sum(root.get(personAgeAttr)));
					}
				});
	}

	@Test
	public void testCreateQueryWithFromUpper() {
		testQueryWithFunction(
				String.class,
				"upper(name)",
				"name",
				new FunctionExpression<String>() {
					@Override
					protected void apply(final CriteriaQuery<String> criteria, final Root<Person> root, final CriteriaBuilder builder) {
						final SingularAttribute<? super Person, String> personNameAttr = getAttribute(Person.class, "name", String.class);
						criteria.select(builder.upper(root.get(personNameAttr)));
					}
				});
	}

	private <T> void testCreateQueryWithFromAndWhere1(final String fieldName, final Class<T> fieldType, final Operand operand, final T fieldValue) {
		testCreateQuery(Person.class, new QueryInfo(QueryType.SELECT, null, fromPerson, new ClauseInfo(fieldName, operand, fieldValue), null),
				new Function<CriteriaQuery<Person>, Void>() {
					@Override
					public Void apply(@Nullable final CriteriaQuery<Person> criteria) {
						final Root<Person> root = notNull(criteria.from(Person.class));
						final SingularAttribute<? super Person, T> personIdAttr = getAttribute(Person.class, fieldName, fieldType);

						final CriteriaBuilder builder = getBuilder(criteria);

						final Path<T> path = root.get(personIdAttr);
						final Predicate predicate;
						@SuppressWarnings("unchecked")
						final Path<Number> numPath = (Path<Number>) path;
						@SuppressWarnings("unchecked")
						final Path<String> strPath = (Path<String>) path;
						switch (operand) {
						case EQ:
							predicate = builder.equal(path, fieldValue);
							break;
						case NE:
							predicate = builder.notEqual(path, fieldValue);
							break;
						case GT:
							predicate = builder.gt(numPath, (Number) fieldValue);
							break;
						case GE:
							predicate = builder.ge(numPath, (Number) fieldValue);
							break;
						case LE:
							predicate = builder.le(numPath, (Number) fieldValue);
							break;
						case LT:
							predicate = builder.lt(numPath, (Number) fieldValue);
							break;
						case LIKE:
							predicate = builder.like(strPath, (String) fieldValue);
							break;
						default:
							throw new UnsupportedOperationException(operand.name());
						}

						criteria.where(predicate);
						return null;
					}
				});
	}

	private <T> void testCreateQuery(final Class<T> clazz, final QueryInfo expected, final Function<CriteriaQuery<T>, Void> f) {
		final CriteriaBuilder builder = getCriteriaBuilder();
		final CriteriaQuery<T> criteria = builder.createQuery(clazz);

		assertNotNull(criteria);
		assertEquals(clazz, criteria.getResultType());

		f.apply(criteria);

		final TypedQuery<T> query = em.createQuery(criteria);
		assertNotNull(query);
		assertEquals(QueryImpl.class, query.getClass());

		final QueryImpl<T> queryImpl = (QueryImpl<T>) query;
		final QueryInfo queryInfo = queryImpl.getQueryInfo();
		assertNotNull(queryInfo);

		QueryInfoAssert.assertQueryInfo(expected, queryInfo);
	}

	private EntityManager createEntityManager(final String persistorClassName) {
		System.setProperty(MESTOR_PERSISTOR_CLASS, persistorClassName);
		System.setProperty(MESTOR_ENTITY_PACKAGE, Person.class.getPackage().getName());
		try {

			final EntityMetadata<Person> personEmd = new EntityMetadata<Person>(Person.class);
			personEmd.setTableName("person");
			return Persistence.createEntityManagerFactory("mestortest").createEntityManager();
		} finally {
			System.getProperties().remove(MESTOR_PERSISTOR_CLASS);
		}
	}

	private CriteriaBuilder getCriteriaBuilder() {
		final CriteriaBuilder builder = em.getCriteriaBuilder();
		assertNotNull(builder);
		return builder;
	}


	private static <T> T notNull(final T value) {
		assertNotNull(value);
		return value;
	}
}