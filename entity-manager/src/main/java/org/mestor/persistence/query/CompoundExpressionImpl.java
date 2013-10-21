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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;

import org.mestor.context.EntityContext;

public class CompoundExpressionImpl extends ExpressionImpl<Boolean> implements Predicate {
	private final BooleanOperator operator;
	private final boolean not;
	protected final List<Expression<Boolean>> expressions = new ArrayList<>();



	@SafeVarargs
	CompoundExpressionImpl(final EntityContext entityContext, final BooleanOperator operator, final Expression<Boolean> ... expressions) {
		this(entityContext, operator, false, expressions);
	}


	@SafeVarargs
	CompoundExpressionImpl(final EntityContext entityContext, final BooleanOperator operator, final boolean not, final Expression<Boolean> ... expressions) {
		this(entityContext, operator, not, Arrays.asList(expressions));
	}


	CompoundExpressionImpl(final EntityContext entityContext, final BooleanOperator operator, final Collection<Expression<Boolean>> expressions) {
		this(entityContext, operator, false, expressions);
	}


	CompoundExpressionImpl(final EntityContext entityContext, final BooleanOperator operator, final boolean not, final Collection<Expression<Boolean>> expressions) {
		super(entityContext, Boolean.class);
		this.operator = operator;
		this.not = not;
		this.expressions.addAll(expressions);
	}

	@Override
	public BooleanOperator getOperator() {
		return operator;
	}

	@Override
	public boolean isNegated() {
		return not;
	}

	@Override
	public List<Expression<Boolean>> getExpressions() {
		return expressions;
	}

	@Override
	public Predicate not() {
		return new CompoundExpressionImpl(getEntityContext(), operator, !not, expressions);
	}
}
