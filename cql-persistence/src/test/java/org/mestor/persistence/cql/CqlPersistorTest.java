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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import org.junit.Test;
import org.mestor.context.EntityContext;
import org.mockito.Mockito;

import com.datastax.driver.core.exceptions.NoHostAvailableException;

public class CqlPersistorTest {

	@Test(expected = IllegalArgumentException.class)
	public void testCreateNullContext() throws IOException {
		new CqlPersistor(null);
	}

	/**
	 * This test creates instance of {@link CqlPersistor} that automatically connects to
	 * Cassandra using default configuration, i.e. to Cassandra running on localhost.
	 * The test result depends on the fact whether Cassandra is running on localhost.
	 * If Cassandra is running the test succeeds otherwise it fails with {@link NoHostAvailableException}.
	 * To make this test stable it handles both cases safely.
	 */
	@Test
	public void testCreateAndConnectNullProperties() {
		safelyCreateAndConnect(null);
	}

	@Test
	public void testCreateAndConnectNullHostAndPort() {
		safelyCreateAndConnect(Collections.<String, Object>emptyMap());
	}

	@Test
	public void testCreateAndConnectLocalhost() {
		safelyCreateAndConnect(Collections.<String, Object>singletonMap(CqlPersistorProperties.CASSANDRA_HOSTS.property(), "localhost"));
	}


	@Test(expected = IllegalArgumentException.class)
	public void testCreateAndConnectWrongHost() {
		safelyCreateAndConnect(Collections.<String, Object>singletonMap(CqlPersistorProperties.CASSANDRA_HOSTS.property(), "doesnotexist"));
	}

	@Test(expected = IOException.class)
	public void testCreateAndConnectToLocalhostWrongPort() throws IOException {
		createAndConnect(Collections.<String, Object>singletonMap(CqlPersistorProperties.CASSANDRA_PORT.property(), 12345));
	}

	private void safelyCreateAndConnect(final Map<String, Object> props) {
		try {
			createAndConnect(props);
			// if we are here cassandra is not running on localhost
		} catch (final IOException e) {
			// check that this is indeed exception thrown when cassandra is not running on localhost
			assertEquals(NoHostAvailableException.class, e.getCause().getClass());
		}
	}

	private void createAndConnect(final Map<String, Object> props) throws IOException {
		final EntityContext ctx = Mockito.mock(EntityContext.class);
		doReturn(props).when(ctx).getProperties();
		@SuppressWarnings("unused")
		final
		CqlPersistor cqlPersistor = new CqlPersistor(ctx);
	}

}
