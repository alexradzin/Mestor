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
package org.mestor.benchmarks;

import java.io.FileWriter;

import javax.persistence.EntityManager;

import org.mestor.benchmarks.queries.CreateCallable;
import org.mestor.benchmarks.queries.SelectCallable;

public class CacheBenchmark extends BenchmarkBase {

	public static void main(final String[] args) throws Exception {
		new CacheBenchmark().cacheBenchmark();
	}

	public void cacheBenchmark() throws Exception {
		doBenchmarks();
	}

	@Override
	protected void benchmarks(final EntityManager em, final FileWriter insertsFw, final FileWriter selectsFw, final FileWriter deletesFw) throws Exception {
		final int iterationsCount = Parameters.ITERATIONS_COUNT.getInt();
		final int objectsCount = Parameters.INSERTS_PER_ITERATION.getInt();
		benchmark(insertsFw, new CreateCallable(this, em, 0, objectsCount));
		for (int i = 0; i < iterationsCount; i++) {
			benchmark(selectsFw, new SelectCallable(this, em, objectsCount));
		}
	}

}
