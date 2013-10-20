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

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.persistence.Embeddable;
import javax.persistence.metamodel.EmbeddableType;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.Metamodel;

import org.mestor.metadata.EntityMetadata;

public class MetamodelImpl implements Metamodel {
	private final Map<Class<?>, EntityMetadata<?>> entityClasses;
	private final Map<Class<?>, ManagedTypeImpl<?>> managedTypes;
	//TODO go over the context's entity metadata and create all ManagedTypes.
	// then iterate over just created Managed types and update reference to ManagedType
	// in all attributes: this is relevant for inheritance.

	public MetamodelImpl(final Collection<EntityMetadata<?>> entityMetadata) {
		this.entityClasses = new LinkedHashMap<>();
		this.managedTypes =  new LinkedHashMap<>();
		for (final EntityMetadata<?> emd : entityMetadata) {
			final Class<?> type = emd.getEntityType();
			entityClasses.put(type, emd);
			managedTypes.put(type, createManagedType(emd));
		}
	}

	@Override
	public <X> EntityType<X> entity(final Class<X> cls) {
		return managedTypeImpl(cls);
	}

	@Override
	public <X> ManagedType<X> managedType(final Class<X> cls) {
		final ManagedType<X> mt = managedTypeImpl(cls);
		return mt instanceof EmbeddableType ? null : mt;
	}

	@SuppressWarnings("unchecked")
	private <X> ManagedTypeImpl<X> managedTypeImpl(final Class<X> cls) {
		return (ManagedTypeImpl<X>)managedTypes.get(cls);
	}

	@Override
	public <X> EmbeddableType<X> embeddable(final Class<X> cls) {
		final ManagedType<X> mt = managedTypeImpl(cls);
		return mt instanceof EmbeddableType ? (EmbeddableType<X>)mt : null;
	}

	@Override
	public Set<ManagedType<?>> getManagedTypes() {
		final Set<ManagedType<?>> managed = new HashSet<>();
		for (final ManagedTypeImpl<?> t : managedTypes.values()) {
			if (!(t instanceof EmbeddableType)) {
				managed.add(t);
			}
		}
		return managed;
	}

	@Override
	public Set<EntityType<?>> getEntities() {
		return new HashSet<EntityType<?>>(managedTypes.values());
	}

	@Override
	public Set<EmbeddableType<?>> getEmbeddables() {
		final Set<EmbeddableType<?>> embeddables = new HashSet<>();
		for (final ManagedTypeImpl<?> t : managedTypes.values()) {
			if (t instanceof EmbeddableType) {
				embeddables.add((EmbeddableType<?>)t);
			}
		}
		return embeddables;
	}


	private <T> ManagedTypeImpl<T> createManagedType(final EntityMetadata<T> emd) {
		return isEmbeddable(emd) ? new EmbeddableTypeImpl<>(emd) : new ManagedTypeImpl<>(emd);
	}

	private <T> boolean isEmbeddable(final EntityMetadata<T> emd) {
		return emd.getEntityType().getAnnotation(Embeddable.class) != null;
	}
}
