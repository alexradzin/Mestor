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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.mestor.context.EntityContext;
import org.mestor.context.Persistor;
import org.mestor.metadata.EntityMetadata;
import org.mestor.metadata.FieldMetadata;
import org.mestor.metadata.IndexMetadata;
import org.mestor.metadata.MetadataTestHelper;
import org.mockito.Mockito;

import com.datastax.driver.core.ColumnMetadata;
import com.datastax.driver.core.TableMetadata;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.primitives.Primitives;

class CqlPersistorTestHelper extends MetadataTestHelper {
	private final Persistor persistor;
	final EntityContext ctx = Mockito.mock(EntityContext.class);


	CqlPersistorTestHelper() throws IOException {
		this(Collections.<String, Object>emptyMap());
	}

	CqlPersistorTestHelper(final Map<String, Object> props) throws IOException {
		super();
		persistor = createAndConnect(props);
	}

	CqlPersistorTestHelper(final Persistor persistor) {
		super();
		this.persistor = persistor;
	}

	@SuppressWarnings("serial")
	private static Map<Class<?>, Class<?>> prop2cql = new HashMap<Class<?>, Class<?>>() {{
		put(BigInteger.class, Long.class);
		put(BigDecimal.class, BigDecimal.class);
		put(byte[].class, ByteBuffer.class);
		put(Byte[].class, ByteBuffer.class);
	}};

	Persistor createAndConnect(final Map<String, Object> props) throws IOException {
		doReturn(props).when(ctx).getParameters();
		return new CqlPersistor(ctx);
	}


	void testCreateSchema(final String schemaName, final Map<String, Object> with, final boolean drop) {
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
	<E> void testEditTable(final EntityMetadata<E> entityMetadata, final Map<String, Object> properties, final boolean create, final String ... additionalIndexes) {
		final String keyspace = entityMetadata.getSchemaName();
		final String table = entityMetadata.getTableName();

		final TableMetadata existingTable = findTableMetadata(keyspace, table);
		if (create) {
			assertNull(existingTable);
			persistor.createTable(entityMetadata, properties);
		} else {
			assertNotNull(existingTable);
			persistor.updateTable(entityMetadata, properties);
		}

		final TableMetadata tmd = findTableMetadata(keyspace, table);
		assertNotNull(tmd);
		assertMatches(entityMetadata, properties, tmd, additionalIndexes);
	}



	TableMetadata findTableMetadata(final String keyspace, final String table) {
		// search for just created table. Since (at least in current configuration) cassandra is case-insensitive
		// and creates tables using lower case and we do not know whether this is configurable
		// we have to search for table by iterating over all tables to make test more not sensitive to the possible
		// future configuration changes.
		for (final TableMetadata t : ((CqlPersistor)persistor).getCluster().getMetadata().getKeyspace(keyspace).getTables()) {
			if (t.getName().equalsIgnoreCase(table)) {
				return t;
			}
		}
		return null;
	}

	<E> void assertMatches(final EntityMetadata<E> emd, final Map<String, Object> properties, final TableMetadata tmd, final String ... additionalIndexes) {
		assertNotNull(tmd);

		assertEquals(emd.getTableName().toLowerCase(), tmd.getName().toLowerCase());
		assertEquals(emd.getSchemaName(), tmd.getKeyspace().getName());

		final Collection<String> actualIndexedColumns = new HashSet<>();

		for (final FieldMetadata<E, ?, ?> fmd : emd.getFields()) {
			final String column = fmd.getColumn();
			if (column == null) {
				continue;
			}
			final ColumnMetadata cmd = tmd.getColumn(column);
			assertNotNull("Column " + column + " is not found", cmd);
			assertEquals(column, cmd.getName());



			final Class<?> fmdType = fmd.getType();
			final Class<?> fmdTypeComponent = fmdType.getComponentType();
			final Set<Class<?>> specialArrayTypes = new HashSet<>(Arrays.<Class<?>>asList(byte.class, Byte.class));
			if (fmdType.isArray() && !specialArrayTypes.contains(fmdTypeComponent)) {
				assertEquals(List.class, cmd.getType().asJavaClass());
				assertEquals(fmdTypeComponent, cmd.getType().getTypeArguments().iterator().next().asJavaClass());
			} else {
				assertEquals(toCqlJavaType(fmd.getColumnType()), cmd.getType().asJavaClass());
			}

			if(cmd.getIndex() != null) {
				actualIndexedColumns.add(cmd.getIndex().getIndexedColumn().getName());
			}
		}

		Collection<String> exepectedPKColumns =
		Collections2.transform(
		Collections2.filter(emd.getFields(), new Predicate<FieldMetadata<?, ?, ?>>() {
			@Override
			public boolean apply(final FieldMetadata<?, ?, ?> fmd) {
				return fmd.isKey();
			}
		}),
		new Function<FieldMetadata<?, ?, ?>, String>() {
			@Override
			public String apply(final FieldMetadata<?, ?, ?> fmd) {
				return fmd.getColumn();
			}
		});


		final FieldMetadata<E, ?, ?> pkmd = emd.getPrimaryKey();
		if (pkmd != null) {
			final String pkColumn = pkmd.getColumn();

			// column exists that means that PK is "real" but for some reason does not appear in regular list of fields.
			if (pkColumn != null && emd.getField(pkColumn) == null) {
				exepectedPKColumns = new ArrayList<String>(exepectedPKColumns); // collection after guava transforms is unmodifiable.
				exepectedPKColumns.add(pkColumn);
			}
		}

		final Collection<String> actualPKColumns =
		Collections2.transform(tmd.getPrimaryKey(), new Function<ColumnMetadata, String>() {
			@Override
			public String apply(final ColumnMetadata cmd) {
				return cmd.getName();
			}
		});


		assertArrayEquals(exepectedPKColumns.toArray(), actualPKColumns.toArray());

		final Map<String, String> expectedIndexes = new HashMap<>();

		for (final IndexMetadata<E> imd : emd.getIndexes()) {
			final FieldMetadata<E, ? extends Object, ? extends Object>[] indexFields = imd.getField();
			assertEquals(1, indexFields.length);
			expectedIndexes.put(imd.getName(), indexFields[0].getColumn());
		}


		final Set<String> allExpectedIndexes = new HashSet<>(expectedIndexes.values());
		if (additionalIndexes != null) {
			allExpectedIndexes.addAll(Arrays.asList(additionalIndexes));
		}

		assertEquals("Unexpected list of indexed columns", allExpectedIndexes, new HashSet<String>(actualIndexedColumns));
	}

	private Class<?> toCqlJavaType(final Class<?> type) {
		final Class<?> cqlType = prop2cql.get(type);
		if (cqlType != null) {
			return cqlType;
		}

		return Primitives.wrap(type);
	}



	Persistor getPersistor() {

		return persistor;
	}
}
