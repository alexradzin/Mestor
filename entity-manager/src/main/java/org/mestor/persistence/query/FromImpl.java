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
import java.util.Map;
import java.util.Set;

import javax.persistence.criteria.CollectionJoin;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Fetch;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.ListJoin;
import javax.persistence.criteria.MapJoin;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.SetJoin;
import javax.persistence.metamodel.Attribute.PersistentAttributeType;
import javax.persistence.metamodel.Bindable;
import javax.persistence.metamodel.CollectionAttribute;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;

import org.mestor.context.EntityContext;
import org.mestor.metadata.EntityMetadata;
import org.mestor.metadata.FieldMetadata;
import org.mestor.persistence.metamodel.ManagedTypeImpl;

@SuppressWarnings("serial")
class FromImpl<Z, W> extends PathImpl<W> implements From<Z, W> {
	FromImpl(final EntityContext entityContext, final Path<?> parent, final Class<? extends W> javaClass, final Bindable<W> modelArtifact) {
		super(entityContext, parent, javaClass, modelArtifact);
	}

	@Override
	public <Y> Path<Y> get(final SingularAttribute<? super W, Y> attribute) {
        if (attribute.getPersistentAttributeType().equals(PersistentAttributeType.BASIC)){
            return new PathImpl<Y>(getEntityContext(), this, attribute.getBindableJavaType(), attribute);
        }
        throw new IllegalArgumentException("Joins are not supported so far");
	}

	@Override
	public <E, C extends Collection<E>> Expression<C> get(final PluralAttribute<W, C, E> collection) {
        throw new UnsupportedOperationException("Plural attributes cannot be used in from statement right now");
	}

	@Override
	public <K, V, M extends Map<K, V>> Expression<M> get(final MapAttribute<W, K, V> map) {
        throw new UnsupportedOperationException("Map attributes cannot be used in from statement right now");
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Expression<Class<? extends W>> type() {
		return new ExpressionImpl(getEntityContext(), getJavaType());
	}

	@Override
	public <Y> Path<Y> get(final String attributeName) {
		final FieldMetadata<? extends W, Y, ?> fmd = getEntityContext().getEntityMetadata(getJavaType()).getFieldByName(attributeName);
		final EntityMetadata<? extends W> emd = getEntityContext().getEntityMetadata(fmd.getClassType());
		// This is bad to create new instance of ManagedType rather than retrieve it from Metamodel.
		// However this is the easiest way to achieve this instance here now.
		// Probably in future we should add reference to Metamodel instead or additionally to EntityContext to one of the base classes
		// of FromImpl (e.g. SelectionImpl)
		final ManagedType<? extends W> managedType = new ManagedTypeImpl<>(emd);

		@SuppressWarnings("unchecked") //De facto all implementations of Attribute are Bindable
		final Bindable<Y> bindableAttribute = (Bindable<Y>)managedType.getAttribute(attributeName);

		return new PathImpl<Y>(getEntityContext(), this, fmd.getType(), bindableAttribute);
	}



	@Override
	public Set<Fetch<W, ?>> getFetches() {
		throw new UnsupportedOperationException("join");
	}

	@Override
	public <Y> Fetch<W, Y> fetch(final SingularAttribute<? super W, Y> attribute) {
		throw new UnsupportedOperationException("join");
	}

	@Override
	public <Y> Fetch<W, Y> fetch(final SingularAttribute<? super W, Y> attribute, final JoinType jt) {
		throw new UnsupportedOperationException("join");
	}

	@Override
	public <Y> Fetch<W, Y> fetch(final PluralAttribute<? super W, ?, Y> attribute) {
		throw new UnsupportedOperationException("join");
	}

	@Override
	public <Y> Fetch<W, Y> fetch(final PluralAttribute<? super W, ?, Y> attribute, final JoinType jt) {
		throw new UnsupportedOperationException("join");
	}

	@Override
	public <X, Y> Fetch<X, Y> fetch(final String attributeName) {
		throw new UnsupportedOperationException("join");
	}

	@Override
	public <X, Y> Fetch<X, Y> fetch(final String attributeName, final JoinType jt) {
		throw new UnsupportedOperationException("join");
	}

	@Override
	public Set<Join<W, ?>> getJoins() {
		throw new UnsupportedOperationException("join");
	}

	@Override
	public boolean isCorrelated() {
		return false;
	}

	@Override
	public From<Z, W> getCorrelationParent() {
		throw new IllegalStateException("Correlation point is not supported");
	}

	@Override
	public <Y> Join<W, Y> join(final SingularAttribute<? super W, Y> attribute) {
		throw new UnsupportedOperationException("join");
	}

	@Override
	public <Y> Join<W, Y> join(final SingularAttribute<? super W, Y> attribute, final JoinType jt) {
		throw new UnsupportedOperationException("join");
	}

	@Override
	public <Y> CollectionJoin<W, Y> join(final CollectionAttribute<? super W, Y> collection) {
		throw new UnsupportedOperationException("join");
	}

	@Override
	public <Y> SetJoin<W, Y> join(final SetAttribute<? super W, Y> set) {
		throw new UnsupportedOperationException("join");
	}

	@Override
	public <Y> ListJoin<W, Y> join(final ListAttribute<? super W, Y> list) {
		throw new UnsupportedOperationException("join");
	}

	@Override
	public <K, V> MapJoin<W, K, V> join(final MapAttribute<? super W, K, V> map) {
		throw new UnsupportedOperationException("join");
	}

	@Override
	public <Y> CollectionJoin<W, Y> join(final CollectionAttribute<? super W, Y> collection, final JoinType jt) {
		throw new UnsupportedOperationException("join");
	}

	@Override
	public <Y> SetJoin<W, Y> join(final SetAttribute<? super W, Y> set, final JoinType jt) {
		throw new UnsupportedOperationException("join");
	}

	@Override
	public <Y> ListJoin<W, Y> join(final ListAttribute<? super W, Y> list, final JoinType jt) {
		throw new UnsupportedOperationException("join");
	}

	@Override
	public <K, V> MapJoin<W, K, V> join(final MapAttribute<? super W, K, V> map, final JoinType jt) {
		throw new UnsupportedOperationException("join");
	}

	@Override
	public <X, Y> Join<X, Y> join(final String attributeName) {
		throw new UnsupportedOperationException("join");
	}

	@Override
	public <X, Y> CollectionJoin<X, Y> joinCollection(final String attributeName) {
		throw new UnsupportedOperationException("join");
	}

	@Override
	public <X, Y> SetJoin<X, Y> joinSet(final String attributeName) {
		throw new UnsupportedOperationException("join");
	}

	@Override
	public <X, Y> ListJoin<X, Y> joinList(final String attributeName) {
		throw new UnsupportedOperationException("join");
	}

	@Override
	public <X, K, V> MapJoin<X, K, V> joinMap(final String attributeName) {
		throw new UnsupportedOperationException("join");
	}

	@Override
	public <X, Y> Join<X, Y> join(final String attributeName, final JoinType jt) {
		throw new UnsupportedOperationException("join");
	}

	@Override
	public <X, Y> CollectionJoin<X, Y> joinCollection(final String attributeName, final JoinType jt) {
		throw new UnsupportedOperationException("join");
	}

	@Override
	public <X, Y> SetJoin<X, Y> joinSet(final String attributeName, final JoinType jt) {
		throw new UnsupportedOperationException("join");
	}

	@Override
	public <X, Y> ListJoin<X, Y> joinList(final String attributeName, final JoinType jt) {
		throw new UnsupportedOperationException("join");
	}

	@Override
	public <X, K, V> MapJoin<X, K, V> joinMap(final String attributeName, final JoinType jt) {
		throw new UnsupportedOperationException("join");
	}


}
