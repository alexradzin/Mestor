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
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Selection;

import org.mestor.context.EntityContext;

@SuppressWarnings("serial")
public class FunctionExpressionImpl<R> extends ExpressionImpl<R> {
	private final Class<R> returnType;
	private final String function;
	private final List<Expression<?>> arguments = new ArrayList<>();



	@SuppressWarnings("unchecked")
	FunctionExpressionImpl(final EntityContext entityContext, final String function, final Expression<?> argument) {
		this(entityContext, (Class<R>)argument.getJavaType(), function, Collections.<Expression<?>>singleton(argument));
	}



	FunctionExpressionImpl(final EntityContext entityContext, final Class<R> returnType, final String function, final Expression<?> ... arguments) {
		this(entityContext, returnType, function, Arrays.<Expression<?>>asList(arguments));
	}

	@SuppressWarnings("unchecked")
	FunctionExpressionImpl(final EntityContext entityContext, final String function, final Expression<?> ... arguments) {
		this(entityContext, (Class<R>)arguments.getClass().getComponentType(), function, Arrays.<Expression<?>>asList(arguments));
	}

	FunctionExpressionImpl(final EntityContext entityContext, final Class<R> returnType, final String function, final Collection<Expression<?>> arguments) {
		super(entityContext, returnType);
		this.returnType = returnType;
		this.function = function;
		this.arguments.addAll(arguments);

		// Look for the first Path argument to extract alias
		for (final Expression<?> arg : arguments) {
			if (arg instanceof Path) {
				this.alias(arg.getAlias());
				break;
			}
		}
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


	protected void addArgument(final Expression<?> argument) {
		this.arguments.add(argument);
	}

	public List<Expression<?>> getArguments() {
		return Collections.unmodifiableList(arguments);
	}

	public String getFunction() {
		return function;
	}
}
