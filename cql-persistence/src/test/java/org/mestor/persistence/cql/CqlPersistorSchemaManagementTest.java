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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.cassandra.locator.NetworkTopologyStrategy;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mestor.cassandra.CassandraAwareTestRunner;
import org.mestor.context.EntityContext;
import org.mestor.context.Persistor;
import org.mestor.metadata.EntityMetadata;
import org.mestor.metadata.FieldMetadata;
import org.mockito.Mockito;

import com.datastax.driver.core.ColumnMetadata;
import com.datastax.driver.core.TableMetadata;
import com.datastax.driver.core.exceptions.SyntaxError;
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
	public void testCreateSchemaWithReplication() {
		Map<String, Object> replication = new HashMap<String, Object>() {{
			put("class", "SimpleStrategy");
			put("replication_factor", 1);
		}};
		testCreateSchema("test1", Collections.<String, Object>singletonMap("replication", replication), true);
	}


	/**
	 * Create persistor with explicitly defined default keyspace creation properties as string.
	 * This simulates a "real world" scenario when these properties are configured in {@code persistence.xml} 
	 * and sent via {@code EntityManager}.  
	 * @throws IOException
	 */
	@Test
	public void testCreateSchemaWithReplicationAsString() throws IOException {
		final String schemaName = "test2";
		Persistor persistor = createAndConnect(Collections.<String, Object>singletonMap("org.mestor.cassandra.keyspace.properties", "replication = {'class':'NetworkTopologyStrategy'}"));
		assertFalse(Iterables.contains(persistor.getSchemaNames(), schemaName));
		persistor.createSchema(schemaName, null);
		assertTrue(Iterables.contains(persistor.getSchemaNames(), schemaName));
		assertEquals(NetworkTopologyStrategy.class.getName(), ((CqlPersistor)persistor).getCluster().getMetadata().getKeyspace(schemaName).getReplication().get("class"));
		persistor.dropSchema(schemaName);
	}
	
	
	
	/**
	 * Creates schema using default schema properties (WITH clause of corresponding CQL statement). 
	 * Default properties are used when {@code null} is passed as value of properties.
	 */
	@Test
	public void testCreateDefaultSchema() {
		testCreateSchema("test1", null, true);
	}
	
	/**
	 * This test sends empty properties when creating keyspace. This is illegal, so exception is expected.
	 */
	@Test(expected = SyntaxError.class)
	public void testCreateSchemaWithout() {
		testCreateSchema("test1", Collections.<String, Object>emptyMap(), true);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public <E> void testCreateTable() {
		final String schemaName = "test1";
		
		try {
			testCreateSchema(schemaName, null, false);
			EntityMetadata<Person> emd = new EntityMetadata<>(Person.class);
			emd.setEntityName(Person.class.getSimpleName());
			emd.setTableName("People");
			emd.setSchemaName(schemaName);

			
			FieldMetadata<Person, Integer> pk = new FieldMetadata<>(Person.class, Integer.class, "id");
			pk.setColumn("identifier");
			pk.setKey(true);
			
			emd.setPrimaryKey(pk);
			
			
			FieldMetadata<Person, String> nameField = new FieldMetadata<>(Person.class, String.class, "name");
			nameField.setColumn("full_name");
			FieldMetadata<Person, Integer> ageField = new FieldMetadata<>(Person.class, Integer.class, "age");
			ageField.setColumn("age");
			
			Map<String, FieldMetadata<Person, Object>> fields = new LinkedHashMap<>();
			for (FieldMetadata<Person, Object> field : new FieldMetadata[] {pk, nameField, ageField}) {
				fields.put(field.getColumn(), field);
			}
			
			emd.setFields(fields);
			testCreateTable(emd, null);
		} finally {
			persistor.dropSchema(schemaName);
		}
	}
	
	
	private Persistor createAndConnect(Map<String, Object> props) throws IOException {
		EntityContext ctx = Mockito.mock(EntityContext.class);
		doReturn(props).when(ctx).getProperties();
		return new CqlPersistor(ctx);
	}

	
	private void testCreateSchema(String schemaName, Map<String, Object> with, boolean drop) {
		assertFalse(Iterables.contains(persistor.getSchemaNames(), schemaName));
		persistor.createSchema(schemaName, with);
		assertTrue(Iterables.contains(persistor.getSchemaNames(), schemaName));
		if (drop) {
			persistor.dropSchema(schemaName);
		}
	}

	
	private <E> void testCreateTable(EntityMetadata<E> entityMetadata, Map<String, Object> properties) {
		String keyspace = entityMetadata.getSchemaName();
		String table = entityMetadata.getTableName();
		
		assertNull(((CqlPersistor)persistor).getCluster().getMetadata().getKeyspace(keyspace).getTable(table));
		
		persistor.createTable(entityMetadata, properties);

		// search for just created table. Since (at least in current configuration) cassandra is case-insensitive 
		// and creates tables using lower case and we do not know whether this is configurable 
		// we have to search for table by iterating over all tables to make test more not sensitive to the possible 
		// future configuration changes. 
		TableMetadata tmd = null;
		for (TableMetadata t : ((CqlPersistor)persistor).getCluster().getMetadata().getKeyspace(keyspace).getTables()) {
			if (t.getName().equalsIgnoreCase(table)) {
				tmd = t;
				break;
			}
		}
		
		assertNotNull(tmd);
		assertMatches(entityMetadata, properties, tmd);
	}
	
	private <E> void assertMatches(EntityMetadata<E> emd, Map<String, Object> properties, TableMetadata tmd) {
		assertNotNull(tmd);
		
		assertEquals(emd.getTableName().toLowerCase(), tmd.getName().toLowerCase());
		assertEquals(emd.getSchemaName(), tmd.getKeyspace().getName());
		
		for (FieldMetadata<E, ?> fmd : emd.getFields().values()) {
			ColumnMetadata cmd = tmd.getColumn(fmd.getColumn());
			assertNotNull(cmd);
			assertEquals(fmd.getColumn(), cmd.getName());
			assertEquals(fmd.getType(), cmd.getType().asJavaClass());
		}
		 
	}
}
