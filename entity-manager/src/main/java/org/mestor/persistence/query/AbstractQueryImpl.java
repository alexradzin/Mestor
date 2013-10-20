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
import java.util.Collections;
import java.util.List;

import javax.persistence.criteria.AbstractQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import javax.persistence.metamodel.EntityType;


abstract class AbstractQueryImpl<T, S extends CommonAbstractCriteriaBase<T, S> & AbstractQuery<T>> extends CommonAbstractCriteriaBase<T, S>  implements AbstractQuery<T> {
//    protected Metamodel metamodel;

    private boolean distinct = false;
    private final List<Expression<?>> groupBy = new ArrayList<Expression<?>>();

    protected Selection<? extends T> selection;


	protected AbstractQueryImpl(final Class<T> resultClass, final CriteriaBuilder queryBuilder) {
		super(resultClass, queryBuilder);
	}

	@Override
	public <X> Root<X> from(final Class<X> entityClass) {
		return addRoot(new RootImpl<X>(entityClass));
	}

	@Override
	public <X> Root<X> from(final EntityType<X> entity) {
		return from(entity.getJavaType());
	}

	@Override
	public S groupBy(final Expression<?>... grouping) {
		return groupBy(asList(grouping));
	}

	@Override
	public S groupBy(final List<Expression<?>> grouping) {
		groupBy.addAll(grouping);
		return me();
	}

	@Override
	public S having(final Expression<Boolean> restriction) {
		throw new UnsupportedOperationException("HAVING is not supporteed");
	}

	@Override
	public S having(final Predicate... restrictions) {
		for (final Predicate restriction : restrictions) {
			having(restriction);
		}
		return me();
	}

	@Override
	public S distinct(final boolean distinct) {
		this.distinct = distinct;
		return me();
	}

//	@Override
//	public Set<Root<?>> getRoots() {
//		return roots;
//	}

	@SuppressWarnings("unchecked")
	@Override
	public Selection<T> getSelection() {
		return (Selection<T>)selection;
	}

	@Override
	public List<Expression<?>> getGroupList() {
		return groupBy;
	}

	@Override
	public Predicate getGroupRestriction() {
		throw new UnsupportedOperationException("HAVING is not supporteed");
	}

	@Override
	public boolean isDistinct() {
		return distinct;
	}

	@Override
	public Class<T> getResultType() {
		return resultClass;
	}

	@SuppressWarnings("unchecked")
	private S me() {
        return (S)this;
	}

	@SafeVarargs
	protected final <X> List<X> asList( final X ... elements) {
		return elements == null ? Collections.<X>emptyList() : Arrays.asList(elements);
	}

//	@Override
//	protected final <R> Root<R> addRoot(final Root<R> root) {
//		roots.add(root);
//		return root;
//	}
}
