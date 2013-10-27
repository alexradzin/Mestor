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

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.Callable;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;

import org.mestor.benchmarks.entities.IdObject;

public abstract class BenchmarkBase {

	private static final String DEFAULT_CASSANDRA_HOST = "172.16.70.37";
	private final Constructor<? extends IdObject> entityConstructor;

	public BenchmarkBase() {
		super();
		mergeSystemProperties();
		try {
			entityConstructor = getEntityClass().getConstructor(new Class<?>[] { long.class });
		} catch ( NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(e);
		}
	}

	private void mergeSystemProperties() {
		try {
			final Properties fileProps = new Properties();
			try(FileInputStream fis = new FileInputStream("benchmark.properties");){
				fileProps.load(fis);
			}
			final Properties mergedProps = new Properties(fileProps);
			mergedProps.putAll(System.getProperties());
			System.setProperties(mergedProps);
		}catch(final IOException e){
			//ignore, use default
		}
	}

	protected <T> T benchmark(final FileWriter fw, final Callable<T> callable) throws Exception {
		final long start = System.nanoTime();
		try {
			return callable.call();
		} finally {
			final long end = System.nanoTime();
			fw.append(String.valueOf(end - start));
			fw.append('\n');
			fw.flush();
		}
	}

	public IdObject createInstance(final long id) {
		try {
			return entityConstructor.newInstance(new Object[] { id });
		} catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	private EntityManager getEntityManager() {
		// final String persistenceXmlLocation = "persistence.xml";
		/*
		 * if (System.getProperty("javax.persistence.jdbc.driver") == null) {
		 * System.setProperty("javax.persistence.jdbc.driver",
		 * "org.apache.derby.jdbc.ClientDriver"); } if
		 * (System.getProperty("javax.persistence.jdbc.url") == null) {
		 * System.setProperty("javax.persistence.jdbc.url",
		 * "jdbc:derby://172.16.70.37//mnt/db/benchmarks;create=true;user=app;"
		 * ); }
		 */

		if (System.getProperty("org.mestor.cassandra.hosts") == null) {
			System.setProperty("org.mestor.cassandra.hosts", DEFAULT_CASSANDRA_HOST);
		}
		return Persistence.createEntityManagerFactory(Parameters.PERSISTENCE_UNIT.getString()).createEntityManager();
	}

	@SuppressWarnings("unchecked")
	public Class<? extends IdObject> getEntityClass() {
		return (Class<? extends IdObject>) Parameters.ENTITY_CLASS.getClazz();
	}

	protected void doBenchmarks() throws Exception {
		final String outDirectory = Parameters.OUTPUT_DIR.getString();

		final EntityManager em = getEntityManager();
		try {
			beforeBenchmarks(em);
	
			System.out.print("Start: ");
			System.out.println(new Date());
			try (final FileWriter insertsFw = new FileWriter(outDirectory + "inserts");
					final FileWriter selectsFw = new FileWriter(outDirectory + "selects");
					final FileWriter deletesFw = new FileWriter(outDirectory + "deletes");) {
	
				benchmarks(em, insertsFw, selectsFw, deletesFw);
			}
			System.out.print("Finish: ");
			System.out.println(new Date());
		} finally {
			em.close();
		}
	}

	protected void beforeBenchmarks(final EntityManager em) throws Exception {

	}

	protected abstract void benchmarks(EntityManager em, FileWriter insertsFw, FileWriter selectsFw, FileWriter deletesFw) throws Exception;

}