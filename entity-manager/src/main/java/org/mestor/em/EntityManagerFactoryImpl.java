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

import java.util.Map;
import java.util.regex.Pattern;

import javax.persistence.Cache;
import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnitUtil;
import javax.persistence.Query;
import javax.persistence.SynchronizationType;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.spi.PersistenceUnitInfo;

import org.mestor.util.CollectionUtils;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Maps;

public class EntityManagerFactoryImpl implements EntityManagerFactory {
	private boolean open = true;
	
	
	private final PersistenceUnitInfo info;
	private final Map<String, String> map;
	
	
	public EntityManagerFactoryImpl(PersistenceUnitInfo info, Map<String, String> map) {
		this.info = info;
		this.map = map;
	}

	@Override
	public EntityManager createEntityManager() {
		checkOpen();
		return createEntityManager(map);
	}

	@SuppressWarnings("unchecked")
	@Override
	public EntityManager createEntityManager(@SuppressWarnings("rawtypes") Map map) {
		checkOpen();
		return new EntityManagerImpl(info, map);
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
	public EntityManager createEntityManager(SynchronizationType synchronizationType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public EntityManager createEntityManager(SynchronizationType synchronizationType, @SuppressWarnings("rawtypes") Map map) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CriteriaBuilder getCriteriaBuilder() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Metamodel getMetamodel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Object> getProperties() {
		// TODO Auto-generated method stub
		return null;
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
	public void addNamedQuery(String name, Query query) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public <T> T unwrap(Class<T> cls) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> void addNamedEntityGraph(String graphName, EntityGraph<T> entityGraph) {
		// TODO Auto-generated method stub
		
	}

}
