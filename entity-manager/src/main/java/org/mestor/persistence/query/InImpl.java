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

import javax.persistence.criteria.CriteriaBuilder.In;
import javax.persistence.criteria.Expression;

import org.mestor.context.EntityContext;

@SuppressWarnings("serial")
public class InImpl<B> extends BooleanFunctionExpressionImpl implements In<B> {
	@SuppressWarnings("unchecked")
	InImpl(final EntityContext entityContext, final B ... values) {
		super(entityContext, "in", BooleanFunctionExpressionImpl.valuesToExpressions(Arrays.asList(values)));
	}


	@SafeVarargs
	InImpl(final EntityContext entityContext, final Expression<B> ...expressions) {
		this(entityContext, Arrays.<Expression<?>>asList(expressions));
	}

	InImpl(final EntityContext entityContext, final Collection<Expression<?>> expressions) {
		super(entityContext, "in", expressions);
	}


	@Override
	public Expression<B> getExpression() {
		return null; //??
	}

	@SuppressWarnings("unchecked")
	@Override
	public In<B> value(final B value) {
		value(new ConstantExpresion<B>((Class<? extends B>) value.getClass(), value));
		return this;
	}

	@Override
	public In<B> value(final Expression<? extends B> value) {
		addArgument(value);
		return this;
	}

}
