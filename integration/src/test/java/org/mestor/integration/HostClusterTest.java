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
package org.mestor.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;

import org.junit.Test;
import org.mestor.em.MestorProperties;
import org.mestor.entities.integration.HostCluster;

public class HostClusterTest {

	// FIXME: duplicate EntityManagerTest.getEntityManager
	private EntityManager getEntityManager(final String persistenceXmlLocation, final String puName) {
		System.setProperty(MestorProperties.PERSISTENCE_XML.key(), persistenceXmlLocation);
		return Persistence.createEntityManagerFactory(puName).createEntityManager();
	}

	@Test
	public void testHostCluster() {
		final Long[] ids = new Long[] { 1l, 10l, 21l, 5l };
		final SortedSet<Long> sortedIds = new TreeSet<>(Arrays.asList(ids));
		final EntityManager em = getEntityManager("integration.xml", "host_cluster");
		for (final Long id : ids) {
			createHostCluster(em, id);
		}
		testSelectSorted(sortedIds, em);
		final TypedQuery<Long> q = em.createNamedQuery("countHostClusters", Long.class);
		final List<Long> resultList = q.getResultList();
		assertEquals(1, resultList.size());
		final Long result = resultList.iterator().next();
		assertEquals(ids.length, result.intValue());
	}

	private void testSelectSorted(final SortedSet<Long> sortedIds, final EntityManager em) {
		final TypedQuery<HostCluster> q = em.createNamedQuery("findAllHostClusters", HostCluster.class);
		final List<HostCluster> resultList = q.getResultList();
		final Iterator<HostCluster> resIt = resultList.iterator();
		final Iterator<Long> idsIt = sortedIds.iterator();
		while(resIt.hasNext()) {
			final HostCluster hc = resIt.next();
			final Long id = idsIt.next();
			assertEquals(id, hc.getId());
		}
		assertFalse(idsIt.hasNext());
	}

	private void createHostCluster(final EntityManager em, final Long id) {
		final HostCluster hc = new HostCluster();
		hc.setId(id);
		hc.setCreatedAt(System.nanoTime());
		hc.setName(id.toString());
		em.persist(hc);
	}
}
