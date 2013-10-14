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

import java.util.List;
import java.util.Set;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import javax.persistence.criteria.Subquery;
import javax.persistence.metamodel.EntityType;

public class CriteriaQueryImpl<T> implements CriteriaQuery<T> {

	@Override
	public <X> Root<X> from(Class<X> entityClass) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <X> Root<X> from(EntityType<X> entity) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<Root<?>> getRoots() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Selection<T> getSelection() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Expression<?>> getGroupList() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Predicate getGroupRestriction() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isDistinct() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Class<T> getResultType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <U> Subquery<U> subquery(Class<U> type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Predicate getRestriction() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CriteriaQuery<T> select(Selection<? extends T> selection) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CriteriaQuery<T> multiselect(Selection<?>... selections) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CriteriaQuery<T> multiselect(List<Selection<?>> selectionList) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CriteriaQuery<T> where(Expression<Boolean> restriction) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CriteriaQuery<T> where(Predicate... restrictions) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CriteriaQuery<T> groupBy(Expression<?>... grouping) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CriteriaQuery<T> groupBy(List<Expression<?>> grouping) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CriteriaQuery<T> having(Expression<Boolean> restriction) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CriteriaQuery<T> having(Predicate... restrictions) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CriteriaQuery<T> orderBy(Order... o) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CriteriaQuery<T> orderBy(List<Order> o) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CriteriaQuery<T> distinct(boolean distinct) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Order> getOrderList() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<ParameterExpression<?>> getParameters() {
		// TODO Auto-generated method stub
		return null;
	}


}
