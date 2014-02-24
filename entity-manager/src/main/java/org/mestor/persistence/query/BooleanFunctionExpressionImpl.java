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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;

import org.mestor.context.EntityContext;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;

@SuppressWarnings("serial")
public class BooleanFunctionExpressionImpl extends FunctionExpressionImpl<Boolean> implements Predicate {
	private final static Function<Object, Expression<?>> value2expression = new Function<Object, Expression<?>>() {
		@Override
		public Expression<Object> apply(final Object value) {
			return new ConstantExpresion<Object>(value);
		}
	};

	BooleanFunctionExpressionImpl(final EntityContext entityContext, final String function, final Expression<?> ... arguments) {
		this(entityContext, function, Arrays.<Expression<?>>asList(arguments));
	}

	BooleanFunctionExpressionImpl(final EntityContext entityContext, final String function, final Collection<Expression<?>> arguments) {
		super(entityContext, Boolean.class, function, arguments);
	}

	@Override
	public BooleanOperator getOperator() {
		return BooleanOperator.AND;
	}

	@Override
	public boolean isNegated() {
		return false;
	}

	@Override
	public List<Expression<Boolean>> getExpressions() {
		return Arrays.<Expression<Boolean>>asList(this);
	}

	@Override
	public Predicate not() {
		//TODO: implement this
		//To implement this I have to hold enum of operations and their nagators, e.g. lt - ge, eq - ne etc
		throw new UnsupportedOperationException("Cannot negate boolean function");
	}


	static Collection<Expression<?>> valuesToExpressions(final Collection<?> values) {
		return Collections2.transform(values, value2expression);
	}
}
