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
import java.util.Set;

import javax.persistence.criteria.AbstractQuery;
import javax.persistence.criteria.CollectionJoin;
import javax.persistence.criteria.CommonAbstractCriteria;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.ListJoin;
import javax.persistence.criteria.MapJoin;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import javax.persistence.criteria.SetJoin;
import javax.persistence.criteria.Subquery;

class SubqueryImpl<T> extends AbstractQueryImpl<T, SubqueryImpl<T>> implements Subquery<T> {
	private String alias;
	private final CommonAbstractCriteria parent;

	SubqueryImpl(final Class<T> resultClass, final CriteriaBuilder queryBuilder, final CommonAbstractCriteria parent) {
		super(resultClass, queryBuilder);
		this.parent = parent;
	}


	@Override
	public Predicate isNull() {
		return checkNull("is null");
	}

	@Override
	public Predicate isNotNull() {
		return checkNull("not null");
	}

	private Predicate checkNull(final String cmd) {
		return new BooleanFunctionExpressionImpl(cmd);
	}

	@Override
	public Expression<T> getSelection() {
		return (Expression<T>)super.getSelection();
	}

	@Override
	public Predicate in(final Object... values) {
		return new InImpl<>(values);
	}

	@Override
	public Predicate in(final Expression<?>... values) {
		return new InImpl<Object>(Arrays.asList(values));
	}

	@Override
	public Predicate in(final Collection<?> values) {
		return new InImpl<Object>(values);
	}

	@Override
	public Predicate in(final Expression<Collection<?>> values) {
		return new InImpl<Object>(values);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <X> Expression<X> as(final Class<X> type) {
		return (Expression<X>) this;
	}

    @Override
	public Selection<T> alias(final String name) {
        this.alias = name;
        return this;
    }

    @Override
	public String getAlias() {
        return this.alias;
    }

    @Override
	public Class<T> getJavaType() {
        return getResultType();
    }

	@Override
	public boolean isCompoundSelection() {
		return false;
	}

	@Override
	public List<Selection<?>> getCompoundSelectionItems() {
        throw new IllegalStateException("Criteria not a compound selection");
	}


	@Override
	public Subquery<T> select(final Expression<T> expression) {
		selection = expression;
		return this;
	}

	@Override
	public <Y> Root<Y> correlate(final Root<Y> parentRoot) {
		return addRoot(new RootImpl<Y>(parentRoot.getJavaType()));
	}

	@Override
	public <X, Y> Join<X, Y> correlate(final Join<X, Y> parentJoin) {
		throw new UnsupportedOperationException("Joins are not supported");
	}

	@Override
	public <X, Y> CollectionJoin<X, Y> correlate(final CollectionJoin<X, Y> parentCollection) {
		throw new UnsupportedOperationException("Joins are not supported");
	}

	@Override
	public <X, Y> SetJoin<X, Y> correlate(final SetJoin<X, Y> parentSet) {
		throw new UnsupportedOperationException("Joins are not supported");
	}

	@Override
	public <X, Y> ListJoin<X, Y> correlate(final ListJoin<X, Y> parentList) {
		throw new UnsupportedOperationException("Joins are not supported");
	}

	@Override
	public <X, K, V> MapJoin<X, K, V> correlate(final MapJoin<X, K, V> parentMap) {
		throw new UnsupportedOperationException("Joins are not supported");
	}

	@Override
	public AbstractQuery<?> getParent() {
		return (AbstractQuery<?>)parent;
	}

	@Override
	public CommonAbstractCriteria getContainingQuery() {
		return parent;
	}

	@Override
	public Set<Join<?, ?>> getCorrelatedJoins() {
		throw new UnsupportedOperationException("Joins are not supported");
	}

}
