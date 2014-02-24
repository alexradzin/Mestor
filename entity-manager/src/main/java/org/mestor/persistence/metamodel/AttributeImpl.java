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

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Member;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.ManagedType;

import org.mestor.metadata.FieldMetadata;

abstract class AttributeImpl<E, F> implements Attribute<E, F> {
	protected final ManagedType<E> managedType;
	protected final FieldMetadata<E, F, ?> fmd;
	protected final PersistentAttributeType persistentAttributeType;

	@SuppressWarnings("serial")
	private final static Map<PersistentAttributeType, Class<? extends Annotation>> attributeTypeAnnotations = new EnumMap<PersistentAttributeType, Class<? extends Annotation>>(PersistentAttributeType.class) {{
	     put(PersistentAttributeType.MANY_TO_ONE, ManyToOne.class);
	     put(PersistentAttributeType.ONE_TO_ONE, OneToOne.class);
	     put(PersistentAttributeType.BASIC, Basic.class);
	     put(PersistentAttributeType.EMBEDDED, Embedded.class);
	     put(PersistentAttributeType.MANY_TO_MANY, ManyToMany.class);
	     put(PersistentAttributeType.ONE_TO_MANY, OneToMany.class);
	     put(PersistentAttributeType.ELEMENT_COLLECTION, ElementCollection.class);
	}};




	public AttributeImpl(final ManagedType<E> managedType, final FieldMetadata<E, F, ?> fmd) {
		this.managedType = managedType;
		this.fmd = fmd;
		this.persistentAttributeType = Utils.getAnnotatedCategory(
				attributeTypeAnnotations,
				fmd.getAccessor(),
				PersistentAttributeType.values(),
				PersistentAttributeType.BASIC);
	}


	@Override
	public String getName() {
		return fmd.getName();
	}

	@Override
	public PersistentAttributeType getPersistentAttributeType() {
		return persistentAttributeType;
	}

	@Override
	public ManagedType<E> getDeclaringType() {
		return managedType;
	}

	@Override
	public Class<F> getJavaType() {
		return fmd.getType();
	}

	@Override
	public Member getJavaMember() {
		return fmd.getAccessor();
	}

	@Override
	public boolean isAssociation() {
		final Class<F> clazz = getJavaType();
		final AnnotatedElement accessor = fmd.getAccessor();
		if (accessor.getAnnotation(Entity.class) != null) {
			return true;
		}
		if (Collection.class.isAssignableFrom(clazz) || Map.class.isAssignableFrom(clazz)) {
			if (accessor.getAnnotation(Embedded.class) == null) {
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean isCollection() {
		return Collection.class.isAssignableFrom(getJavaType());
	}


	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static <T, F> Attribute<T, F> createInstance(final FieldMetadata<T, F, ?> fmd, final ManagedType<T> managedType) {
		final Class<F> type = fmd.getType();
		if (List.class.isAssignableFrom(type)) {
			return  new ListAttributeImpl(managedType, fmd);
		}
		if (Set.class.isAssignableFrom(type)) {
			return new SetAttributeImpl(managedType, fmd);
		}
		if (Map.class.isAssignableFrom(type)) {
			return new MapAttributeImpl(managedType, fmd);
		}
		return new SingularAttributeImpl(managedType, fmd);
	}

	FieldMetadata<E, F, ?> getFieldMetadata() {
		return fmd;
	}
}
