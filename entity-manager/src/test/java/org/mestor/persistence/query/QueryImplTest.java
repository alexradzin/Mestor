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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.Collections;

import javax.persistence.Parameter;

import org.junit.Before;
import org.junit.Test;
import org.mestor.context.EntityContext;
import org.mestor.context.Persistor;
import org.mestor.entities.annotated.Person;
import org.mestor.metadata.EntityMetadata;
import org.mestor.metadata.jpa.JpaAnnotationsMetadataFactory;
import org.mestor.query.ArgumentInfo;
import org.mestor.query.ClauseInfo;
import org.mestor.query.ClauseInfo.Operand;
import org.mestor.query.QueryInfo;
import org.mestor.query.QueryInfo.QueryType;
import org.mockito.Mock;



public class QueryImplTest {
	@Mock private Persistor persistor;
	@Mock private EntityContext ctx;

	@Before
	public void setUp() {
		initMocks(this);
		doReturn(persistor).when(ctx).getPersistor();
		final EntityMetadata<Person> emd = new JpaAnnotationsMetadataFactory(ctx).create(Person.class);
		doReturn(emd).when(ctx).getEntityMetadata(Person.class);
		doReturn(emd).when(ctx).getEntityMetadata("Person");
	}


	@Test
	public void testQueryNoParams() {
		testQueryCreation(
				new QueryInfo(QueryType.SELECT, Collections.<String, Object>singletonMap("Person", "name"), Collections.singletonMap("p", "Person")),
				Person.class,
				new String[0],
				new Integer[0],
				new Class[0]
		);
	}


	@Test
	public void testQueryOneParam() {
		testQueryCreation(
				new QueryInfo(
						QueryType.SELECT,
						null,
						Collections.singletonMap("Person", "Person"),
						new ClauseInfo("identifier", Operand.GT, new ArgumentInfo<Integer>("id", null)),
						null
				),
				Person.class,
				new String[] {"id"},
				new Integer[] {null},
				new Class[] {int.class}
		);
	}

	@Test
	public void testQuerySeveralParamsInCompositeWhere() {
		testQueryCreation(
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
				),
				Person.class,
				new String[] {"givenName", "surname", "years"},
				new Integer[] {null, null, null},
				new Class[] {String.class, String.class, int.class}
		);
	}

	private <T> void testQueryCreation(final QueryInfo queryInfo, final Class<T> expectedResultType, final String[] paramNames, final Integer[] paramIndexes, final Class<?>[] paramTypes) {
		final QueryImpl<T> query = new QueryImpl<T>(queryInfo, ctx);
		//query.getParameter("").get
		assertEquals(expectedResultType, query.getResultType());


		assertNotNull(paramNames);
		assertNotNull(paramIndexes);
		assertNotNull(paramTypes);

		assertEquals(paramNames.length, paramIndexes.length);
		assertEquals(paramNames.length, paramTypes.length);

		final int n = paramNames.length;

		for (int i = 0; i < n; i++) {
			final String paramName = paramNames[i];
			final Integer paramIndex = paramIndexes[i];
			if (paramName != null) {
				final Parameter<?> param = query.getParameter(paramName);
				assertEquals(paramTypes[i], param.getParameterType());
			}
			if (paramIndex != null) {
				final Parameter<?> param = query.getParameter(paramIndex);
				assertEquals(paramTypes[i], param.getParameterType());
			}
		}
	}

}
