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

package org.mestor.query;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;

public class QueryInfoAssert {
	public static void assertQueryInfo(final QueryInfo expected, final QueryInfo actual) {
		assertEquals(expected.getType(), actual.getType());
		assertEquals(expected.getWhat(), actual.getWhat());
		assertEquals(expected.getFrom(), actual.getFrom());
		assertClauseInfo(expected.getWhere(), actual.getWhere());

		assertOrderByInfo(expected.getOrders(), actual.getOrders());
		assertEquals(expected.getStart(), actual.getStart());
		assertEquals(expected.getLimit(), actual.getLimit());
	}

	public static void assertClauseInfo(final ClauseInfo expected, final ClauseInfo actual) {
		if (expected == null) {
			assertNull(actual);
			return;
		}

		final Object expectedExpression = expected.getExpression();
		final Object actualExpression = actual.getExpression();

		if (expectedExpression instanceof ClauseInfo) {
			assertClauseInfo((ClauseInfo)expectedExpression, (ClauseInfo)actualExpression);
		} else if (expectedExpression != null && expectedExpression.getClass().isArray()) {
			final int expectedLength = Array.getLength(expectedExpression);
			final int actualLength = Array.getLength(actualExpression);
			assertEquals(expectedLength, actualLength);
			for (int i = 0; i < expectedLength; i++) {
				final Object e = Array.get(expectedExpression, i);
				final Object a = Array.get(actualExpression, i);
				if (e instanceof ClauseInfo) {
					assertClauseInfo((ClauseInfo)e, (ClauseInfo)a);
				} else if (e != null && e.getClass().isArray()) {
					assertArrayEquals((Object[])e, (Object[])a);
				} else {
					assertEquals(e, a);
				}
			}
		} else if (expectedExpression instanceof ArgumentInfo) {
			assertArgumentInfo((ArgumentInfo<?>)expectedExpression, (ArgumentInfo<?>)actualExpression);
		} else {
			assertEquals(expectedExpression, actualExpression);
		}

		assertEquals(expected.getField(), actual.getField());
		assertEquals(expected.getOperand(), actual.getOperand());
	}


	public static void assertArgumentInfo(final ArgumentInfo<?> expectedArgument, final ArgumentInfo<?> actualArgument) {
		if (expectedArgument == null) {
			assertNull(actualArgument);
			return;
		}

		assertEquals(expectedArgument.getName(), actualArgument.getName());
		assertEquals(expectedArgument.getPosition(), actualArgument.getPosition());
		assertEquals(expectedArgument.getValue(), actualArgument.getValue());
	}


	public static void assertOrderByInfo(final Collection<OrderByInfo> expecteds, final Collection<OrderByInfo> actuals) {
		if (expecteds == null) {
			assertNull(actuals);
			return;
		}

		assertEquals(expecteds.size(), actuals.size());

		for(Iterator<OrderByInfo> expIt = expecteds.iterator(), actIt = actuals.iterator(); expIt.hasNext() && actIt.hasNext();) {
			assertOrderByInfo(expIt.next(), actIt.next());
		}
	}


	public static void assertOrderByInfo(final OrderByInfo expected, final OrderByInfo actual) {
		if (expected == null) {
			assertNull(actual);
			return;
		}

		assertEquals(expected.getField(), actual.getField());
		assertEquals(expected.getOrder(), actual.getOrder());
	}
}
