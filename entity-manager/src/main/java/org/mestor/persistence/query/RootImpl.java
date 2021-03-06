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

import javax.persistence.criteria.Root;
import javax.persistence.metamodel.EntityType;

import org.mestor.context.EntityContext;

//TODO: add metamodel here
@SuppressWarnings("serial")
class RootImpl<X> extends FromImpl<X, X> implements Root<X> {
	RootImpl(final EntityContext entityContext, final Class<? extends X> clazz) {
		super(entityContext, null, clazz, null);
	}

//	@Override
//	public Set<Join<X, ?>> getJoins() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public boolean isCorrelated() {
//		// TODO Auto-generated method stub
//		return false;
//	}
//
//	@Override
//	public From<X, X> getCorrelationParent() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public <Y> Join<X, Y> join(final SingularAttribute<? super X, Y> attribute) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public <Y> Join<X, Y> join(final SingularAttribute<? super X, Y> attribute, final JoinType jt) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public <Y> CollectionJoin<X, Y> join(final CollectionAttribute<? super X, Y> collection) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public <Y> SetJoin<X, Y> join(final SetAttribute<? super X, Y> set) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public <Y> ListJoin<X, Y> join(final ListAttribute<? super X, Y> list) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public <K, V> MapJoin<X, K, V> join(final MapAttribute<? super X, K, V> map) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public <Y> CollectionJoin<X, Y> join(final CollectionAttribute<? super X, Y> collection, final JoinType jt) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public <Y> SetJoin<X, Y> join(final SetAttribute<? super X, Y> set, final JoinType jt) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public <Y> ListJoin<X, Y> join(final ListAttribute<? super X, Y> list, final JoinType jt) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public <K, V> MapJoin<X, K, V> join(final MapAttribute<? super X, K, V> map, final JoinType jt) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public <X, Y> Join<X, Y> join(final String attributeName) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public <X, Y> CollectionJoin<X, Y> joinCollection(final String attributeName) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public <X, Y> SetJoin<X, Y> joinSet(final String attributeName) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public <X, Y> ListJoin<X, Y> joinList(final String attributeName) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public <X, K, V> MapJoin<X, K, V> joinMap(final String attributeName) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public <X, Y> Join<X, Y> join(final String attributeName, final JoinType jt) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public <X, Y> CollectionJoin<X, Y> joinCollection(final String attributeName, final JoinType jt) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public <X, Y> SetJoin<X, Y> joinSet(final String attributeName, final JoinType jt) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public <X, Y> ListJoin<X, Y> joinList(final String attributeName, final JoinType jt) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public <X, K, V> MapJoin<X, K, V> joinMap(final String attributeName, final JoinType jt) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public Path<?> getParentPath() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public <Y> Path<Y> get(final SingularAttribute<? super X, Y> attribute) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public <E, C extends Collection<E>> Expression<C> get(final PluralAttribute<X, C, E> collection) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public <K, V, M extends Map<K, V>> Expression<M> get(final MapAttribute<X, K, V> map) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public Expression<Class<? extends X>> type() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public <Y> Path<Y> get(final String attributeName) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public Predicate isNull() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public Predicate isNotNull() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public Predicate in(final Object... values) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public Predicate in(final Expression<?>... values) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public Predicate in(final Collection<?> values) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public Predicate in(final Expression<Collection<?>> values) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public <X> Expression<X> as(final Class<X> type) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public Selection<X> alias(final String name) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public boolean isCompoundSelection() {
//		// TODO Auto-generated method stub
//		return false;
//	}
//
//	@Override
//	public List<Selection<?>> getCompoundSelectionItems() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public String getAlias() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public Set<Fetch<X, ?>> getFetches() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public <Y> Fetch<X, Y> fetch(final SingularAttribute<? super X, Y> attribute) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public <Y> Fetch<X, Y> fetch(final SingularAttribute<? super X, Y> attribute, final JoinType jt) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public <Y> Fetch<X, Y> fetch(final PluralAttribute<? super X, ?, Y> attribute) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public <Y> Fetch<X, Y> fetch(final PluralAttribute<? super X, ?, Y> attribute, final JoinType jt) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public <X, Y> Fetch<X, Y> fetch(final String attributeName) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public <X, Y> Fetch<X, Y> fetch(final String attributeName, final JoinType jt) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
	@Override
	public EntityType<X> getModel() {
		// TODO Auto-generated method stub
		return null;
	}



}
