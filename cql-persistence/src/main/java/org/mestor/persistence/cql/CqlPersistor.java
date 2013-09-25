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

import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static com.datastax.driver.core.querybuilder.QueryBuilder.insertInto;
import static com.datastax.driver.core.querybuilder.QueryBuilder.select;
import static com.google.common.collect.Iterables.transform;
import static org.mestor.persistence.cql.management.CommandBuilder.createKeyspace;
import static org.mestor.persistence.cql.management.CommandBuilder.dropKeyspace;
import static org.mestor.persistence.cql.management.CommandHelper.quote;

import java.io.IOException;
import java.lang.reflect.Array;
import java.math.BigInteger;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.mestor.context.EntityContext;
import org.mestor.context.Persistor;
import org.mestor.metadata.EntityComparator;
import org.mestor.metadata.EntityMetadata;
import org.mestor.metadata.FieldMetadata;
import org.mestor.metadata.IndexMetadata;
import org.mestor.metadata.ValueConverter;
import org.mestor.persistence.cql.CqlPersistorProperties.ThrowOnViolation;
import org.mestor.persistence.cql.management.AlterTable;
import org.mestor.persistence.cql.management.CommandBuilder;
import org.mestor.persistence.cql.management.CreateTable;
import org.mestor.persistence.cql.management.CreateTable.FieldAttribute;
import org.mestor.util.CollectionUtils;
import org.mestor.wrap.ObjectWrapperFactory;
import org.mestor.wrap.javassist.JavassistObjectWrapperFactory;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ColumnMetadata;
import com.datastax.driver.core.KeyspaceMetadata;
import com.datastax.driver.core.Query;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.TableMetadata;
import com.datastax.driver.core.exceptions.NoHostAvailableException;
import com.datastax.driver.core.querybuilder.Clause;
import com.datastax.driver.core.querybuilder.Delete;
import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.datastax.driver.core.querybuilder.Select.Where;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

public class CqlPersistor implements Persistor {
	private EntityContext context;
	private Cluster cluster;
	private Session session;

	private Map<String, Object> defaultKeyspaceProperties;
	

	private final Function<Query, Void> sessionHandler = new Function<Query, Void>() {
		@Override
		public Void apply(Query query) {
			session.execute(query);
			return null;
		}
	};

	
	/**
	 * This interface provides bidirectional access to underlined {@link ValueConverter}. It is good to reuse
	 * code that should be executed when entity is stored in database and fetched from it.  
	 * The interface is named after pushmi-pullyu (pronounced "push-me—pull-you"), 
	 * character of "The Story of Doctor Dolittle" by Hugh Lofting.   
	 * It is a "gazelle-unicorn cross" which has two heads (one of each) at opposite ends of its body.
	 * @author alexr
	 */
	private static interface PushmiPullyuConverter {
		public Object convert(ValueConverter<Object, Object> conveter, Object from);
	}
	
	
	@SuppressWarnings({ "cast", "unchecked" })
	public CqlPersistor(EntityContext context) throws IOException {
		if (context == null) {
			throw new IllegalArgumentException(
					EntityContext.class.getSimpleName() + " cannot be null");
		}
		this.context = context;

		Map<String, Object> properties = context.getProperties();
		// set defaults
		Integer port = CqlPersistorProperties.CASSANDRA_PORT.getValue(properties);
		String[] hosts = CqlPersistorProperties.CASSANDRA_HOSTS.getValue(properties);

		Cluster.Builder clusterBuilder = Cluster.builder();
		if (port != null) {
			clusterBuilder.withPort(port);
		}
		connect(clusterBuilder,
				hosts,
				(Map<String, Object>) CqlPersistorProperties.CASSANDRA_KEYSPACE_PROPERTIES
						.getValue(properties));
	}

	private void connect(Cluster.Builder clusterBuilder, String[] hosts,
			Map<String, Object> keyspaceProperties) throws IOException {
		try {
			cluster = clusterBuilder.addContactPoints(hosts).build();
			session = cluster.connect();
			defaultKeyspaceProperties = keyspaceProperties;
		} catch (NoHostAvailableException e) {
			throw new IOException(e);
		}
	}

	@Override
	public <E> void store(E entity) {
		Class<E> clazz = getEntityClass(entity);
		EntityMetadata<E> emd = context.getEntityMetadata(clazz);
		String table = emd.getTableName();
		String keyspace = emd.getSchemaName();

		Insert insert = insertInto(quote(keyspace), quote(table));
		
		
		Collection<FieldMetadata<E, Object, Object>> allFields = emd.getFields();
		
		FieldMetadata<E, Object, Object> pkmd = emd.getPrimaryKey();
		if (pkmd != null) {
			String pkColumn = pkmd.getColumn();
			if (emd.getField(pkColumn) == null) {
				allFields = new ArrayList<>(allFields);
				allFields.add(pkmd);
			}
		}
		
		for (FieldMetadata<E, Object, ?> fmd : allFields) {
			String name = fmd.getColumn();
			Object value = convertValue(fmd, transformValue(fmd.getAccessor().getValue(entity)), new PushmiPullyuConverter() {
				@Override
				public Object convert(ValueConverter<Object, Object> conveter, Object v) {
					return conveter.toColumn(v);
				}
			});
			if (value != null) {
				insert.value(quote(name), value);
			}
		}

		session.execute(insert);

		// TODO implement cascade here
	}

	


	/**
	 * This method converts given value into other form using appropriate converter. The method is used in 
	 * both direction: when saving object in database for converting java types to corresponding column types 
	 * and when fetching object from database for converting column types to java types.
	 * The direction is controlled by {@link PushmiPullyuConverter}.  
	 * @param fmd
	 * @param obj
	 * @param pushmiPullyu
	 * @return converted object
	 */
	private <E> Object convertValue(FieldMetadata<E, Object, ?> fmd, Object obj, PushmiPullyuConverter pushmiPullyu) {
		Object result = obj;
		
		
		if (obj instanceof Collection) {
			@SuppressWarnings("unchecked")
			Collection<Object> collection = (Collection<Object>)obj;
			
			final Collection<Object> resCollection;
			if (obj instanceof List) {
				resCollection = new ArrayList<Object>();
			} else if (obj instanceof Set) {
				resCollection = new LinkedHashSet<Object>(); //LinkedHashSet to preserve the order just in case  
			} else {
				throw new IllegalArgumentException("Only List or Set are supported, got " + obj.getClass());
			}
			
			ValueConverter<Object, Object> elementConverter = fmd.getConverter(1);
			for (Object element : collection) {
				resCollection.add(pushmiPullyu.convert(elementConverter, element));
			}
			result = resCollection;
		} else if (obj instanceof Map) {
			@SuppressWarnings({ "rawtypes", "unchecked" })
			final Map<Object, Object> map = (Map)obj;
			final Map<Object, Object> resMap = new LinkedHashMap<>();
			
			ValueConverter<Object, Object> keyConverter = fmd.getConverter(1);
			ValueConverter<Object, Object> valueConverter = fmd.getConverter(2);
			
			for (Entry<Object, Object> entry : map.entrySet()) {
				Object key = entry.getKey();
				Object value = entry.getValue();
				resMap.put(pushmiPullyu.convert(keyConverter, key), pushmiPullyu.convert(valueConverter, value));
			}
			
			result = resMap;
		}

		
		return pushmiPullyu.convert(fmd.getConverter(0), result);
	}
	
	
	
	private Object transformValue(Object value) {
		if (value == null) {
			return value;
		}
		if (byte[].class.equals(value.getClass())) {
			return ByteBuffer.wrap((byte[]) value);
		}
		if (value instanceof InetAddress) {
			return ((InetAddress) value).getHostAddress();
		}
		if (value.getClass().isArray()) {
			int n = Array.getLength(value);
			List<Object> list = new ArrayList<Object>();
			for (int i = 0; i < n; i++) {
				list.add(Array.get(value, i));
			}
			return list;
		}
		return value;
	}

	
	@Override
	public <E, P> E fetch(Class<E> clazz, P primaryKey) {
		EntityMetadata<E> emd = context.getEntityMetadata(clazz);

		Select.Selection select = select();

		Map<String, Class<?>[]> fieldTypes = new LinkedHashMap<String, Class<?>[]>();
		for (FieldMetadata<E, ?, ?> fmd : emd.getFields()) {
			if (!fmd.isLazy()) {
				select.column(quote(fmd.getColumn()));
				fieldTypes.put(fmd.getColumn(), emd.getColumnTypes(fmd.getColumn()));
			}
		}

		Iterator<Map<String, Object>> result = doSelect(select, emd, primaryKey, fieldTypes).iterator();

		if (!result.hasNext()) {
			return null;
		}

		Map<String, Object> data = result.next();
		// TODO: should we throw exception if more than one record were
		// returned?

		E entity;
		try {
			entity = clazz.newInstance();
		} catch (ReflectiveOperationException e) {
			throw new IllegalArgumentException("Class " + clazz
					+ " must have accessible default constructor");
		}

		// set primary key. If we are here the primary key definitely equals to one that is 
		// in persisted entity.
		emd.getPrimaryKey().getAccessor().setValue(entity, primaryKey);

		// create fetch context lazily
		boolean fetchContextOriginator = false;
		if (fetchContext.get() == null) {
			fetchContext.set(new TreeMap<Object, Object>(new EntityComparator<Object>(context)));
			fetchContextOriginator = true;
		}
		
		// store the just created instance of entity into fetchContext BEFORE populating of all its properties
		// to prevent possible infinite recursion if entity refers to itself either directly or indirectly.
		fetchContext.get().put(entity, entity);
		
		try {
			for (Entry<String, Object> d : data.entrySet()) {
				String columnName = d.getKey();
				Object columnValue = d.getValue();
				
				FieldMetadata<E, Object, Object> fmd = emd.getField(columnName);
				//Object fieldValue = fmd.getConverter().fromColumn(columnValue);
				
				Object fieldValue = convertValue(fmd, columnValue, new PushmiPullyuConverter() {
					@Override
					public Object convert(ValueConverter<Object, Object> conveter, Object v) {
						return conveter.fromColumn(v);
					}
				});
				
				
				fmd.getAccessor().setValue(entity, fieldValue);
			}
		} finally {
			if (fetchContextOriginator) {
				fetchContext.remove();
			}
		}

		return getObjectWrapperFactory(clazz).wrap(entity);
	}

	@Override
	public <E> void remove(E entity) {
		Class<E> clazz = getEntityClass(entity);
		EntityMetadata<E> emd = context.getEntityMetadata(clazz);
		String table = emd.getTableName();
		String keyspace = emd.getSchemaName();
		Delete.Where where = QueryBuilder.delete()
				.from(quote(keyspace), quote(table)).where();

		Object primaryKey = emd.getPrimaryKey().getAccessor().getValue(entity);

		for (Clause clause : getPrimaryKeyClause(emd, primaryKey)) {
			where.and(clause);
		}

		session.execute(where);
		// TODO add support of cascade here.
	}

	private <E, P> Iterable<Clause> getPrimaryKeyClause(EntityMetadata<E> emd, P primaryKey) {
		FieldMetadata<E, Object, Object> pkmd = emd.getPrimaryKey();
		Object pkColumnValue = pkmd.getConverter().toColumn(primaryKey);
		String pkName = emd.getPrimaryKey().getColumn();
		return Arrays.asList(eq(quote(pkName), pkColumnValue));
	}

	@Override
	public <E> boolean exists(Class<E> entityClass, Object primaryKey) {
		return count(entityClass, primaryKey).signum() == 1;
	}

	private <E> BigInteger count(Class<E> entityClass, Object primaryKey) {
		EntityMetadata<E> emd = context.getEntityMetadata(entityClass);
		return (BigInteger)doSelect(
				select().countAll(),
				emd,
				primaryKey,
				Collections.<String, Class<?>[]> singletonMap("count", new Class[] { BigInteger.class })).
					iterator().next().get("count");
	}
	
	
	
	private <E, P> Iterable<Map<String, Object>> doSelect(
			Select.Builder queryBuilder, EntityMetadata<E> emd, P primaryKey,
			Map<String, Class<?>[]> fields) {
		String table = emd.getTableName();
		String keyspace = emd.getSchemaName();

		Where where = queryBuilder.from(quote(keyspace), quote(table)).allowFiltering().where();

		for (Clause clause : getPrimaryKeyClause(emd, primaryKey)) {
			where.and(clause);
		}

		return execute(where, fields);
	}

	@Override
	public <E> Object[] fetchProperty(E entity, String... propertyNames) {
		Class<E> clazz = getEntityClass(entity);
		EntityMetadata<E> emd = context.getEntityMetadata(clazz);
		Object primaryKey = emd.getPrimaryKey().getAccessor().getValue(entity);

		Set<String> propertyNamesSet = new HashSet<>(Arrays.asList(propertyNames));

		
		Select.Selection select = select();

		List<String> columns = new ArrayList<>();
		Map<String, Class<?>[]> columnTypes = new LinkedHashMap<String, Class<?>[]>();
		for (FieldMetadata<E, ?, ?> fmd : emd.getFields()) {
			if (propertyNamesSet.contains(fmd.getName())) {
				final String column = fmd.getColumn();
				columns.add(column);
				select.column(quote(column));
				columnTypes.put(column, emd.getColumnTypes(column));
			}
		}

		Object[] result = new Object[propertyNames.length];

		Map<String, Object> rawResult = doSelect(select, emd, primaryKey, columnTypes).iterator().next();

		for (int i = 0; i < result.length; i++) {
			result[i] = rawResult.get(columns.get(i));
		}

		return result;
	}

	@Override
	public <T> ObjectWrapperFactory<T> getObjectWrapperFactory(Class<T> type) {
		return new JavassistObjectWrapperFactory<T>(context);
	}

	@Override
	public void createSchema(String name, Map<String, Object> properties) {
		session.execute(createKeyspace().named(name).with(
				properties == null ? defaultKeyspaceProperties : properties));
	}

	@Override
	public void dropSchema(String name) {
		session.execute(dropKeyspace().named(name));
	}

	@Override
	public Iterable<String> getSchemaNames() {
		return Lists.transform(cluster.getMetadata().getKeyspaces(),
				new Function<KeyspaceMetadata, String>() {
					@Override
					public String apply(KeyspaceMetadata input) {
						return input.getName();
					}
				});
	}

	@Override
	public void useSchema(String name) {
		session.execute("USE " + name);
	}

	@Override
	public <E> void createTable(EntityMetadata<E> entityMetadata,
			Map<String, Object> properties) {
		createTable(sessionHandler, entityMetadata, properties);
	}

	private <E> void createTable(Function<Query, Void> queryHandler,
			EntityMetadata<E> entityMetadata, Map<String, Object> properties) {
		final CreateTable table = CommandBuilder.createTable()
				.named(entityMetadata.getTableName())
				.in(entityMetadata.getSchemaName()).with(properties);

		final Collection<String> indexedColumns = getIndexedColumns(entityMetadata);
		
		FieldMetadata<E, ?, ?> pkmd = entityMetadata.getPrimaryKey();
		if (pkmd != null) {
			String pkColumn = pkmd.getColumn();
			if (entityMetadata.getField(pkColumn) == null) {
				addColumn(table, pkmd, indexedColumns);					
			}
		}
		
		for (FieldMetadata<E, ?, ?> fmd : entityMetadata.getFields()) {
			addColumn(table, fmd, indexedColumns);					
		}

		queryHandler.apply(table);

		final String keyspaceName = entityMetadata.getSchemaName();
		final String tableName = entityMetadata.getTableName();

		// Index creation statement does not support specification of keyspace
		// as a part of index identifier using dot-notation.
		// So we have to specify current keyspace using "use keyspace"
		useSchema(keyspaceName);
		for (String column : getRequiredIndexedFields(entityMetadata)) {
			queryHandler.apply(CommandBuilder.createIndex().in(keyspaceName)
					.named(createIndexName(keyspaceName, tableName, column))
					.on(tableName).column(column));
		}
	}
	
	private <E> void addColumn(CreateTable table, FieldMetadata<E, ?, ?> fmd, Collection<String> indexedColumns) {
			table.add(fmd.getColumn(), fmd.getColumnType(), fmd.getColumnGenericTypes()
					.toArray(new Class[0]),
					getFieldAttributes(fmd, indexedColumns));
	}

	@Override
	public <E> void updateTable(EntityMetadata<E> entityMetadata,
			Map<String, Object> properties) {
		processTable(sessionHandler, entityMetadata, properties);
	}

	@Override
	public <E> void validateTable(EntityMetadata<E> entityMetadata,
			Map<String, Object> properties) {
		final Map<String, Object> allProps = CollectionUtils.merge(
				context.getProperties(), properties);

		final ThrowOnViolation onViolation = CqlPersistorProperties.SCHEMA_VALIDATION
				.getValue(allProps);

		final Function<Query, Void> validationHandler;
		final List<String> errorMessages = new ArrayList<>();

		switch (onViolation) {
		case THROW_ALL_TOGETHER:
			validationHandler = new Function<Query, Void>() {
				@Override
				public Void apply(Query query) {
					errorMessages.add(query.toString());
					return null;
				}
			};
			break;
		case THROW_FIRST:
			validationHandler = new Function<Query, Void>() {
				@Override
				public Void apply(Query query) {
					throw new IllegalStateException(query.toString());
				}
			};
			break;
		default:
			throw new IllegalArgumentException(onViolation.name());
		}

		processTable(validationHandler, entityMetadata, properties);

		if (!errorMessages.isEmpty()) {
			throw new IllegalStateException(Joiner.on(
					System.getProperty("line.separator")).join(errorMessages));
		}
	}

	@Override
	public <E> void dropTable(String keyspace, String tableName) {
		session.execute(CommandBuilder.dropTable().named(tableName)
				.in(keyspace));
	}

	@Override
	public Iterable<String> getTableNames(String keyspace) {
		return Collections2.transform(
				cluster.getMetadata().getKeyspace(keyspace).getTables(),
				new Function<TableMetadata, String>() {
					@Override
					public String apply(TableMetadata table) {
						return table.getName();
					}
				});
	}

	// private <T> Iterable<T> selectSingleColumn(String keyspace, String table,
	// String columnName, Class<T> columnType) {
	// return execute(select().column(columnName).from(keyspace, table),
	// columnName, columnType);
	// }

	// private <T> Iterable<T> execute(final Query query, final String name,
	// Class<T> type) {
	// return execute(query, name, new Class<?>[] {type});
	// }

	// private <T> Iterable<T> execute(final Query query, final String name,
	// Class<?>[] types) {
	// return transform(session.execute(query), new RowFieldExtractor<T>(name,
	// types));
	// }

	private Iterable<Map<String, Object>> execute(final Query query,
			final Map<String, Class<?>[]> fields) {
		return transform(session.execute(query), new RowSplitter(fields));
	}

	private <E> FieldAttribute[] getFieldAttributes(FieldMetadata<E, ?, ?> fmd,
			Collection<String> indexedColumns) {
		Collection<FieldAttribute> attrs = new ArrayList<>();
		if (fmd.isKey()) {
			attrs.add(FieldAttribute.PRIMARY_KEY);
		}
		if (indexedColumns.contains(fmd.getColumn())) {
			attrs.add(FieldAttribute.INDEX);
		}
		return attrs.toArray(new FieldAttribute[0]);
	}

	private <E> Collection<String> getIndexedColumns(
			EntityMetadata<E> entityMetadata) {
		Collection<String> indexedColumns = new LinkedHashSet<String>();
		for (IndexMetadata<E> imd : entityMetadata.getIndexes()) {
			for (FieldMetadata<E, ?, ?> fmd : imd.getField()) {
				indexedColumns.add(fmd.getColumn());
			}
		}
		return indexedColumns;
	}

	private <E> void processTable(Function<Query, Void> queryHandler,
			EntityMetadata<E> entityMetadata, Map<String, Object> properties) {
		String entityTable = entityMetadata.getTableName();
		String existingTableName = null;
		for (String tableName : getTableNames(entityMetadata.getSchemaName())) {
			if (entityTable.equalsIgnoreCase(tableName)) {
				existingTableName = tableName;
			}
		}

		if (existingTableName == null) {
			createTable(queryHandler, entityMetadata, properties);
			return;
		}

		// table already exists

		TableMetadata existingTable = cluster.getMetadata()
				.getKeyspace(entityMetadata.getSchemaName())
				.getTable(existingTableName);

		Map<String, ColumnMetadata> existingColumns = new LinkedHashMap<>();
		for (ColumnMetadata cmd : existingTable.getColumns()) {
			existingColumns.put(cmd.getName(), cmd);
		}

		Collection<String> indexedColumns = getIndexedColumns(entityMetadata);

		// find fields that have to be added or altered
		for (FieldMetadata<E, ?, ?> fmd : entityMetadata.getFields()) {
			// AlterTable table =
			// CommandBuilder.alterTable().named(entityMetadata.getTableName()).in(entityMetadata.getSchemaName()).with(properties);
			String column = fmd.getColumn();
			Class<?> columnType = fmd.getColumnType();
			
			ColumnMetadata existingColumn = existingColumns.get(column);
			if (existingColumn == null) {
				handle(queryHandler,
						CommandBuilder.alterTable().addColumn(column,
								columnType), entityMetadata, properties);
				continue;
			}

			Class<?> existingType = existingColumn.getType().asJavaClass();

			boolean existingColumnIsIndexed = existingColumn.getIndex() != null;
			boolean fieldIsIndexed = indexedColumns.contains(fmd.getColumn());

			if (!existingType.isAssignableFrom(columnType)
					|| existingColumnIsIndexed != fieldIsIndexed) {
				handle(queryHandler,
						CommandBuilder.alterTable().alterColumn(column,
								columnType), entityMetadata, properties);
				continue;
			}
		}

		// find fields that should be dropped
		// TODO: not sure that this should be always done. The "extra" fields
		// may contain data.
		// This code is currently commented out for 2 reasons.
		// 1. attempt to drop column from table rises error message (even
		// manually). Questions are asked at SO and datastax forum
		// http://stackoverflow.com/questions/18842933/failed-to-drop-column-in-cassandra-using-cql
		// http://www.datastax.com/support-forums/topic/failed-to-drop-column?replies=1#post-16148
		// 2. Even if it works it is a BIG question whether we should drop
		// columns that could contain data. Probably we should do it
		// only if this column is empty for all records.
		/*
		 * for (ColumnMetadata cmd : existingTable.getColumns()) {
		 * if(entityMetadata.getField(cmd.getName()) == null) {
		 * queryHandler.apply
		 * (CommandBuilder.alterTable().dropColumn(cmd.getName(
		 * )).named(entityMetadata
		 * .getTableName()).in(entityMetadata.getSchemaName
		 * ()).with(properties)); } }
		 */

		// process indexes
		String keyspaceName = entityMetadata.getSchemaName();
		String tableName = entityMetadata.getTableName();

		Set<String> requiredIndexes = getRequiredIndexedFields(entityMetadata);

		Set<String> existingIndexes = new HashSet<>(Collections2.transform(
				Collections2.filter(existingTable.getColumns(),
						new Predicate<ColumnMetadata>() {
							@Override
							public boolean apply(ColumnMetadata cmd) {
								return cmd.getIndex() != null;
							}
						}), new Function<ColumnMetadata, String>() {
					@Override
					public String apply(ColumnMetadata cmd) {
						return cmd.getName();
					}
				}));

		// find missing indexes
		Set<String> missingIndexes = new HashSet<>(requiredIndexes);
		missingIndexes.removeAll(existingIndexes);

		for (String column : missingIndexes) {
			queryHandler.apply(CommandBuilder.createIndex().in(keyspaceName)
					.named(createIndexName(keyspaceName, tableName, column))
					.on(tableName).column(column));
		}

		// find redundant indexes
		Set<String> redundantIndexes = new HashSet<>(existingIndexes);
		redundantIndexes.removeAll(requiredIndexes);

		for (String column : redundantIndexes) {
			queryHandler.apply(CommandBuilder.dropIndex().named(
					createIndexName(keyspaceName, tableName, column)));
		}

	}

	private <E> void handle(Function<Query, Void> queryHandler,
			AlterTable command, EntityMetadata<E> entityMetadata,
			Map<String, Object> properties) {
		queryHandler.apply(command.named(entityMetadata.getTableName())
				.in(entityMetadata.getSchemaName()).with(properties));
	}

	/**
	 * Creates unique index name using format
	 * {@code KEYSPACE_TABLE_COLUMN_index}.
	 * 
	 * The index name is global in whole Cassandra server, so we have to use
	 * keyspace, table name and column name when creating index name to avoid
	 * conflicts.
	 * 
	 * @param keyspace
	 * @param table
	 * @param column
	 * @return unique index name
	 */
	private String createIndexName(String keyspace, String table, String column) {
		return Joiner.on('_').join(keyspace, table, column, "index");
	}

	@SuppressWarnings("unchecked")
	private <E> Class<E> getEntityClass(E entity) {
		ObjectWrapperFactory<E> wf = getObjectWrapperFactory((Class<E>) entity
				.getClass());
		E obj = wf.isWrapped(entity) ? wf.unwrap(entity) : entity;
		return (Class<E>) obj.getClass();
	}

	private <E> Set<String> getRequiredIndexedFields(
			EntityMetadata<E> entityMetadata) {
		Set<String> requiredIndexes = new HashSet<>();

		for (IndexMetadata<E> imd : entityMetadata.getIndexes()) {
			requiredIndexes.addAll(Collections2.transform(
					Arrays.asList(imd.getField()),
					new Function<FieldMetadata<E, ? extends Object, ? extends Object>, String>() {
						@Override
						public String apply(
								FieldMetadata<E, ? extends Object, ? extends Object> fmd) {
							return fmd.getColumn();
						}
					}));
		}

		return requiredIndexes;
	}

	// package protected for testing
	Cluster getCluster() {
		return cluster;
	}
	
}
