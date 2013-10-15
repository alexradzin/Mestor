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
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import org.apache.cassandra.locator.NetworkTopologyStrategy;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mestor.cassandra.CassandraAwareTestRunner;
import org.mestor.context.Persistor;
import org.mestor.entities.Person;
import org.mestor.metadata.EntityMetadata;
import org.mestor.metadata.FieldMetadata;
import org.mestor.metadata.IndexMetadata;
import org.mestor.persistence.cql.CqlPersistorProperties.ThrowOnViolation;

import com.datastax.driver.core.TableMetadata;
import com.datastax.driver.core.exceptions.AlreadyExistsException;
import com.datastax.driver.core.exceptions.InvalidQueryException;
import com.datastax.driver.core.exceptions.SyntaxError;
import com.google.common.collect.Iterables;

@RunWith(CassandraAwareTestRunner.class)
public class CqlPersistorSchemaManagementTest {
	private final Persistor persistor;
	private final CqlPersistorTestHelper helper;
	
	
	
	
	public CqlPersistorSchemaManagementTest() throws IOException {
		helper = new CqlPersistorTestHelper();
		persistor = helper.getPersistor();
		doReturn(helper.getPersistor()).when(helper.ctx).getPersistor();
	}
	
	
	@Test
	public void testGetSchemaNames() {
		final Iterable<String> schemas = persistor.getSchemaNames();
		assertNotNull(schemas);
		
		for (final String systemSchema : new String[] {"system_traces", "system"}) {
			assertTrue(Iterables.contains(schemas, systemSchema));
		}
	}

	
	@Test
	public void testCreateSchemaWithReplication() {
		final Map<String, Object> replication = new HashMap<String, Object>() {{
			put("class", "SimpleStrategy");
			put("replication_factor", 1);
		}};
		helper.testCreateSchema("test1", Collections.<String, Object>singletonMap("replication", replication), true);
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
		final Persistor persistor = helper.createAndConnect(Collections.<String, Object>singletonMap("org.mestor.cassandra.keyspace.properties", "replication = {'class':'NetworkTopologyStrategy'}"));
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
		helper.testCreateSchema("test1", null, true);
	}
	
	/**
	 * This test sends empty properties when creating keyspace. This is illegal, so exception is expected.
	 */
	@Test(expected = SyntaxError.class)
	public void testCreateSchemaWithout() {
		helper.testCreateSchema("test1", Collections.<String, Object>emptyMap(), true);
	}
	
	
	/**
	 * Creates table with only one {@code int} field that is a primary key
	 */
	@Test
	public void testCreateTableOneIntFieldPK() {
		final String schemaName = "test1";
		try {
			helper.testCreateSchema(schemaName, null, false);
			final FieldMetadata<Person, Integer, Integer> pk = helper.createFieldMetadata(Person.class, Integer.class, "id");
			pk.setColumn("identifier");
			pk.setKey(true);
			helper.testEditTable(helper.createMetadata(Person.class, schemaName, "People", pk, pk), null, true);
		} finally {
			persistor.dropSchema(schemaName);
		}
	}

	@Test
	public void testCreateTableCompositePK() {
		final String schemaName = "test1";
		try {
			helper.testCreateSchema(schemaName, null, false);
			
			final FieldMetadata<Person, Integer, Integer> id = helper.createFieldMetadata(Person.class, Integer.class, "id", "identifier", true);
			final FieldMetadata<Person, String, String> name = helper.createFieldMetadata(Person.class, String.class, "name", "first_name", true);
			
			final EntityMetadata<Person> emd = new EntityMetadata<>(Person.class);
			emd.setEntityName("Person");
			emd.setTableName("people");
			emd.setSchemaName(schemaName);
			
			emd.addField(id);
			emd.addField(name);

			helper.testEditTable(emd, null, true);
		} finally {
			persistor.dropSchema(schemaName);
		}
	}
	
	@Test(expected=InvalidQueryException.class)
	public void testCreateTableWithTwoFieldsMappedToOneColumn() {
		final String schemaName = "test1";
		try {
			helper.testCreateSchema(schemaName, null, false);
			
			final FieldMetadata<Person, Integer, Integer> pk = helper.createFieldMetadata(Person.class, int.class, "id", "identifier", true);
			
			@SuppressWarnings("unchecked")
			final
			FieldMetadata<Person, Object, Object>[] fields = new FieldMetadata[] {
					pk,
					helper.createFieldMetadata(Person.class, String.class, "name", "full_name", false),
					helper.createFieldMetadata(Person.class, String.class, "last_name", "full_name", false),
			};
			
			helper.testEditTable(helper.createMetadata(Person.class, schemaName, "people", pk, fields), null, true);
		} finally {
			persistor.dropSchema(schemaName);
		}
	}
	
	@Test
	public void testCreateTableWithOneFieldMappedToTwoColumn() {
		final String schemaName = "test1";
		try {
			helper.testCreateSchema(schemaName, null, false);
			
			final FieldMetadata<Person, Integer, Integer> pk = helper.createFieldMetadata(Person.class, int.class, "id", "identifier", true);
			
			@SuppressWarnings("unchecked")
			final
			FieldMetadata<Person, Object, Object>[] fields = new FieldMetadata[] {
					pk,
					helper.createFieldMetadata(Person.class, String.class, "name", "given_name", false),
					helper.createFieldMetadata(Person.class, String.class, "name", "first_name", false),
			};
			
			helper.testEditTable(helper.createMetadata(Person.class, schemaName, "people", pk, fields), null, true);
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
			helper.testCreateSchema(schemaName, null, false);
			final FieldMetadata<Person, Integer, Integer> pk = helper.createFieldMetadata(Person.class, Integer.class, "id");
			pk.setColumn("identifier");
			helper.testEditTable(helper.createMetadata(Person.class, schemaName, "People", null, pk), null, true);
		} finally {
			persistor.dropSchema(schemaName);
		}
	}
	

	@Test(expected=SyntaxError.class)
	public void testCreateTableNoFields() {
		final String schemaName = "test1";
		try {
			helper.testCreateSchema(schemaName, null, false);
			helper.testEditTable(helper.createMetadata(Person.class, schemaName, "People", null), null, true);
		} finally {
			persistor.dropSchema(schemaName);
		}
	}

	
	
	
	
	@Test
	public void testCreateDuplicateTable() {
		final String schemaName = "test1";
		try {
			helper.testCreateSchema(schemaName, null, false);
			
			final FieldMetadata<Person, Integer, Integer> pk = helper.createFieldMetadata(Person.class, Integer.class, "id");
			pk.setColumn("identifier");
			pk.setKey(true);
			
			final EntityMetadata<Person> emd = helper.createMetadata(Person.class, schemaName, "People", pk, pk);
			helper.testEditTable(emd, null, true);
			// try again
			try {
				persistor.createTable(emd, null);
				fail("Attempt to create table again must fail");
			} catch (final AlreadyExistsException e) {
				// it is OK. The exception is expected here.
			}
		} finally {
			persistor.dropSchema(schemaName);
		}
	}

	
	@Test
	public void testCreateAndDropTable() {
		final String schemaName = "test1";
		final String tableName = "People";
		try {
			helper.testCreateSchema(schemaName, null, false);
			
			// first create metadata
			final FieldMetadata<Person, Integer, Integer> pk = helper.createFieldMetadata(Person.class, Integer.class, "id");
			pk.setColumn("identifier");
			pk.setKey(true);

			// now create table
			final EntityMetadata<Person> emd = helper.createMetadata(Person.class, schemaName, tableName, pk, pk);
			helper.testEditTable(emd, null, true);
			
			// drop table 
			persistor.dropTable(schemaName, tableName);
			final TableMetadata tmd = helper.findTableMetadata(schemaName, tableName);
			assertNull(tmd);
		} finally {
			persistor.dropSchema(schemaName);
		}
	}
	
	
	
	//TODO: add test that drops column
	// This is irrelevant for now because dropping column does not work via CQL at least with currently used versions. 
	// See comment into CqlPersistor.processTable()

	
	
	@Test
	public void testCreateAndDropIndex() {
		final String schemaName = "test1";
		final String tableName = "People";
		try {
			helper.testCreateSchema(schemaName, null, false);

			
			// first create metadata
			final FieldMetadata<Person, Integer, Integer> pk = helper.createFieldMetadata(Person.class, Integer.class, "id");
			pk.setColumn("identifier");
			pk.setKey(true);

			
			final FieldMetadata<Person, String, String> nameField = helper.createFieldMetadata(Person.class, String.class, "name");
			nameField.setColumn("first_name");
			
			final EntityMetadata<Person> emd = helper.createMetadata(Person.class, schemaName, tableName, pk, pk, nameField);
			emd.addAllIndexes(Collections.singletonList(new IndexMetadata<Person>(Person.class, "name_index", nameField)));
			
			// now create table
			helper.testEditTable(emd, null, true);


			// create entity metadata again without index 
			final EntityMetadata<Person> emd2 = helper.createMetadata(Person.class, schemaName, tableName, pk, pk, nameField);
			// update (alter) table
			helper.testEditTable(emd2, null, false);
			
		} finally {
			persistor.dropSchema(schemaName);
		}
	}
	
	
	
	@Test
	public void testCreateTableWithColumnsOfAllTypes() {
		final String schemaName = "test1";
		try {
			helper.testCreateSchema(schemaName, null, false);
			
			final FieldMetadata<ManySimpleTypes, Integer, Integer> pk = helper.createFieldMetadata(ManySimpleTypes.class, int.class, "intPrimitive", "intPrimitive", true);
			
			@SuppressWarnings("unchecked")
			final
			FieldMetadata<ManySimpleTypes, Object, Object>[] fields = new FieldMetadata[] {
					pk,
					helper.createFieldMetadata(ManySimpleTypes.class, long.class, "longPrimitive"),
					helper.createFieldMetadata(ManySimpleTypes.class, float.class, "floatPrimitive"),
					helper.createFieldMetadata(ManySimpleTypes.class, double.class, "doublePrimitive"), 
					helper.createFieldMetadata(ManySimpleTypes.class, Integer.class, "intWrapper"),
					helper.createFieldMetadata(ManySimpleTypes.class, Long.class, "longWrapper"),
					helper.createFieldMetadata(ManySimpleTypes.class, Float.class, "floatWrapper"),
					helper.createFieldMetadata(ManySimpleTypes.class, Double.class, "doubleWrapper"),
					helper.createFieldMetadata(ManySimpleTypes.class, BigDecimal.class, "bigDecimal"),
					helper.createFieldMetadata(ManySimpleTypes.class, BigInteger.class, "bigInteger"),
					helper.createFieldMetadata(ManySimpleTypes.class, boolean.class, "booleanPrimitive"),
					helper.createFieldMetadata(ManySimpleTypes.class, Boolean.class, "booleanWrapper"),
					helper.createFieldMetadata(ManySimpleTypes.class, String.class, "string"),
					helper.createFieldMetadata(ManySimpleTypes.class, ByteBuffer.class, "bytebuffer"),
					helper.createFieldMetadata(ManySimpleTypes.class, byte[].class, "bytearray"),
					helper.createFieldMetadata(ManySimpleTypes.class, InetAddress.class, "inet"),
					helper.createFieldMetadata(ManySimpleTypes.class, Date.class, "date"),
					helper.createFieldMetadata(ManySimpleTypes.class, UUID.class, "uuid"),
			};
			
			
			final EntityMetadata<ManySimpleTypes> emd = helper.createMetadata(ManySimpleTypes.class, schemaName, "TypesTest", pk, fields);
			helper.testEditTable(emd, null, true);
		} finally {
			persistor.dropSchema(schemaName);
		}
	}
	

	@Test
	public void testCreateTableWithInnerCollections() {
		final String schemaName = "test1";
		try {
			helper.testCreateSchema(schemaName, null, false);
			
			final FieldMetadata<InnerCollections, Integer, Integer> pk = helper.createFieldMetadata(InnerCollections.class, int.class, "id", "id", true);
			
			@SuppressWarnings("unchecked")
			final
			FieldMetadata<InnerCollections, Object, Object>[] fields = new FieldMetadata[] {
					pk,
					helper.createFieldMetadata(InnerCollections.class, String[].class, "stringArray"),
					helper.createFieldMetadata(InnerCollections.class, List.class, new Class[] {String.class}, "stringList"),
					helper.createFieldMetadata(InnerCollections.class, Set.class, new Class[] {String.class}, "stringSet"),
					helper.createFieldMetadata(InnerCollections.class, Map.class, new Class[] {String.class, Integer.class}, "stringToIntegerMap"),
			};
			
			
			final EntityMetadata<InnerCollections> emd = helper.createMetadata(InnerCollections.class, schemaName, "InnerCollections", pk, fields);
			helper.testEditTable(emd, null, true);
		} finally {
			persistor.dropSchema(schemaName);
		}
	}
	
	
	
	
	/**
	 * This test creates entity metadata, then uses it to create table, 
	 * then validates just created table using the same entity.  
	 */
	@Test
	public void testCreateAndValidateTable() {
		final String schemaName = "test1";
		try {
			helper.testCreateSchema(schemaName, null, false);
			
			final FieldMetadata<Person, Integer, Integer> pk = helper.createFieldMetadata(Person.class, Integer.class, "id");
			pk.setColumn("identifier");
			pk.setKey(true);
			
			final EntityMetadata<Person> emd = helper.createMetadata(Person.class, schemaName, "People", pk, pk);
			helper.testEditTable(emd, null, true);
			persistor.validateTable(emd, null);
		} finally {
			persistor.dropSchema(schemaName);
		}
	}
	
	@Test(expected=IllegalStateException.class)
	public void testValidateNotExistingTable() {
		final String schemaName = "test1";
		try {
			helper.testCreateSchema(schemaName, null, false);
			
			final FieldMetadata<Person, Integer, Integer> pk = helper.createFieldMetadata(Person.class, Integer.class, "id");
			pk.setColumn("identifier");
			pk.setKey(true);
			
			final EntityMetadata<Person> emd = helper.createMetadata(Person.class, schemaName, "People", pk, pk);
			persistor.validateTable(emd, null);
		} finally {
			persistor.dropSchema(schemaName);
		}
	}

	@Test
	public void testValidateMissingFields() {
		final Pattern oneException = Pattern.compile("^ALTER TABLE test1.\"People\" ADD first_name text$");
		final Pattern allExceptions = Pattern.compile("^ALTER TABLE test1.\"People\" ADD first_name text.ALTER TABLE test1.\"People\" ADD age int$", Pattern.DOTALL | Pattern.MULTILINE);
		
		testValidate(null, allExceptions);
		testValidate(ThrowOnViolation.THROW_ALL_TOGETHER, allExceptions);
		testValidate(ThrowOnViolation.THROW_FIRST, oneException);
	}
	
	/**
	 * This testing scenario creates entity metadata and uses it to create table. 
	 * Then it creates other metadata from the same class but adds 2 more files and uses this metadata
	 * for validation of existing table. The validation should fail. 
	 * 
	 * The purpose of the test is to check that the error message is as expected dependently on
	 * configuration defined by argument {@code throwOnViolation} of type {@link ThrowOnViolation}. 
	 * This parameter defines whether the validation should stop when first problem is found ({@link ThrowOnViolation#THROW_FIRST}
	 * or continue validation and throw exception that contains message about all problems  
	 * ({@link ThrowOnViolation#THROW_ALL_TOGETHER} or {@code null} for default). 
	 * 
	 * @param throwOnViolation
	 * @param errorMessagePattern
	 */
	private void testValidate(final ThrowOnViolation throwOnViolation, final Pattern errorMessagePattern) {
		final String schemaName = "test1";
		final String tableName = "People";
		try {
			helper.testCreateSchema(schemaName, null, false);

			
			// first create metadata
			final FieldMetadata<Person, Integer, Integer> pk = helper.createFieldMetadata(Person.class, Integer.class, "id");
			pk.setColumn("identifier");
			pk.setKey(true);

			final EntityMetadata<Person> emd = helper.createMetadata(Person.class, schemaName, tableName, pk, pk);
			// now create table
			helper.testEditTable(emd, null, true);

			
			// create similar metadata but with 2 additional fields
			final FieldMetadata<Person, String, String> nameField = helper.createFieldMetadata(Person.class, String.class, "name");
			nameField.setColumn("first_name");
			
			final FieldMetadata<Person, Integer, Integer> ageField = helper.createFieldMetadata(Person.class, Integer.class, "age");
			ageField.setColumn("age");
			
			final EntityMetadata<Person> emd2 = helper.createMetadata(Person.class, schemaName, tableName, pk, pk, nameField, ageField);

			Map<String, Object> props = null;
			if (throwOnViolation != null) {
				props = Collections.<String, Object>singletonMap(CqlPersistorProperties.SCHEMA_VALIDATION.property(), throwOnViolation);
			}
			try {
				persistor.validateTable(emd2, props);
				fail("Schema violation exception is expected but was not thrown");
			} catch (final IllegalStateException e) {
				final String msg = e.getMessage();
				assertTrue("Unexpected error message. Should match pattern " + errorMessagePattern + " but was " + msg,  errorMessagePattern.matcher(msg).find());
			}
			
			
		} finally {
			persistor.dropSchema(schemaName);
		}
	}
	
	
	
	
	/**
	 * Creates table with 2 indexes, then adds yet another field with index (alters table), 
	 * then drops table.
	 */
	@Test
	public <E> void testCreateAlterTableAndIndex() {
		final String schemaName = "test1";
		
		try {
			helper.testCreateSchema(schemaName, null, false);
			final EntityMetadata<Person> emd = new EntityMetadata<>(Person.class);
			emd.setEntityName(Person.class.getSimpleName());
			emd.setTableName("People");
			emd.setSchemaName(schemaName);

			
			final FieldMetadata<Person, Integer, Integer> pk = helper.createFieldMetadata(Person.class, Integer.class, "id");
			pk.setColumn("identifier");
			pk.setKey(true);
			
			emd.setPrimaryKey(pk);
			
			
			final FieldMetadata<Person, String, String> nameField = helper.createFieldMetadata(Person.class, String.class, "name");
			nameField.setColumn("first_name");
			
			final FieldMetadata<Person, Integer, Integer> ageField = helper.createFieldMetadata(Person.class, Integer.class, "age");
			ageField.setColumn("age");
			
			emd.addField(pk);
			emd.addField(nameField);
			emd.addField(ageField);
			
			
			emd.addAllIndexes(Arrays.asList(
					new IndexMetadata<Person>(Person.class, "name_index", nameField),
					new IndexMetadata<Person>(Person.class, "age_index", ageField)
			));
			
			
			helper.testEditTable(emd, null, true);
			
			
			// now add column, i.e. alter table.
			final FieldMetadata<Person, String, String> lastNameField = helper.createFieldMetadata(Person.class, String.class, "lastName");
			lastNameField.setColumn("last_name");
			emd.addField(lastNameField);
			
			emd.addAllIndexes(Arrays.asList(
					new IndexMetadata<Person>(Person.class, "name_index", nameField),
					new IndexMetadata<Person>(Person.class, "last_name_index", lastNameField),
					new IndexMetadata<Person>(Person.class, "age_index", ageField)
			));
			
			helper.testEditTable(emd, null, false);

			// drop table and check that it is indeed dropped.
			final String keyspace = emd.getSchemaName();
			final String table = emd.getTableName();
			persistor.dropTable(keyspace, table);
			assertNull(helper.findTableMetadata(keyspace, table));
		} finally {
			persistor.dropSchema(schemaName);
		}
	}
	
	

	
	
}
