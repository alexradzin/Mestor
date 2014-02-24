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

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;

import org.mestor.context.EntityContext;

@SuppressWarnings("serial")
public class ExpressionImpl<T> extends SelectionImpl<T> implements Expression<T> {
	ExpressionImpl(final EntityContext entityContext, final Class<? extends T> type) {
		super(entityContext, type);
	}

	@Override
	public Predicate isNull() {
		throw new UnsupportedOperationException("is null");
	}

	@Override
	public Predicate isNotNull() {
		throw new UnsupportedOperationException("is not null");
	}

	@Override
	public Predicate in(final Object... values) {
		return in(values == null ? null : Arrays.asList(values));
	}

	@Override
	public Predicate in(final Expression<?>... values) {
		return setThisAlias(new BooleanFunctionExpressionImpl(getEntityContext(), "in", values));
	}

	@Override
	public Predicate in(final Collection<?> values) {
		return setThisAlias(new BooleanFunctionExpressionImpl(getEntityContext(), "in", BooleanFunctionExpressionImpl.valuesToExpressions(values)));
	}

	@Override
	public Predicate in(final Expression<Collection<?>> values) {
		return setThisAlias(new BooleanFunctionExpressionImpl(getEntityContext(), "in", values));
	}

	@SuppressWarnings("unchecked")
	@Override
	public <X> Expression<X> as(final Class<X> type) {
		return (Expression<X>)this;
	}

	private Predicate setThisAlias(final Predicate predicate) {
		predicate.alias(getAlias());
		return predicate;
	}
}
