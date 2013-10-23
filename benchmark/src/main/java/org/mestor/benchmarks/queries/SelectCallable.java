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
package org.mestor.benchmarks.queries;

import static org.junit.Assert.fail;

import java.util.Random;
import java.util.concurrent.Callable;

import javax.persistence.EntityManager;

import org.mestor.benchmarks.BenchmarkBase;
import org.mestor.benchmarks.Parameters;

public class SelectCallable extends EntityManagerAware implements Callable<Void> {

	private final static Random RANDOM = new Random();
	
	private final int currentObjectsCount;
	
	public SelectCallable(final BenchmarkBase benchmarkTest, final EntityManager em, final int currentObjectsCount) {
		super(benchmarkTest, em);
		this.currentObjectsCount = currentObjectsCount;
	}

	
	@Override
	public Void call() throws Exception {
		this.select();
		return null;
	}

	void select() {

		/*
		 * em.getEntityManagerFactory().getCache().evictAll(); if(em instanceof
		 * EntityManagerImpl){ final EntityManagerImpl emi =
		 * (EntityManagerImpl)em;
		 * emi.getSession().getIdentityMapAccessor().clearQueryCache(); }
		 */
		final boolean clear = Parameters.CLEAR_SELECTS_CACHE.getBoolean();
		for (int i = 0; i < Parameters.SELECTS_PER_ITERATION.getInt(); i++) {
			if (clear) {
				em.clear();
			}
			final int objectId = RANDOM.nextInt(currentObjectsCount) + 1;
			if (em.find(test.getEntityClass(), (long) objectId) == null) {
				fail("Object not found: " + objectId);
			}
		}
		if (clear) {
			em.clear();
		}
	}
}