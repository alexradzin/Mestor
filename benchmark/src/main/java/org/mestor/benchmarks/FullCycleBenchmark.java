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
import org.mestor.benchmarks.queries.DeleteCallable;
import org.mestor.benchmarks.queries.SelectCallable;

public class FullCycleBenchmark extends BenchmarkBase {

	public static void main(final String[] args) {
		try {
			new FullCycleBenchmark().fullCycleBenchmark();
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	public void fullCycleBenchmark() throws Exception {
		doBenchmarks();
	}

	@Override
	protected void beforeBenchmarks(final EntityManager em) throws Exception {
		warmUp(em);
	}

	@Override
	protected void benchmarks(final EntityManager em, final FileWriter insertsFw, final FileWriter selectsFw, final FileWriter deletesFw) throws Exception {
		final int insertsPerIteration = Parameters.INSERTS_PER_ITERATION.getInt();
		final int iterationsCount = Parameters.ITERATIONS_COUNT.getInt();

		for (int i = 0; i < iterationsCount; i++) {
			final int start = i * insertsPerIteration;
			final int end = start + insertsPerIteration;
			benchmark(insertsFw, new CreateCallable(this, em, start, end));
			benchmark(selectsFw, new SelectCallable(this, em, end));
		}
		for (int i = 0; i < iterationsCount; i++) {
			final int start = i * insertsPerIteration;
			final int end = start + insertsPerIteration;
			benchmark(deletesFw, new DeleteCallable(this, em, start, end));
		}
	}

	private void warmUp(final EntityManager em) throws Exception {
		new CreateCallable(this, em, 0, Parameters.INSERTS_PER_ITERATION.getInt()).call();
		new SelectCallable(this, em, Parameters.SELECTS_PER_ITERATION.getInt()).call();
		new DeleteCallable(this, em, 0, Parameters.INSERTS_PER_ITERATION.getInt()).call();
	}

}
