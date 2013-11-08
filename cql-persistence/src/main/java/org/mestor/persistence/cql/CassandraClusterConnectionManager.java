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

package org.mestor.persistence.cql;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;

class CassandraClusterConnectionManager {
	private static CassandraClusterConnectionManager connectionManager = new CassandraClusterConnectionManager();
	private final Map<ConnectionSpec, ClusterConnection> connections = new HashMap<>();


	static CassandraClusterConnectionManager getInstance() {
		return connectionManager;
	}

	ClusterConnection getConnection(final Integer port, final String[] hosts) {
		final ConnectionSpec connectionSpec = new ConnectionSpec(port, hosts);
		ClusterConnection connection = connections.get(connectionSpec);
		if (connection == null) {
			final Cluster.Builder clusterBuilder = Cluster.builder();
			if (port != null) {
				clusterBuilder.withPort(port);
			}

			final Cluster cluster = clusterBuilder.addContactPoints(hosts).build();
			final Session session = cluster.connect();

			connection = new ClusterConnection(connectionSpec, cluster, session);
			connections.put(connectionSpec, connection);
		}
		return connection;
	}

	void closeConnection(final ClusterConnection connection) {
		connection.shutdown();
		connections.remove(connection.connectionSpec);
	}

	static class ConnectionSpec {
		private final Integer port;
		private final String[] hosts;

		ConnectionSpec(final Integer port, final String[] hosts) {
			this.port = port;
			this.hosts = hosts;
		}

		@Override
		public int hashCode() {
			return 31 * Objects.hash(port) + Objects.hash(hosts);
		}

		@Override
		public boolean equals(final Object obj) {
			if (obj == null || !(obj instanceof ConnectionSpec)) {
				return false;
			}
			final ConnectionSpec other = (ConnectionSpec)obj;
			return Objects.equals(port, other.port) && Objects.deepEquals(hosts, other.hosts);
		}
	}


	static class ClusterConnection {
		private final ConnectionSpec connectionSpec;
		private final Cluster cluster;
		private final Session session;

		ClusterConnection(final ConnectionSpec connectionSpec, final Cluster cluster, final Session session) {
			this.connectionSpec = connectionSpec;
			this.cluster = cluster;
			this.session = session;
		}

		public Cluster getCluster() {
			return cluster;
		}
		public Session getSession() {
			return session;
		}

		void shutdown() {
			cluster.shutdown();
			session.shutdown();
		}
	}


}
