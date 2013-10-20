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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.metamodel.EmbeddableType;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.Metamodel;

public class CompositeMetamodel implements Metamodel {
	private final Collection<Metamodel> metamodels = new ArrayList<>();

	public CompositeMetamodel(final Metamodel ... metamodels) {
		this(Arrays.asList(metamodels));
	}


	public CompositeMetamodel(final Collection<Metamodel> metamodels) {
		this.metamodels.addAll(metamodels);
	}

	public void add(final Metamodel ... metamodels) {
		add(Arrays.asList(metamodels));
	}

	public void add(final Collection<Metamodel> metamodels) {
		this.metamodels.addAll(metamodels);
	}

	@Override
	public <X> EntityType<X> entity(final Class<X> cls) {
		for (final Metamodel metamodel : metamodels) {
			final EntityType<X> et = metamodel.entity(cls);
			if (et != null) {
				return et;
			}
		}
		return null;
	}

	@Override
	public <X> ManagedType<X> managedType(final Class<X> cls) {
		for (final Metamodel metamodel : metamodels) {
			final ManagedType<X> mt = metamodel.managedType(cls);
			if (mt != null) {
				return mt;
			}
		}
		return null;
	}

	@Override
	public <X> EmbeddableType<X> embeddable(final Class<X> cls) {
		for (final Metamodel metamodel : metamodels) {
			final EmbeddableType<X> embeddable = metamodel.embeddable(cls);
			if (embeddable != null) {
				return embeddable;
			}
		}
		return null;
	}

	@Override
	public Set<ManagedType<?>> getManagedTypes() {
		final Set<ManagedType<?>> managedTypes = new HashSet<>();
		for (final Metamodel metamodel : metamodels) {
			managedTypes.addAll(metamodel.getManagedTypes());
		}
		return managedTypes;
	}

	@Override
	public Set<EntityType<?>> getEntities() {
		final Set<EntityType<?>> entities = new HashSet<>();
		for (final Metamodel metamodel : metamodels) {
			entities.addAll(metamodel.getEntities());
		}
		return entities;
	}

	@Override
	public Set<EmbeddableType<?>> getEmbeddables() {
		final Set<EmbeddableType<?>> embeddables = new HashSet<>();
		for (final Metamodel metamodel : metamodels) {
			embeddables.addAll(metamodel.getEmbeddables());
		}
		return embeddables;
	}

}
