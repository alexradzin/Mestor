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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mestor.cassandra.CassandraAwareTestRunner;
import org.mestor.context.EntityContext;
import org.mestor.context.Persistor;
import org.mockito.Mockito;

import com.google.common.collect.Iterables;

@RunWith(CassandraAwareTestRunner.class)
public class CqlPersistorSchemaManagementTest {
	private final Persistor persistor;
	
	public CqlPersistorSchemaManagementTest() throws IOException {
		persistor = createAndConnect(Collections.<String, Object>emptyMap());
	}
	
	
	@Test
	public void testGetSchemaNames() {
		Iterable<String> schemas = persistor.getSchemaNames();
		assertNotNull(schemas);
		
		for (String systemSchema : new String[] {"system_auth", "system_traces", "system"}) {
			assertTrue(Iterables.contains(schemas, systemSchema));
		}
	}

	
	@Test
	public void testCreateSchema() {
		String schemaName = "test1";
		assertFalse(Iterables.contains(persistor.getSchemaNames(), schemaName));
		persistor.createSchema(schemaName, Collections.<String, Object>singletonMap("replication", "{'class':'SimpleStrategy', 'replication_factor':1}"));
		assertTrue(Iterables.contains(persistor.getSchemaNames(), schemaName));
		persistor.dropSchema(schemaName);
	}
	
	
	private Persistor createAndConnect(Map<String, Object> props) throws IOException {
		EntityContext ctx = Mockito.mock(EntityContext.class);
		doReturn(props).when(ctx).getProperties();
		return new CqlPersistor(ctx);
	}

}
