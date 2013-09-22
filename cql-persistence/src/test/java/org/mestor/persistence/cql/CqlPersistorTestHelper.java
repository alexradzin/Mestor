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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.mestor.cassandra.util.ReflectiveBean;
import org.mestor.context.EntityContext;
import org.mestor.context.Persistor;
import org.mestor.metadata.EntityMetadata;
import org.mestor.metadata.FieldMetadata;
import org.mestor.metadata.IndexMetadata;
import org.mockito.Mockito;

import com.datastax.driver.core.ColumnMetadata;
import com.datastax.driver.core.TableMetadata;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.primitives.Primitives;

class CqlPersistorTestHelper {
	private final Persistor persistor;
	final EntityContext ctx = Mockito.mock(EntityContext.class);
	
	CqlPersistorTestHelper() throws IOException {
		persistor = createAndConnect(Collections.<String, Object>emptyMap());
	}
	
	CqlPersistorTestHelper(Persistor persistor) {
		this.persistor = persistor;
	}
	
	private static Map<Class<?>, Class<?>> prop2cql = new HashMap<Class<?>, Class<?>>() {{
		put(BigInteger.class, Long.class);
		put(BigDecimal.class, BigDecimal.class);
		put(byte[].class, ByteBuffer.class);
		put(Byte[].class, ByteBuffer.class);
	}};

	Persistor createAndConnect(Map<String, Object> props) throws IOException {
		doReturn(props).when(ctx).getProperties();
		return new CqlPersistor(ctx);
	}

	
	void testCreateSchema(String schemaName, Map<String, Object> with, boolean drop) {
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
	<E> void testEditTable(EntityMetadata<E> entityMetadata, Map<String, Object> properties, boolean create) {
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

	
	
	TableMetadata findTableMetadata(String keyspace, String table) {
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
	
	<E> void assertMatches(EntityMetadata<E> emd, Map<String, Object> properties, TableMetadata tmd) {
		assertNotNull(tmd);
		
		assertEquals(emd.getTableName().toLowerCase(), tmd.getName().toLowerCase());
		assertEquals(emd.getSchemaName(), tmd.getKeyspace().getName());
		
		Collection<String> actualIndexedColumns = new HashSet<>();
		
		for (FieldMetadata<E, ?> fmd : emd.getFields()) {
			String column = fmd.getColumn();
			ColumnMetadata cmd = tmd.getColumn(column);
			assertNotNull("Column " + column + " is not found", cmd);
			assertEquals(column, cmd.getName());
			
			
			
			Class<?> fmdType = fmd.getType();
			Class<?> fmdTypeComponent = fmdType.getComponentType();
			Set<Class<?>> specialArrayTypes = new HashSet<>(Arrays.<Class<?>>asList(byte.class, Byte.class));
			if (fmdType.isArray() && !specialArrayTypes.contains(fmdTypeComponent)) {
				assertEquals(List.class, cmd.getType().asJavaClass());
				assertEquals(fmdTypeComponent, cmd.getType().getTypeArguments().iterator().next().asJavaClass());
			} else {
				assertEquals(toCqlJavaType(fmd.getType()), cmd.getType().asJavaClass());
			}
			
			if(cmd.getIndex() != null) {
				actualIndexedColumns.add(cmd.getIndex().getIndexedColumn().getName());
			}
		}
		
		Collection<String> exepectedPKColumns = 
		Collections2.transform(
		Collections2.filter(emd.getFields(), new Predicate<FieldMetadata<?, ?>>() {
			@Override
			public boolean apply(FieldMetadata<?, ?> fmd) {
				return fmd.isKey();
			}
		}), 
		new Function<FieldMetadata<?, ?>, String>() {
			@Override
			public String apply(FieldMetadata<?, ?> fmd) {
				return fmd.getColumn();
			}
		});
		
		Collection<String> actualPKColumns = 
		Collections2.transform(tmd.getPrimaryKey(), new Function<ColumnMetadata, String>() {
			@Override
			public String apply(ColumnMetadata cmd) {
				return cmd.getName();
			}
		});
		
	
		assertArrayEquals(exepectedPKColumns.toArray(), actualPKColumns.toArray());
		
		Map<String, String> expectedIndexes = new HashMap<>();
		
		for (IndexMetadata<E> imd : emd.getIndexes()) {
			FieldMetadata<E, ? extends Object>[] indexFields = imd.getField();
			assertEquals(1, indexFields.length);
			expectedIndexes.put(imd.getName(), indexFields[0].getColumn());
		}

		assertEquals("Unexpected list of indexed columns", new HashSet<String>(expectedIndexes.values()), new HashSet<String>(actualIndexedColumns));
	}
	
	private Class<?> toCqlJavaType(Class<?> type) {
		Class<?> cqlType = prop2cql.get(type);
		if (cqlType != null) {
			return cqlType;
		}
		
		return Primitives.wrap(type);
	}
	
	
	@SuppressWarnings({"unchecked", "rawtypes"})
	<T> EntityMetadata<T> createMetadata(Class<T> clazz, String schemaName, String tableName, FieldMetadata pk, FieldMetadata ... fieldsMetadata) {
		EntityMetadata<T> emd = new EntityMetadata<>(clazz);
		emd.setEntityName(clazz.getSimpleName());
		emd.setTableName(tableName);
		emd.setSchemaName(schemaName);
		
		if (pk != null) {
			pk.setKey(true);
		}
		emd.setPrimaryKey(pk);
		
		for (FieldMetadata<T, Object> field : fieldsMetadata) {
			emd.addField(field);
		}
		
		return emd;
	}
	

	<T, F> FieldMetadata<T, F> createFieldMetadata(Class<T> classType, Class<F> type, String name) {
		return createFieldMetadata(classType, type, name, name, false);
	}
	
	<T, F> FieldMetadata<T, F> createFieldMetadata(Class<T> classType, Class<F> type, Class<?>[] generics, String name) {
		return createFieldMetadata(classType, type, generics, name, name, false);
	}
	
	<T, F> FieldMetadata<T, F> createFieldMetadata(Class<T> classType, Class<F> type, String name, String column, boolean key) {
		return createFieldMetadata(classType, type, null, name, column, key);
	}
	
	
	<T, F> FieldMetadata<T, F> createFieldMetadata(Class<T> classType, Class<F> type, Class<?>[] generics, String name, String column, boolean key) {
		FieldMetadata<T, F> field = new FieldMetadata<>(
				classType, type, name, 
				ReflectiveBean.getField(classType, name),
				ReflectiveBean.getGetter(classType, name),
				ReflectiveBean.getSetter(classType, type, name)
		);
		field.setColumn(column);
		field.setKey(key);
		if (generics != null) {
			field.setGenericTypes(Arrays.asList(generics));
		}
		return field;
	}
	
	
	
	Persistor getPersistor() {
		
		return persistor;
	}
}
