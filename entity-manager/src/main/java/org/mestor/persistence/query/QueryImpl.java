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
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.Parameter;
import javax.persistence.PersistenceException;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;

import org.mestor.context.EntityContext;
import org.mestor.context.Persistor;
import org.mestor.query.QueryInfo;

import com.google.common.base.Function;

public class QueryImpl<T> implements TypedQuery<T> {
	private final Class<T> resultType;
	private final EntityContext context;
	private final Persistor persistor;

	private int firstResult = 0;
	private int limit = Integer.MAX_VALUE;

	private FlushModeType flushMode = FlushModeType.AUTO;

	private final Map<String, Object> hints = new LinkedHashMap<>();
	private final Map<String, Object> parameterValues = new LinkedHashMap<>();
	private final Map<String, Parameter<?>> parameters = new LinkedHashMap<>();
	private final List<String> parameterNames = new ArrayList<>();
	private final Map<String, Object> parameterInfo = new LinkedHashMap<>();


	private QueryInfo queryInfo;


	public QueryImpl(final Class<T> resultType, final EntityContext context) {
		this.resultType = resultType;
		this.context = context;
		this.persistor = context.getPersistor();
	}


	@Override
	public List<T> getResultList() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public T getSingleResult() {
		return null;
	}

	@Override
	public int executeUpdate() {
		// TODO Auto-generated method stub
		return 0;
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
		final
		Parameter<P> parameter = (Parameter<P>)getParameter(name);
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
		return (P)parameterValues.get(checkBound(param));
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
			final
			C unwrapped = (C) this;
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
		if (!type.isAssignableFrom(valueType)) {
			throw new IllegalArgumentException(
					"Type of query parameter " + type +
					" is not compatible with value of type " + valueType);
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
			return (Parameter<P>)parameters.get(name);
		}
	}


	private class IndexedParameter<P> implements Function<Integer, Parameter<P>> {
		@SuppressWarnings("unchecked")
		@Override
		public Parameter<P> apply(final Integer index) {
			return (Parameter<P>)parameters.get(parameterNames.get(index));
		}
	}

	public QueryInfo getQueryInfo() {
		return queryInfo;
	}
}
