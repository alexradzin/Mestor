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

import org.junit.Test;
import org.mestor.entities.annotated.Person;
import org.mestor.metadata.EntityMetadata;
import org.mestor.persistence.query.CommonAbstractCriteriaBase;
import org.mestor.persistence.query.QueryImpl;
import org.mestor.query.ClauseInfo;
import org.mestor.query.ClauseInfo.Operand;
import org.mestor.query.QueryInfo;
import org.mestor.query.QueryInfo.QueryType;
import org.mestor.query.QueryInfoAssert;

import com.google.common.base.Function;

public class CriteriaBuilderTest {
	private final String MESTOR_PERSISTOR_CLASS = "org.mestor.persistor.class";
	private final String MESTOR_ENTITY_PACKAGE = "org.mestor.managed.package";

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
		testCreateQuery(Person.class, new QueryInfo(QueryType.SELECT, null, Collections.<String, String>emptyMap()), new Function<CriteriaQuery<Person>, Void>() {
			@Override
			public Void apply(@Nullable final CriteriaQuery<Person> input) {
				return null;
			}
		});
	}

	@Test
	public void testCreateQueryWithFrom() {
		testCreateQuery(
			Person.class,
			new QueryInfo(QueryType.SELECT, null, Collections.<String, String>singletonMap("person", null)),
			new Function<CriteriaQuery<Person>, Void>() {
				@Override
				public Void apply(@Nullable final CriteriaQuery<Person> criteria) {
					criteria.from(Person.class);
					return null;
				}
			}
		);
	}


	@Test
	public void testCreateQueryWithFromAndWhereIdEqConst() {
		testCreateQueryWithFromAndWhere1("identifier", Integer.class, Operand.EQ, 123);
	}

	@Test
	public void testCreateQueryWithFromAndWhereIdGtConst() {
		testCreateQueryWithFromAndWhere1("identifier", Integer.class, Operand.GT, 456);
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
	public void testCreateQueryWithFromAndWhereWith2Fields() {
		testCreateQuery(
				Person.class,
				new QueryInfo(
						QueryType.SELECT,
						null,
						Collections.<String, String>singletonMap("person", null),
						new ClauseInfo(null, Operand.AND, new ClauseInfo[] {
								new ClauseInfo("name", Operand.EQ, "John"),
								new ClauseInfo("lastName", Operand.EQ, "Lennon")
						}),
						null),


				new Function<CriteriaQuery<Person>, Void>() {
					@Override
					public Void apply(@Nullable final CriteriaQuery<Person> criteria) {
						final Root<Person> root = notNull(criteria.from(Person.class));
						final EntityType<Person> personType = notNull(em.getMetamodel().entity(Person.class));
						final SingularAttribute<? super Person, String> personNameAttr = notNull(personType.getSingularAttribute("name", String.class));
						final SingularAttribute<? super Person, String> personLastNameAttr = notNull(personType.getSingularAttribute("lastName", String.class));

						// this is ugly casting but it is good enough for tests.
						@SuppressWarnings("rawtypes")
						final CriteriaBuilder builder = ((CommonAbstractCriteriaBase)criteria).getCriteriaBuilder();

						criteria.where(
								builder.and(
										builder.equal(root.get(personNameAttr), "John"),
										builder.equal(root.get(personLastNameAttr), "Lennon")
								));
						return null;
					}
				}
			);
	}

	@Test
	public void testCreateQueryWithFromAndWhereIn() {
		testCreateQuery(
				Person.class,
				new QueryInfo(
						QueryType.SELECT,
						null,
						Collections.<String, String>singletonMap("person", null),
							new ClauseInfo("name", Operand.IN, new String[] {"John", "Paul", "George", "Ringo"}),
						null),


				new Function<CriteriaQuery<Person>, Void>() {
					@Override
					public Void apply(@Nullable final CriteriaQuery<Person> criteria) {
						final Root<Person> root = notNull(criteria.from(Person.class));
						final EntityType<Person> personType = notNull(em.getMetamodel().entity(Person.class));
						final SingularAttribute<? super Person, String> personNameAttr = notNull(personType.getSingularAttribute("name", String.class));
						criteria.where(root.get(personNameAttr).in(Arrays.asList("John", "Paul", "George", "Ringo")));
						return null;
					}
				}
			);
	}


	private <T> void testCreateQueryWithFromAndWhere1(final String fieldName, final Class<T> fieldType, final Operand operand, final T fieldValue) {
		testCreateQuery(
			Person.class,
			new QueryInfo(QueryType.SELECT, null, Collections.<String, String>singletonMap("person", null), new ClauseInfo(fieldName, operand, fieldValue), null),
			new Function<CriteriaQuery<Person>, Void>() {
				@Override
				public Void apply(@Nullable final CriteriaQuery<Person> criteria) {
					final Root<Person> root = notNull(criteria.from(Person.class));
					final EntityType<Person> personType = notNull(em.getMetamodel().entity(Person.class));
					final SingularAttribute<? super Person, T> personIdAttr = notNull(personType.getSingularAttribute(fieldName, fieldType));

					// this is ugly casting but it is good enough for tests.
					@SuppressWarnings("rawtypes")
					final CriteriaBuilder builder = ((CommonAbstractCriteriaBase)criteria).getCriteriaBuilder();

					final Path<T> path = root.get(personIdAttr);
					final Predicate predicate;

					switch(operand) {
						case EQ:
							predicate = builder.equal(path, fieldValue);
							break;
						case GT: {
							@SuppressWarnings("unchecked")
							final Path<Number> numPath = (Path<Number>)path;
							predicate = builder.gt(numPath, (Number)fieldValue);
							break;
						}
						default:
							throw new UnsupportedOperationException(operand.name());
					}


					criteria.where(predicate);
					return null;
				}
			}
		);
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


		final QueryImpl<T> queryImpl = (QueryImpl<T>)query;
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