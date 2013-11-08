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

package org.mestor.em;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.persistence.Cache;
import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceException;
import javax.persistence.PersistenceUnitUtil;
import javax.persistence.Query;
import javax.persistence.SynchronizationType;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.spi.PersistenceUnitInfo;

import org.mestor.context.EntityContext;
import org.mestor.persistence.metamodel.CompositeMetamodel;
import org.mestor.util.CollectionUtils;
import org.mestor.util.SystemProperties;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Maps;

public class EntityManagerFactoryImpl implements EntityManagerFactory {
	private boolean open = true;

	private final String prefix = MestorProperties.PREFIX.key();
	private final Pattern prefixPattern = Pattern.compile("^" + prefix + "\\.");
	private final Predicate<CharSequence> predicate = Predicates.contains(prefixPattern);

//	private final MetamodelImpl metamodel;
	private final CompositeMetamodel metamodel = new CompositeMetamodel();


	private final PersistenceUnitInfo info;
	private final EntityContext context;
	private final Map<String, String> map;
	private final Map<String, Object> properties = new HashMap<>();


	public EntityManagerFactoryImpl(final PersistenceUnitInfo info, final EntityContext context, final Map<String, String> map) {
		this.info = info;
		this.context = context;
		this.map = map;

		properties.putAll(CollectionUtils.merge(
				Maps.filterKeys(System.getenv(), predicate),
				Maps.filterKeys(Maps.fromProperties(SystemProperties.systemProperties()), predicate),
				this.map));
//		metamodel = new MetamodelImpl(info, this, properties);
	}

	@Override
	public synchronized EntityManager createEntityManager() {
		checkOpen();
		return createEntityManager(properties);
	}

	@Override
	public EntityManager createEntityManager(@SuppressWarnings("rawtypes") final Map map) {
		return createEntityManager(SynchronizationType.UNSYNCHRONIZED, map);
	}

	@Override
	public void close() {
		open = false;
	}

	@Override
	public boolean isOpen() {
		return open;
	}


	private void checkOpen() {
		if(!open) {
			throw new IllegalStateException("Entity manager is closed");
		}
	}

	@Override
	public EntityManager createEntityManager(final SynchronizationType synchronizationType) {
		return createEntityManager(SynchronizationType.UNSYNCHRONIZED, Collections.emptyMap());
	}

	@SuppressWarnings({ "unchecked"})
	@Override
	public EntityManager createEntityManager(final SynchronizationType synchronizationType, @SuppressWarnings("rawtypes") final Map map) {
		checkOpen();
		final EntityManager em = new EntityManagerImpl(info, context, this, CollectionUtils.merge(properties, map), null);
		metamodel.add(em.getMetamodel());
		return em;
	}

	@Override
	public CriteriaBuilder getCriteriaBuilder() {
		throw new UnsupportedOperationException("EntityManagerFactory.getCriteriaBuilder() is not supported yet");
	}

	@Override
	public Metamodel getMetamodel() {
		return metamodel;
	}

	@Override
	public Map<String, Object> getProperties() {
		return properties;
	}

	@Override
	public Cache getCache() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PersistenceUnitUtil getPersistenceUnitUtil() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addNamedQuery(final String name, final Query query) {
		// TODO Auto-generated method stub

	}

	@Override
	public <T> T unwrap(final Class<T> cls) {
        if (cls.isAssignableFrom(this.getClass())) {
            // unwraps any proxy to EntityManagerFactory
        	@SuppressWarnings("unchecked")
			final
			T unwrapped = (T) this;
            return unwrapped;
        }

        throw new PersistenceException("Could not unwrap entity manager factory to: " + cls);
	}

	@Override
	public <T> void addNamedEntityGraph(final String graphName, final EntityGraph<T> entityGraph) {
		throw new UnsupportedOperationException("EntityGraphs are not supported right now");
	}

}
