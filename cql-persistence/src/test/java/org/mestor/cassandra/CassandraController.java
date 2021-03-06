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

package org.mestor.cassandra;

import java.io.IOException;

import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.service.CassandraDaemon;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.exceptions.NoHostAvailableException;

public class CassandraController {
	private final static String CASSANDRA_DAEMON_KEY = "CASSANDRA_DAEMON";

	public void start() {
		DatabaseDescriptor.createAllDirectories();
		final CassandraDaemon cassandraDaemon = new CassandraDaemon();
		try {
			cassandraDaemon.init(null);
		} catch (final IOException ioe) {
			throw new IllegalStateException("Failed to start Cassandra daemon", ioe);
		}
		cassandraDaemon.start();
		registerCassandraDaemon(cassandraDaemon);
	}

	public void shutdown() {
		final CassandraDaemon cassandraDaemon = getCassandraDaemon();
		if (cassandraDaemon == null) {
			return;
		}
    	cassandraDaemon.deactivate();
    	cassandraDaemon.destroy();
    	cassandraDaemon.stop();
    	removeCassandraDaemon();
	}

	public boolean isRunning() {
		final CassandraDaemon cassandraDaemon = getCassandraDaemon();
		if(cassandraDaemon != null && cassandraDaemon.thriftServer != null && cassandraDaemon.thriftServer.isRunning()) {
			return true; // cassandra is running in-process
		}
		// check whether out-of-process cassandra running on localhost
		try {
			//TODO make host and port configurable via system properties
			final Cluster cluster = Cluster.builder().addContactPoint("localhost").withoutJMXReporting().withoutMetrics().build();
			final Session session = cluster.connect();
	    	session.shutdown();
	    	cluster.shutdown();
	        return true;
		} catch (final NoHostAvailableException e) {
			return false;
		}
	}


	// We store cassandra daemon in system properties. This is a kind of abuse of system properties that are designed
	// to store string-to-string associations. This trick however guarantees that the instance will be available
	// for all tests independently on the class loader.
	private CassandraDaemon getCassandraDaemon() {
		return (CassandraDaemon)System.getProperties().get(CASSANDRA_DAEMON_KEY);
	}

	private void registerCassandraDaemon(final CassandraDaemon cassandraDaemon) {
		System.getProperties().put(CASSANDRA_DAEMON_KEY, cassandraDaemon);
	}

	private void removeCassandraDaemon() {
		System.getProperties().remove(CASSANDRA_DAEMON_KEY);
	}
}
