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

import java.util.Collection;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;

public class ExpressionImpl<T> extends SelectionImpl<T> implements Expression<T> {

	ExpressionImpl(final Class<? extends T> type) {
		super(type);
	}

	@Override
	public Predicate isNull() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Predicate isNotNull() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Predicate in(final Object... values) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Predicate in(final Expression<?>... values) {
		return new BooleanFunctionExpressionImpl("in", values);
	}

	@Override
	public Predicate in(final Collection<?> values) {
		return new BooleanFunctionExpressionImpl("in", BooleanFunctionExpressionImpl.valuesToExpressions(values));
	}

	@Override
	public Predicate in(final Expression<Collection<?>> values) {
		return new BooleanFunctionExpressionImpl("in", values);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <X> Expression<X> as(final Class<X> type) {
		return (Expression<X>)this;
	}

}
