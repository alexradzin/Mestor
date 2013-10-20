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

import java.util.LinkedHashSet;
import java.util.Set;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import javax.persistence.criteria.Subquery;

public class CommonAbstractCriteriaBase<T, S extends AbstractQueryCriteria<T, S>> implements AbstractQueryCriteria<T, S> {
	protected final Class<T> resultClass;


	private final Set<Root<?>> roots;
	private Expression<Boolean> where;

	protected Selection<? extends T> selection;
	protected final CriteriaBuilder queryBuilder;

	protected CommonAbstractCriteriaBase(final Class<T> resultClass, final CriteriaBuilder queryBuilder) {
		this(resultClass, queryBuilder,  new LinkedHashSet<Root<?>>());
	}


	protected CommonAbstractCriteriaBase(final Class<T> resultClass, final CriteriaBuilder queryBuilder, final Set<Root<?>> roots) {
		this.resultClass = resultClass;
		this.queryBuilder = queryBuilder;
		this.roots = roots;
	}


	@Override
	public <U> Subquery<U> subquery(final Class<U> type) {
		return new SubqueryImpl<U>(type, queryBuilder, this);
	}

	@Override
	public Predicate getRestriction() {
		if (where == null) {
			return null;
		}
		if (where instanceof Predicate) {
			return (Predicate) where;
		}
		return queryBuilder.isTrue(where);
	}



//	@Override
//	public <X> Root<X> from(final Class<X> entityClass) {
//		return addRoot(new RootImpl<X>(entityClass));
//	}
//
//	@Override
//	public <X> Root<X> from(final EntityType<X> entity) {
//		return from(entity.getJavaType());
//	}

	@Override
	public S where(final Expression<Boolean> restriction) {
        this.where = restriction;
        return me();
	}

	@Override
	public S where(final Predicate... restrictions) {
        where = (restrictions == null || restrictions.length == 0) ? null : queryBuilder.and(restrictions);
        return me();
    }

	public Set<Root<?>> getRoots() {
		return roots;
	}


	@SuppressWarnings("unchecked")
	private S me() {
        return (S)this;
	}



	protected final <R> Root<R> addRoot(final Root<R> root) {
		roots.add(root);
		return root;
	}

}
