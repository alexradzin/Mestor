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

import static org.mestor.query.OrderByInfo.Order.ASC;
import static org.mestor.query.OrderByInfo.Order.DSC;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.annotation.Nullable;
import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.Parameter;
import javax.persistence.PersistenceException;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Predicate.BooleanOperator;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;

import org.mestor.context.EntityContext;
import org.mestor.context.Persistor;
import org.mestor.metadata.EntityMetadata;
import org.mestor.query.ArgumentInfo;
import org.mestor.query.ClauseInfo;
import org.mestor.query.ClauseInfo.Operand;
import org.mestor.query.OrderByInfo;
import org.mestor.query.QueryInfo;
import org.mestor.query.QueryInfo.QueryType;
import org.mestor.util.TypeUtil;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;

public class QueryImpl<T> implements TypedQuery<T> {
	private final static int DEFAULT_LIMIT = Integer.MAX_VALUE;

	private final Class<T> resultType;
	private final EntityContext context;
	private final Persistor persistor;

	private int firstResult = 0;
	private int limit = DEFAULT_LIMIT;

	private FlushModeType flushMode = FlushModeType.AUTO;

	private final Map<String, Object> hints = new LinkedHashMap<>();
	private final Map<String, Object> parameterValues = new LinkedHashMap<>();
	private final Map<String, Parameter<?>> parameters = new LinkedHashMap<>();
	private final List<String> parameterNames = new ArrayList<>();
	private final Map<String, Object> parameterInfo = new LinkedHashMap<>();

	private final QueryInfo queryInfo;

	public QueryImpl(final CriteriaQuery<T> criteriaQuery, final EntityContext context) {
		this.resultType = criteriaQuery.getResultType();
		this.context = context;
		this.persistor = context.getPersistor();

		// TODO extract all data from criteria query

		Map<String, Object> what = null;
		final Selection<T> selection = criteriaQuery.getSelection();
		if (selection != null) {
			if (selection.isCompoundSelection()) {
				what = new LinkedHashMap<>();
				for (final Selection<?> s : selection.getCompoundSelectionItems()) {
					final String alias = s.getAlias();
					what.put(alias, alias);
				}
			} else {
				String alias = selection.getAlias();
				final Object field;
				if (selection instanceof FunctionExpressionImpl) {
					final FunctionExpressionImpl<?> funcExp = (FunctionExpressionImpl<?>)selection;
					final String function = funcExp.getFunction();
					switch(function){
						case "count":
							field = "count(*)";
							break;
						case "max":
						case "min":
						case "sum":
						case "upper":
							field = function + "(" + alias + ")";
							break;
						default:
							throw new UnsupportedOperationException(function);
					}
				} else if (selection instanceof ConstantExpresion) {
					final ConstantExpresion<?> ce = (ConstantExpresion<?>) selection;
					final Object value = ce.getValue();
					if(alias == null){
						alias = String.valueOf(value);
					}
					field = value;
				} else {
					field = alias;
				}
				
				what = Collections.<String, Object> singletonMap(alias, field);
			}
		}

		final Collection<Root<?>> roots = criteriaQuery.getRoots();
		Map<String, String> from = null;
		if (roots != null) {
			from = new LinkedHashMap<>();
			for (final Root<?> root : roots) {
				final Class<?> type = root.getJavaType();
				final String table = context.getEntityMetadata(type).getTableName();
				final String alias = root.getAlias();
				from.put(table, alias);
			}
		}

		final ClauseInfo where = createClauseInfo(criteriaQuery.getRestriction());

		final Collection<OrderByInfo> order = createOrderBy(criteriaQuery.getOrderList());

		queryInfo = new QueryInfo(QueryType.SELECT, what, from, where, order);
		initParams();
	}

	public QueryImpl(final QueryInfo queryInfo, final EntityContext context) {
		this.resultType = getResultType(queryInfo, context);
		this.context = context;
		this.persistor = context.getPersistor();
		this.queryInfo = queryInfo;
		initParams();
	}
	
	private void initParams(){
		final ClauseInfo where = queryInfo.getWhere();
		if(where == null){
			return;
		}
		
		if(where.getExpression() instanceof ArgumentInfo) {
			final EntityMetadata<T> emd = context.getEntityMetadata(resultType);
			final String name = where.getField();
			
			final Class<?> fieldType = emd.getFieldByName(name).getType();
			
			parameterNames.add(name);
			parameters.put(name, new ParameterExpressionImpl<>(name, fieldType));
		}
		
	}


	/**
	 * Retrieves result type from {@code queryInfo} utilizing provided {@code context}.
	 * Can throw various {@link RuntimeException}s if specified {@code queryInfo} is wrong.
	 * @param queryInfo
	 * @param context
	 * @return query result type
	 * @throws NullPointerException if {@code queryInfo} or {@link QueryInfo#getFrom()} are {@code null}.
	 * @throws NullPointerException if entity metadata cannot be found in context by name extracted from {@link QueryInfo#getFrom()}.
	 * @throws NoSuchElementException if {@link QueryInfo#getFrom()} is empty
	 */
	private Class<T> getResultType(final QueryInfo queryInfo, final EntityContext context) {
		final String entityName = queryInfo.getFrom().entrySet().iterator().next().getValue();
		final EntityMetadata<T> emd = context.getEntityMetadata(entityName);
		return emd.getEntityType();
	}

	private Collection<OrderByInfo> createOrderBy(final List<Order> orderList) {
		if (orderList == null || orderList.isEmpty()) {
			return null;
		}
		final Collection<OrderByInfo> res = new ArrayList<>(orderList.size());
		for(final Order o : orderList){
			final Expression<?> exp = o.getExpression();
			final String field = exp.getAlias();
			final OrderByInfo obi = new OrderByInfo(field, o.isAscending() ? ASC : DSC);
			res.add(obi);
		}
		return res;
	}

	@Override
	public List<T> getResultList() {
		return getResultList(limit);
	}

	@Override
	public T getSingleResult() {
		final List<T> list = getResultList(1);
		return list == null || list.isEmpty() ? null : list.get(0);
	}

	private List<T> getResultList(final int curentLimit) {
		QueryInfo qi = queryInfo;
		if (curentLimit != DEFAULT_LIMIT) {
			qi = new QueryInfo(queryInfo.getType(), queryInfo.getWhat(), queryInfo.getFrom(), queryInfo.getWhere(), queryInfo.getOrders(), queryInfo.getStart(), curentLimit);
		}
		return persistor.selectQuery(qi, parameterValues);
	}


	@Override
	public int executeUpdate() {
		throw new IllegalStateException("Cannot execute update using CriteriaQuery");
	}

	@Override
	public TypedQuery<T> setMaxResults(final int maxResult) {
		limit = maxResult;
		return this;
	}

	@Override
	public int getMaxResults() {
		return limit;
	}

	@Override
	public TypedQuery<T> setFirstResult(final int startPosition) {
		firstResult = startPosition;
		return this;
	}

	@Override
	public int getFirstResult() {
		return firstResult;
	}

	@Override
	public TypedQuery<T> setHint(final String hintName, final Object value) {
		hints.put(hintName, value);
		return this;
	}

	@Override
	public Map<String, Object> getHints() {
		return hints;
	}

	@Override
	public <P> TypedQuery<T> setParameter(final Parameter<P> param, final P value) {
		return setParameter(param, value, null);
	}

	@Override
	public TypedQuery<T> setParameter(final Parameter<Calendar> param, final Calendar value, final TemporalType temporalType) {
		return setParameter(param, value, temporalType);
	}

	@Override
	public TypedQuery<T> setParameter(final Parameter<Date> param, final Date value, final TemporalType temporalType) {
		return setParameter(param, value, temporalType);
	}

	@Override
	public TypedQuery<T> setParameter(final String name, final Object value) {
		return setReferencedParameter(new NamedParameter<Object>(), name, value, null);
	}

	@Override
	public TypedQuery<T> setParameter(final String name, final Calendar value, final TemporalType temporalType) {
		return setParameter(name, value, temporalType);
	}

	@Override
	public TypedQuery<T> setParameter(final String name, final Date value, final TemporalType temporalType) {
		return setParameter(name, value, temporalType);
	}

	@Override
	public TypedQuery<T> setParameter(final int position, final Object value) {
		return setReferencedParameter(new IndexedParameter<Object>(), position, value, null);
	}

	@Override
	public TypedQuery<T> setParameter(final int position, final Calendar value, final TemporalType temporalType) {
		return setReferencedParameter(new IndexedParameter<Calendar>(), position, value, temporalType);
	}

	@Override
	public TypedQuery<T> setParameter(final int position, final Date value, final TemporalType temporalType) {
		return setReferencedParameter(new IndexedParameter<Date>(), position, value, temporalType);
	}

	@Override
	public Set<Parameter<?>> getParameters() {
		return new LinkedHashSet<Parameter<?>>(parameters.values());
	}

	@Override
	public Parameter<?> getParameter(final String name) {
		return notNull(parameters.get(name), name);
	}

	@Override
	public <P> Parameter<P> getParameter(final String name, final Class<P> type) {
		@SuppressWarnings("unchecked")
		final Parameter<P> parameter = (Parameter<P>) getParameter(name);
		checkType(parameter.getParameterType(), type);
		return parameter;
	}

	@Override
	public Parameter<?> getParameter(final int position) {
		return getParameter(parameterNames.get(checkParameterPosition(position)));
	}

	@Override
	public <P> Parameter<P> getParameter(final int position, final Class<P> type) {
		return getParameter(parameterNames.get(checkParameterPosition(position)), type);
	}

	@Override
	public boolean isBound(final Parameter<?> param) {
		return parameterValues.containsKey(param.getName());
	}

	@SuppressWarnings("unchecked")
	@Override
	public <P> P getParameterValue(final Parameter<P> param) {
		return (P) parameterValues.get(checkBound(param));
	}

	@Override
	public Object getParameterValue(final String name) {
		return getParameterValue(getParameter(name));
	}

	@Override
	public Object getParameterValue(final int position) {
		return getParameterValue(getParameter(position));
	}

	@Override
	public TypedQuery<T> setFlushMode(final FlushModeType flushMode) {
		this.flushMode = flushMode;
		return this;
	}

	@Override
	public FlushModeType getFlushMode() {
		return flushMode;
	}

	@Override
	public TypedQuery<T> setLockMode(final LockModeType lockMode) {
		if (!LockModeType.NONE.equals(lockMode)) {
			throw new IllegalArgumentException("Locking is unsupported");
		}
		return this;
	}

	@Override
	public LockModeType getLockMode() {
		return LockModeType.NONE;
	}

	@Override
	public <C> C unwrap(final Class<C> cls) {
		if (cls.isAssignableFrom(this.getClass())) {
			// unwraps any proxy to Query, JPAQuery or EJBQueryImpl
			@SuppressWarnings("unchecked")
			final C unwrapped = (C) this;
			return unwrapped;
		}

		throw new PersistenceException("Could not unwrap query to: " + cls);
	}

	private <P> TypedQuery<T> setParameter(final Parameter<P> param, final P value, final Object info) {
		final String name = param.getName();
		parameterValues.put(name, value);
		parameters.put(name, param);
		parameterNames.add(name);
		if (info != null) {
			parameterInfo.put(name, info);
		}
		return this;
	}

	private <R, V> TypedQuery<T> setReferencedParameter(final Function<R, Parameter<V>> accessor, final R ref, final V value, final Object info) {
		final Parameter<V> param = notNull(accessor.apply(ref), ref);
		return setParameter(param, isAssignable(value, param.getParameterType()), info);
	}

	private <V, I> V notNull(final V value, final I id) {
		if (value == null) {
			throw new IllegalArgumentException("Unknown query parameter " + id);
		}
		return value;
	}

	private int checkParameterPosition(final int position) {
		if (position < 0 || position >= parameterNames.size()) {
			throw new IllegalArgumentException("Unknown query parameter " + position);
		}
		return position;
	}

	private <V> V isAssignable(final V value, final Class<V> type) {
		if (value != null) {
			checkType(value.getClass(), type);
		}
		return value;
	}

	private <V, C> void checkType(final Class<V> valueType, final Class<C> type) {
		if (!TypeUtil.compareTypes(type, valueType)) {
			throw new IllegalArgumentException("Type of query parameter " + type + " is not compatible with value of type " + valueType);
		}
	}

	private <P> Parameter<P> checkBound(final Parameter<P> param) {
		if (!isBound(param)) {
			throw new IllegalArgumentException("Unbound query parameter " + param.getName());
		}
		return param;
	}

	private class NamedParameter<P> implements Function<String, Parameter<P>> {
		@SuppressWarnings("unchecked")
		@Override
		public Parameter<P> apply(final String name) {
			return (Parameter<P>) parameters.get(name);
		}
	}

	private class IndexedParameter<P> implements Function<Integer, Parameter<P>> {
		@SuppressWarnings("unchecked")
		@Override
		public Parameter<P> apply(final Integer index) {
			return (Parameter<P>) parameters.get(parameterNames.get(index));
		}
	}

	public QueryInfo getQueryInfo() {
		return queryInfo;
	}

	private ClauseInfo createClauseInfo(final Predicate restriction) {
		if (restriction == null) {
			return null;
		}
		final List<Expression<Boolean>> expressions = restriction.getExpressions();

		if (expressions == null || expressions.isEmpty()) {
			return null;
		}
		// final Predicate expression = (Predicate)expressions.get(0); //TODO:
		// add support of several expressions

		final List<ClauseInfo> clauses = createClauses(expressions);

		if (clauses.size() == 1) {
			return clauses.get(0);
		}

		final BooleanOperator operator = restriction.getOperator();
		final Operand operand = Operand.valueOf(operator.name());
		return new ClauseInfo(restriction.getAlias(), operand, clauses.toArray(new ClauseInfo[0]));

		// TODO: add support of In statement
	}

	private List<ClauseInfo> createClauses(final List<Expression<Boolean>> expressions) {
		final List<ClauseInfo> clauses = new ArrayList<>();

		for (final Expression<Boolean> expression : expressions) {
			if (expression instanceof FunctionExpressionImpl) {
				final FunctionExpressionImpl<Boolean> function = ((FunctionExpressionImpl<Boolean>) expression);
				// if function is unsupported IllegalArgumentException will be
				// thrown
				final Operand operand = Operand.valueOf(function.getFunction().toUpperCase());
				final Collection<Object> values = Collections2.transform(Collections2.filter(function.getArguments(), new com.google.common.base.Predicate<Expression<?>>() {
					@Override
					public boolean apply(@Nullable final Expression<?> expr) {
						return expr instanceof ConstantExpresion;
					}
				}), new Function<Expression<?>, Object>() {
					@Override
					public Object apply(@Nullable final Expression<?> expr) {
						if (expr instanceof ConstantExpresion) {
							return ((ConstantExpresion<?>) expr).getValue();
						}
						throw new UnsupportedOperationException(expr.getClass() + " TBD");
					}
				});
				clauses.add(new ClauseInfo(expression.getAlias(), operand, values));
			} else if (expression instanceof CompoundExpressionImpl) {
				final CompoundExpressionImpl cExpr = (CompoundExpressionImpl) expression;
				final List<Expression<Boolean>> subClosesList = cExpr.getExpressions();
				if (!subClosesList.isEmpty()) {
					final ClauseInfo[] subCloses = createClauses(subClosesList).toArray(new ClauseInfo[0]);
					final Operand operand = Operand.valueOf(cExpr.getOperator().name());
					clauses.add(new ClauseInfo(expression.getAlias(), operand, subCloses));
				}
			} else {
				throw new UnsupportedOperationException("Cannot create ClauseInfo from " + expression.getClass() + ": TBD");
			}
		}
		return clauses;
	}
}
