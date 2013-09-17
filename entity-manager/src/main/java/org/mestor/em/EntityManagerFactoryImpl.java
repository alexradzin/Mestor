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

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceUnitInfo;

import org.mestor.util.CollectionUtils;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Maps;

public class EntityManagerFactoryImpl implements EntityManagerFactory {
	private boolean open = true;
	
	private final String prefix = MestorProperties.PREFIX.key();
	private final Pattern prefixPattern = Pattern.compile("^" + prefix + "\\.");
	private final Predicate<CharSequence> predicate = Predicates.contains(prefixPattern);
	
	
	private final PersistenceUnitInfo info;
	private final Map<String, String> map;
	
	
	public EntityManagerFactoryImpl(PersistenceUnitInfo info, Map<String, String> map) {
		this.info = info;
		this.map = map;
	}

	@Override
	public EntityManager createEntityManager() {
		checkOpen();
		return createEntityManager(
				CollectionUtils.merge(
						Maps.filterKeys(System.getenv(), predicate), 
						Maps.filterKeys(Maps.fromProperties(System.getProperties()), predicate), 
						map));
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

}
