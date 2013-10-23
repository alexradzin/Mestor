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

import java.util.concurrent.Callable;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.mestor.benchmarks.BenchmarkBase;
import org.mestor.benchmarks.entities.IdObject;

public class DeleteCallable extends EntityManagerAware implements Callable<Void> {

	private final int start;
	private final int end;
	
	public DeleteCallable(final BenchmarkBase benchmarkTest, final EntityManager em, final int start, final int end) {
		super(benchmarkTest, em);
		this.start = start;
		this.end = end;
	}

	@Override
	public Void call() throws Exception {
		this.delete();
		return null;
	}
	
	void delete() {
		final EntityTransaction transaction = em.getTransaction();
		transaction.begin();
		for (int i = start; i < end; i++) {
			final IdObject entity = em.find(test.getEntityClass(), (long) i + 1);
			em.remove(entity);
		}
		transaction.commit();
	}
}