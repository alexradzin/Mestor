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
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doReturn;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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
import org.mestor.metadata.IndexMetadata;
import org.mockito.Mockito;

import com.datastax.driver.core.ColumnMetadata;
import com.datastax.driver.core.TableMetadata;
import com.datastax.driver.core.exceptions.AlreadyExistsException;
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
	
	
	/**
	 * Creates table with only one {@code int} field that is a primary key
	 */
	@Test
	public void testCreateTableOneIntFieldPK() {
		final String schemaName = "test1";
		try {
			FieldMetadata<Person, Integer> pk = new FieldMetadata<>(Person.class, Integer.class, "id");
			pk.setColumn("identifier");
			pk.setKey(true);
			testEditTable(createPersonMetadata(schemaName, "People", pk, pk), null, true);
		} finally {
			persistor.dropSchema(schemaName);
		}
	}
	


	/**
	 * Creates table with one field that is not a primary key. 
	 * The test is expected to fail because any table must have primary key. 
	 */
	@Test(expected=SyntaxError.class)
	public void testCreateTableOneIntFieldNoPK() {
		final String schemaName = "test1";
		try {
			FieldMetadata<Person, Integer> pk = new FieldMetadata<>(Person.class, Integer.class, "id");
			pk.setColumn("identifier");
			testEditTable(createPersonMetadata(schemaName, "People", null, pk), null, true);
		} finally {
			persistor.dropSchema(schemaName);
		}
	}
	

	@Test(expected=SyntaxError.class)
	public void testCreateTableNoFields() {
		final String schemaName = "test1";
		try {
			testEditTable(createPersonMetadata(schemaName, "People", null), null, true);
		} finally {
			persistor.dropSchema(schemaName);
		}
	}
	
	
	@Test
	public void testCreateDuplicateTable() {
		final String schemaName = "test1";
		try {
			FieldMetadata<Person, Integer> pk = new FieldMetadata<>(Person.class, Integer.class, "id");
			pk.setColumn("identifier");
			pk.setKey(true);
			
			EntityMetadata<Person> emd = createPersonMetadata(schemaName, "People", pk, pk);
			testEditTable(emd, null, true);
			// try again
			try {
				persistor.createTable(emd, null);
				fail("Attempt to create table again must fail");
			} catch (AlreadyExistsException e) {
				// it is OK. The exception is expected here.
			}
		} finally {
			persistor.dropSchema(schemaName);
		}
	}
	
	//TODO: add test that drops table
	//TODO: add test that drops column
	//TODO: add test that drops index
	//TODO: add tests that validate schema
	
	/**
	 * Creates table with 2 indexes, then adds yet another field with index (alters table), 
	 * then drops table.
	 */
	@SuppressWarnings("unchecked")
	@Test
	public <E> void testCreateAlterTableAndIndex() {
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
			nameField.setColumn("first_name");
			
			FieldMetadata<Person, Integer> ageField = new FieldMetadata<>(Person.class, Integer.class, "age");
			ageField.setColumn("age");
			
			Map<String, FieldMetadata<Person, Object>> fields = new LinkedHashMap<>();
			for (FieldMetadata<Person, Object> field : new FieldMetadata[] {pk, nameField, ageField}) {
				fields.put(field.getColumn(), field);
			}
			
			emd.setFields(fields);
			
			
			emd.setIndexes(Arrays.asList(
					new IndexMetadata<Person>(Person.class, "name_index", nameField),
					new IndexMetadata<Person>(Person.class, "age_index", ageField)
			));
			
			
			testEditTable(emd, null, true);
			
			
			// now add column, i.e. alter table.
			FieldMetadata<Person, String> lastNameField = new FieldMetadata<>(Person.class, String.class, "lastName");
			lastNameField.setColumn("last_name");
			for (FieldMetadata<Person, Object> field : new FieldMetadata[] {lastNameField}) {
				fields.put(lastNameField.getColumn(), field);
			}

			emd.setIndexes(Arrays.asList(
					new IndexMetadata<Person>(Person.class, "name_index", nameField),
					new IndexMetadata<Person>(Person.class, "last_name_index", lastNameField),
					new IndexMetadata<Person>(Person.class, "age_index", ageField)
			));
			
			testEditTable(emd, null, false);

			// drop table and check that it is indeed dropped.
			final String keyspace = emd.getSchemaName();
			final String table = emd.getTableName();
			persistor.dropTable(keyspace, table);
			assertNull(findTableMetadata(keyspace, table));
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

	/**
	 * 
	 * @param entityMetadata
	 * @param properties
	 * @param create true for create, false for update
	 */
	private <E> void testEditTable(EntityMetadata<E> entityMetadata, Map<String, Object> properties, boolean create) {
		final String keyspace = entityMetadata.getSchemaName();
		final String table = entityMetadata.getTableName();

		TableMetadata existingTable = findTableMetadata(keyspace, table);
		if (create) {
			assertNull(existingTable);
			persistor.createTable(entityMetadata, properties);
		} else {
			assertNotNull(existingTable);
			persistor.updateTable(entityMetadata, properties);
		}

		TableMetadata tmd = findTableMetadata(keyspace, table);
		assertNotNull(tmd);
		assertMatches(entityMetadata, properties, tmd);
	}

	
	
	private TableMetadata findTableMetadata(String keyspace, String table) {
		// search for just created table. Since (at least in current configuration) cassandra is case-insensitive 
		// and creates tables using lower case and we do not know whether this is configurable 
		// we have to search for table by iterating over all tables to make test more not sensitive to the possible 
		// future configuration changes. 
		for (TableMetadata t : ((CqlPersistor)persistor).getCluster().getMetadata().getKeyspace(keyspace).getTables()) {
			if (t.getName().equalsIgnoreCase(table)) {
				return t;
			}
		}
		return null;
	}
	
	private <E> void assertMatches(EntityMetadata<E> emd, Map<String, Object> properties, TableMetadata tmd) {
		assertNotNull(tmd);
		
		assertEquals(emd.getTableName().toLowerCase(), tmd.getName().toLowerCase());
		assertEquals(emd.getSchemaName(), tmd.getKeyspace().getName());
		
		Collection<String> actualIndexedColumns = new HashSet<>();
		
		for (FieldMetadata<E, ?> fmd : emd.getFields().values()) {
			ColumnMetadata cmd = tmd.getColumn(fmd.getColumn());
			assertNotNull(cmd);
			assertEquals(fmd.getColumn(), cmd.getName());
			assertEquals(fmd.getType(), cmd.getType().asJavaClass());
			
			if(cmd.getIndex() != null) {
				actualIndexedColumns.add(cmd.getIndex().getIndexedColumn().getName());
			}
		}
		

		Map<String, String> expectedIndexes = new HashMap<>();
		
		for (IndexMetadata<E> imd : emd.getIndexes()) {
			FieldMetadata<E, ? extends Object>[] indexFields = imd.getField();
			assertEquals(1, indexFields.length);
			expectedIndexes.put(imd.getName(), indexFields[0].getColumn());
		}

		assertEquals("Unexpected list of indexed columns", new HashSet<String>(expectedIndexes.values()), new HashSet<String>(actualIndexedColumns));
	}
	
	
	@SuppressWarnings({"unchecked", "rawtypes"})
	private EntityMetadata<Person> createPersonMetadata(String schemaName, String tableName, FieldMetadata pk, FieldMetadata ... fieldsMetadata) {
		testCreateSchema(schemaName, null, false);
		EntityMetadata<Person> emd = new EntityMetadata<>(Person.class);
		emd.setEntityName(Person.class.getSimpleName());
		emd.setTableName(tableName);
		emd.setSchemaName(schemaName);
		
		if (pk != null) {
			pk.setKey(true);
		}
		emd.setPrimaryKey(pk);
		
		Map<String, FieldMetadata<Person, Object>> fields = new LinkedHashMap<>();
		if (pk != null) {
			fields.put(pk.getColumn(), pk);
		}
		for (FieldMetadata<Person, Object> field : fieldsMetadata) {
			fields.put(field.getColumn(), field);
		}
		emd.setFields(fields);
		return emd;
	}
	
}
