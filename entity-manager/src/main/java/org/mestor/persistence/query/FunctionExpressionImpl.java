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
import java.util.Collections;
import java.util.List;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Selection;

public class FunctionExpressionImpl<R> extends ExpressionImpl<R> {
	private final Class<R> returnType;
	private final String function;
	private final List<Expression<?>> arguments = new ArrayList<>();



	@SuppressWarnings("unchecked")
	FunctionExpressionImpl(final String function, final Expression<?> argument) {
		this((Class<R>)argument.getJavaType(), function, Collections.<Expression<?>>singleton(argument));
	}



	FunctionExpressionImpl(final Class<R> returnType, final String function, final Expression<?> ... arguments) {
		this(returnType, function, Arrays.<Expression<?>>asList(arguments));
	}

	@SuppressWarnings("unchecked")
	FunctionExpressionImpl(final String function, final Expression<?> ... arguments) {
		this((Class<R>)arguments.getClass().getComponentType(), function, Arrays.<Expression<?>>asList(arguments));
	}

	FunctionExpressionImpl(final Class<R> returnType, final String function, final Collection<Expression<?>> arguments) {
		super(returnType);
		this.returnType = returnType;
		this.function = function;
		this.arguments.addAll(arguments);
	}


	@Override
	public Selection<R> alias(final String name) {
		return this;
	}

	@Override
	public boolean isCompoundSelection() {
		return false;
	}

	@Override
	public List<Selection<?>> getCompoundSelectionItems() {
		return null;
	}

	@Override
	public Class<? extends R> getJavaType() {
		return returnType;
	}

	@Override
	public String getAlias() {
		return function;
	}

	@Override
	public Predicate isNull() {
		return null;
	}

	@Override
	public Predicate isNotNull() {
		return null;
	}

	@Override
	public Predicate in(final Object... values) {
		return null;
	}

	@Override
	public Predicate in(final Expression<?>... values) {
		return null;
	}

	@Override
	public Predicate in(final Collection<?> values) {
		return null;
	}

	@Override
	public Predicate in(final Expression<Collection<?>> values) {
		return null;
	}

	@Override
	public <X> Expression<X> as(final Class<X> type) {
		return null;
	}

	protected void addArgument(final Expression<?> argument) {
		this.arguments.add(argument);
	}
}
