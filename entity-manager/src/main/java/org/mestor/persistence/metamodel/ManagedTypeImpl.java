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

package org.mestor.persistence.metamodel;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.CollectionAttribute;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.IdentifiableType;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.Type;

import org.mestor.metadata.EntityMetadata;
import org.mestor.metadata.FieldMetadata;

import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class ManagedTypeImpl<T> implements ManagedType<T>, EntityType<T> {
	private final EntityMetadata<T> emd;
	private final Class<T> clazz;
	private final PersistenceType persistenceType;
	private final String name;

	private final Map<String, Attribute<T, ?>> members = new LinkedHashMap<>();


	private final Predicate<Attribute<T, ?>> isDeclaredHere = new Predicate<Attribute<T, ?>>() {
		@Override
		public boolean apply(final Attribute<T, ?> attribute) {
			return attribute.getDeclaringType().getJavaType().equals(getJavaType());
		}};

	@SuppressWarnings({ "rawtypes", "unchecked" }) // I do not know why do I need this suppression. TODO: try to fix this.
	private final Predicate<Attribute<T, ?>> isSingularAttribute = new TypedAttributePredicate(SingularAttribute.class);

	@SuppressWarnings({ "rawtypes", "unchecked" }) // I do not know why do I need this suppression. TODO: try to fix this.
	private final Predicate<Attribute<T, ?>> isPluralAttribute = new TypedAttributePredicate(PluralAttribute.class);


	@SuppressWarnings({ "rawtypes", "unchecked" })
	public ManagedTypeImpl(final EntityMetadata<T> emd) {
		this.emd = emd;
		clazz = emd.getEntityType();
		name = emd.getEntityName();

		this.persistenceType = Utils.getAnnotatedCategory(
				Utils.typeAnnotations,
				emd.getClass(),
				Lists.reverse(Arrays.asList(PersistenceType.values())).toArray(new PersistenceType[0]),
				PersistenceType.BASIC);


		for (final FieldMetadata<T, Object, Object> fmd : emd.getFields()) {
			final Class<?> type = fmd.getType();

			Attribute<T, Object> attribute;
			if (List.class.isAssignableFrom(type)) {
				attribute =  new ListAttributeImpl(this, fmd);
			} else if (Set.class.isAssignableFrom(type)) {
				attribute =  new SetAttributeImpl(this, fmd);
			} else if (Map.class.isAssignableFrom(type)) {
				attribute =  new MapAttributeImpl(this, fmd);
			} else {
				attribute =  new SingularAttributeImpl<T, Object>(this, fmd);
			}
			members.put(fmd.getName(), attribute);
		}
	}

	@Override
	public PersistenceType getPersistenceType() {
		return persistenceType;
	}

	@Override
	public Class<T> getJavaType() {
		return clazz;
	}

	@Override
	public Set<Attribute<? super T, ?>> getAttributes() {
		return new LinkedHashSet<Attribute<? super T, ?>>(members.values());
	}

	@Override
	public Set<Attribute<T, ?>> getDeclaredAttributes() {
		return Sets.filter(new LinkedHashSet<Attribute<T, ?>>(members.values()), isDeclaredHere);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <Y> SingularAttribute<? super T, Y> getSingularAttribute(final String name, final Class<Y> type) {
		return new SingularAttributeImpl<T, Y>(this, (FieldMetadata<T, Y, ?>)emd.getFieldByName(name));
	}

	@Override
	public <Y> SingularAttribute<T, Y> getDeclaredSingularAttribute(final String name, final Class<Y> type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<SingularAttribute<? super T, ?>> getSingularAttributes() {
		return getTypedAttributes(new LinkedHashSet<SingularAttribute<? super T, ?>>(), isSingularAttribute);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Set<SingularAttribute<T, ?>> getDeclaredSingularAttributes() {
		return getTypedAttributes(new LinkedHashSet(), Predicates.and(isSingularAttribute, isDeclaredHere));
	}

	@Override
	public <E> CollectionAttribute<? super T, E> getCollection(final String name, final Class<E> elementType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <E> CollectionAttribute<T, E> getDeclaredCollection(final String name, final Class<E> elementType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <E> SetAttribute<? super T, E> getSet(final String name, final Class<E> elementType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <E> SetAttribute<T, E> getDeclaredSet(final String name, final Class<E> elementType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <E> ListAttribute<? super T, E> getList(final String name, final Class<E> elementType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <E> ListAttribute<T, E> getDeclaredList(final String name, final Class<E> elementType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <K, V> MapAttribute<? super T, K, V> getMap(final String name, final Class<K> keyType, final Class<V> valueType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <K, V> MapAttribute<T, K, V> getDeclaredMap(final String name, final Class<K> keyType, final Class<V> valueType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<PluralAttribute<? super T, ?, ?>> getPluralAttributes() {
		return getTypedAttributes(new LinkedHashSet<PluralAttribute<? super T, ?, ?>>(), isPluralAttribute);
	}

	@Override
	public Set<PluralAttribute<T, ?, ?>> getDeclaredPluralAttributes() {
		return getTypedAttributes(new LinkedHashSet<PluralAttribute<T, ?, ?>>(), Predicates.and(isPluralAttribute, isDeclaredHere));
	}

	@Override
	public Attribute<? super T, ?> getAttribute(final String name) {
		return strict(
				members.get(name),
				Predicates.<Attribute<? super T, ?>>notNull(),
				"managed_type_attribute_not_present", name, clazz);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Attribute<T, ?> getDeclaredAttribute(final String name) {
		return strict(
				(Attribute<T, ?>)getAttribute(name),
				isDeclaredHere,
				"managed_type_declared_attribute_not_present_but_is_on_superclass", name, clazz);
	}

	@Override
	public SingularAttribute<? super T, ?> getSingularAttribute(final String name) {
		return (SingularAttribute<? super T, ?>)getAttribute(name);
	}

	@Override
	public SingularAttribute<T, ?> getDeclaredSingularAttribute(final String name) {
		return (SingularAttribute<T, ?>)getDeclaredAttribute(name);
	}

	@SuppressWarnings("unchecked")
	@Override
	public CollectionAttribute<? super T, ?> getCollection(final String name) {
		return (CollectionAttribute<? super T, ?>)getAttribute(name);
	}

	@SuppressWarnings("unchecked")
	@Override
	public CollectionAttribute<T, ?> getDeclaredCollection(final String name) {
		return (CollectionAttribute<T, ?>)getDeclaredAttribute(name);
	}

	@SuppressWarnings("unchecked")
	@Override
	public SetAttribute<? super T, ?> getSet(final String name) {
		return (SetAttribute<? super T, ?>)getAttribute(name);
	}

	@SuppressWarnings("unchecked")
	@Override
	public SetAttribute<T, ?> getDeclaredSet(final String name) {
		return (SetAttribute<T, ?>)getDeclaredAttribute(name);
	}

	@SuppressWarnings("unchecked")
	@Override
	public ListAttribute<? super T, ?> getList(final String name) {
		return (ListAttribute<? super T, ?>)getAttribute(name);
	}

	@SuppressWarnings("unchecked")
	@Override
	public ListAttribute<T, ?> getDeclaredList(final String name) {
		return (ListAttribute<T, ?>)getDeclaredAttribute(name);
	}

	@SuppressWarnings("unchecked")
	@Override
	public MapAttribute<? super T, ?, ?> getMap(final String name) {
		return (MapAttribute<? super T, ?, ?>)getAttribute(name);
	}

	@SuppressWarnings("unchecked")
	@Override
	public MapAttribute<T, ?, ?> getDeclaredMap(final String name) {
		return (MapAttribute<T, ?, ?>)getAttribute(name);
	}

	private <A extends Attribute<? super T, ?>> Set<A> getTypedAttributes(final Set<A> singularAttributes, final Predicate<Attribute<T, ?>> condition) {
		for (final Attribute<T, ?> a : members.values()) {
			if (condition.apply(a)) {
				@SuppressWarnings("unchecked")
				final
				A sa = (A)a;
				singularAttributes.add(sa);
			}
		}
		return singularAttributes;
	}


	private <V> V strict(final V value, final Predicate<V> predicate, final String errorMsg, final Object ... msgParams) {
		if (predicate.apply(value)) {
			return value;
		}

		throw new IllegalArgumentException(Joiner.on(' ').join("metamodel_" + errorMsg, ":", msgParams));
	}



	private static class TypedAttributePredicate<T> implements Predicate<Attribute<T, ?>> {
		private final Class<? extends Attribute<T, ?>> type;

		TypedAttributePredicate(final Class<? extends Attribute<T, ?>> type) {
			this.type = type;
		}

		@Override
		public boolean apply(final Attribute<T, ?> attribute) {
			return type.isAssignableFrom(attribute.getClass());
		}
	}



	@SuppressWarnings("unchecked")
	@Override
	public <Y> SingularAttribute<? super T, Y> getId(final Class<Y> type) {
		return new SingularAttributeImpl<T, Y>(this, (FieldMetadata<T, Y, ?>)emd.getPrimaryKey());
	}

	@Override
	public <Y> SingularAttribute<T, Y> getDeclaredId(final Class<Y> type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <Y> SingularAttribute<? super T, Y> getVersion(final Class<Y> type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <Y> SingularAttribute<T, Y> getDeclaredVersion(final Class<Y> type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IdentifiableType<? super T> getSupertype() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasSingleIdAttribute() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean hasVersionAttribute() {
		return false;
	}

	@Override
	public Set<SingularAttribute<? super T, ?>> getIdClassAttributes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Type<?> getIdType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public javax.persistence.metamodel.Bindable.BindableType getBindableType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Class<T> getBindableJavaType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		return name;
	}


}
