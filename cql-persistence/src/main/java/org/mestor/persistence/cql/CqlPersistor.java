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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.mestor.context.EntityContext;
import org.mestor.context.Persistor;
import org.mestor.metadata.EntityMetadata;
import org.mestor.metadata.FieldMetadata;
import org.mestor.metadata.IndexMetadata;
import org.mestor.metadata.jpa.BeanMetadataFactory;
import org.mestor.persistence.cql.CqlPersistorProperties.ThrowOnViolation;
import org.mestor.persistence.cql.management.AlterTable;
import org.mestor.persistence.cql.management.CommandBuilder;
import org.mestor.persistence.cql.management.CommandHelper;
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
import com.google.common.collect.Iterables;
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

    
    

	@SuppressWarnings({ "cast", "unchecked" })
	public CqlPersistor(EntityContext context) throws IOException {
    	if (context == null) {
    		throw new IllegalArgumentException(EntityContext.class.getSimpleName() + " cannot be null");
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
		connect(clusterBuilder, hosts, (Map<String, Object>)CqlPersistorProperties.CASSANDRA_KEYSPACE_PROPERTIES.getValue(properties));
    }
    
    
    
	
	private void connect(Cluster.Builder clusterBuilder, String[] hosts, Map<String, Object> keyspaceProperties) throws IOException {
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
		
		Insert insert = insertInto(keyspace, table);
		
		for (FieldMetadata<E, ?> fmd : emd.getFields().values()) {
			String name = fmd.getName();
			Object value = fmd.getAccessor().getValue(entity);
			insert.value(name, value);
		}
		
		session.execute(insert);
		
		//TODO implement cascade here 
	}

	@Override
	public <E, P> E fetch(Class<E> clazz, P primaryKey) {
		EntityMetadata<E> emd = context.getEntityMetadata(clazz);
		Iterator<Map<String, Object>> result = doSelect(select().all(), emd, primaryKey).iterator();
		
		if (!result.hasNext()) {
			return null;
		}
		
		Map<String, Object> data = result.next();
		// TODO: should we throw exception if more than one record were returned?
		
		E entity;
		try {
			entity = clazz.newInstance();
		} catch (ReflectiveOperationException e) {
			throw new IllegalArgumentException("Class " + clazz + " must have accessible default constructor");
		}
		for (Entry<String, Object> f : data.entrySet()) {
			String name = f.getKey();
			Object value = f.getValue();
			emd.getField(name).getAccessor().setValue(entity, value);
		}
		
		@SuppressWarnings("unchecked") //TODO why casting is required here? Generics should work but they do not...
		E wrappedEntity = (E)getObjectWrapperFactory().wrap(entity);
		return wrappedEntity;
	}
	

	@Override
	public <E> void remove(E entity) {
		Class<E> clazz = getEntityClass(entity);
		EntityMetadata<E> emd = context.getEntityMetadata(clazz);
		String table = emd.getTableName();
		String keyspace = emd.getSchemaName();
		Delete.Where where = QueryBuilder.delete().from(keyspace, table).where();
		
		Object primaryKey = emd.getPrimaryKey().getAccessor().getValue(entity);
		
		for (Clause clause : getPrimaryKeyClause(emd, primaryKey)) {
			where.and(clause);
		}
	
		session.execute(where);
		//TODO add support of cascade here. 
	}
	
	
	private <E, P> Iterable<Clause> getPrimaryKeyClause(EntityMetadata<E> emd, P primaryKey) {
		@SuppressWarnings("unchecked")
		Class<P> pkType = (Class<P>)primaryKey.getClass();
		
		Collection<Clause> clauses = new ArrayList<>();
		
		if (CommandHelper.toCassandraType(pkType) == null) {
			// This is not cassandra basic type. Therefore this is custom type that should be separated field-by-field
			// Iterate over all fields and check which of them is included into composite primary key.
			// TODO cache this metadata too.
			BeanMetadataFactory f = new BeanMetadataFactory();
			EntityMetadata<P> pkMetadata = f.create(pkType);
			for (FieldMetadata<E, Object> fmd : emd.getFields().values()) {
				if (fmd.isKey()) {
					Object value = pkMetadata.getField(fmd.getName()).getAccessor().getValue(primaryKey);
					clauses.add(eq(fmd.getColumn(), value));
				}
			}
		} else {
			String pkName = emd.getPrimaryKey().getColumn();
			clauses.add(eq(pkName, primaryKey));
		}

		return clauses;
	}

	@Override
	public <E> boolean exists(Class<E> entityClass, Object primaryKey) {
		EntityMetadata<E> emd = context.getEntityMetadata(entityClass);
		return doSelect(select().countAll(), emd, primaryKey).iterator().hasNext();
	}
	
	private <E, P> Iterable<Map<String, Object>> doSelect(Select.Builder queryBuilder, EntityMetadata<E> emd, P primaryKey) {
		String table = emd.getTableName();
		String keyspace = emd.getSchemaName();
		
		Where where = select().all().from(keyspace, table).allowFiltering().where();
		
		for (Clause clause : getPrimaryKeyClause(emd, primaryKey)) {
			where.and(clause);
		}
		
		return execute(where, emd.getFieldTypes());
	}
	
	

	@Override
	public <E> Object[] fetchProperty(E entity, String... propertyNames) {
		Class<E> clazz = getEntityClass(entity);
		EntityMetadata<E> emd = context.getEntityMetadata(clazz);
		Object primaryKey = emd.getPrimaryKey().getAccessor().getValue(entity);
		return Iterables.toArray(doSelect(select(propertyNames), emd, primaryKey), Object.class);
	}


	@Override
	public <T> ObjectWrapperFactory<T> getObjectWrapperFactory() {
		return new JavassistObjectWrapperFactory<T>(context);
	}


	@Override
	public void createSchema(String name, Map<String, Object> properties) {
		session.execute(createKeyspace().named(name).with(properties == null ? defaultKeyspaceProperties : properties));
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
	public <E> void createTable(EntityMetadata<E> entityMetadata, Map<String, Object> properties) {
		createTable(sessionHandler, entityMetadata, properties);
	}

	
	
	private <E> void createTable(Function<Query, Void> queryHandler, EntityMetadata<E> entityMetadata, Map<String, Object> properties) {
		final CreateTable table = CommandBuilder.createTable().named(entityMetadata.getTableName()).in(entityMetadata.getSchemaName()).with(properties);
		
		final Collection<String> indexedColumns = getIndexedColumns(entityMetadata);
		for (FieldMetadata<E, ?> fmd : entityMetadata.getFields().values()) {
			table.add(fmd.getColumn(), fmd.getType(), getFieldAttributes(fmd, indexedColumns));
		}
		
		queryHandler.apply(table);
		
		final String keyspaceName = entityMetadata.getSchemaName();
		final String tableName = entityMetadata.getTableName();
		
		useSchema(keyspaceName);
		for (String column :  getRequiredIndexedFields(entityMetadata)) {
			queryHandler.apply(CommandBuilder.createIndex().in(keyspaceName).named(createIndexName(keyspaceName, tableName, column)).on(tableName).column(column));
		}
	}
	
	
	@Override
	public <E> void updateTable(EntityMetadata<E> entityMetadata, Map<String, Object> properties) {
		processTable(sessionHandler, entityMetadata, properties);
	}

	@Override
	public <E> void validateTable(EntityMetadata<E> entityMetadata, Map<String, Object> properties) {
		final Map<String, Object> allProps = CollectionUtils.merge(context.getProperties(), properties);
		
		final ThrowOnViolation onViolation = CqlPersistorProperties.SCHEMA_VALIDATION.getValue(allProps);
		
		final Function<Query, Void> validationHandler;
		final List<String> errorMessages = new ArrayList<>();
		
		
		switch(onViolation) {
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
			throw new IllegalStateException(Joiner.on(System.getProperty("line.separator")).join(errorMessages));
		}
	}
	
	
	@Override
	public <E> void dropTable(String keyspace, String tableName) {
		session.execute(CommandBuilder.dropTable().named(tableName).in(keyspace));
	}

	@Override
	public Iterable<String> getTableNames(String keyspace) {
		return Collections2.transform(cluster.getMetadata().getKeyspace(keyspace).getTables(), new Function<TableMetadata, String>() {
			@Override
			public String apply(TableMetadata table) {
				return table.getName();
			}
		});
	}

	
//	private <T> Iterable<T> selectSingleColumn(String keyspace, String table, String columnName, Class<T> columnType) {
//		return execute(select().column(columnName).from(keyspace, table), columnName, columnType);
//	}
	
//	private <T> Iterable<T> execute(final Query query, final String name, Class<T> type) {
//		return execute(query, name, new Class<?>[] {type});
//	}
	
//	private <T> Iterable<T> execute(final Query query, final String name, Class<?>[] types) {
//		return transform(session.execute(query), new RowFieldExtractor<T>(name, types));
//	}
	
	
	private Iterable<Map<String, Object>> execute(final Query query, final Map<String, Class<?>[]> fields) {
		return transform(session.execute(query), new RowSplitter(fields));
	}

	private <E> FieldAttribute[] getFieldAttributes(FieldMetadata<E, ?> fmd, Collection<String> indexedColumns) {
		Collection<FieldAttribute> attrs = new ArrayList<>();
		if (fmd.isKey()) {
			attrs.add(FieldAttribute.PRIMARY_KEY);
		}
		if (indexedColumns.contains(fmd.getColumn())) {
			attrs.add(FieldAttribute.INDEX);
		}
		return attrs.toArray(new FieldAttribute[0]);
	}
	
	private <E> Collection<String> getIndexedColumns(EntityMetadata<E> entityMetadata) {
		Collection<String> indexedColumns = new LinkedHashSet<String>();
		for (IndexMetadata<E> imd : entityMetadata.getIndexes()) {
			for (FieldMetadata<E, ?> fmd : imd.getField()) {
				indexedColumns.add(fmd.getColumn());
			}
		}
		return indexedColumns;
	}
	
	
	private <E> void processTable(Function<Query, Void> queryHandler, EntityMetadata<E> entityMetadata, Map<String, Object> properties) {
		String entityTable = entityMetadata.getTableName();
		String existingTableName = null;
		for(String tableName : getTableNames(entityMetadata.getSchemaName())) {
			if (entityTable.equalsIgnoreCase(tableName)) {
				existingTableName = tableName;
			}
		}
		
		if (existingTableName == null) {
			createTable(queryHandler, entityMetadata, properties);
			return;
		}
		
		// table already exists
		
		
		TableMetadata existingTable = cluster.getMetadata().getKeyspace(entityMetadata.getSchemaName()).getTable(existingTableName);
		
		
		Map<String, ColumnMetadata> existingColumns = new LinkedHashMap<>();
		for (ColumnMetadata cmd : existingTable.getColumns()) {
			existingColumns.put(cmd.getName(), cmd);
		}

		Collection<String> indexedColumns = getIndexedColumns(entityMetadata);
		
		// find fields that have to be added or altered
		for (FieldMetadata<E, ?> fmd : entityMetadata.getFields().values()) {
			//AlterTable table = CommandBuilder.alterTable().named(entityMetadata.getTableName()).in(entityMetadata.getSchemaName()).with(properties);
			String column = fmd.getColumn();
			ColumnMetadata existingColumn = existingColumns.get(column); 
			if (existingColumn == null) {
				handle(queryHandler, CommandBuilder.alterTable().addColumn(column, fmd.getType()), entityMetadata, properties);
				continue;
			}
			
			Class<?> existingType = existingColumn.getType().asJavaClass();
			Class<?> fieldType = fmd.getType();
			
			boolean existingColumnIsIndexed = existingColumn.getIndex() != null;
			boolean fieldIsIndexed = indexedColumns.contains(fmd.getColumn());
			
			
			if (!existingType.isAssignableFrom(fieldType) || existingColumnIsIndexed != fieldIsIndexed) {
				handle(queryHandler, CommandBuilder.alterTable().alterColumn(column, fmd.getType()), entityMetadata, properties);
				continue;
			}
		}
		
		// find fields that should be dropped
		// TODO: not sure that this should be always done. The "extra" fields may contain data.
		// This code is currently commented out for 2 reasons. 
		// 1. attempt to drop column from table rises error message (even manually). Questions are asked at SO and datastax forum
		// 		http://stackoverflow.com/questions/18842933/failed-to-drop-column-in-cassandra-using-cql
		// 		http://www.datastax.com/support-forums/topic/failed-to-drop-column?replies=1#post-16148
		// 2. Even if it works it is a BIG question whether we should drop columns that could contain data. Probably we should do it
		//	  only if this column is empty for all records.  
		/*
		for (ColumnMetadata cmd : existingTable.getColumns()) {
			if(entityMetadata.getField(cmd.getName()) == null) {
				queryHandler.apply(CommandBuilder.alterTable().dropColumn(cmd.getName()).named(entityMetadata.getTableName()).in(entityMetadata.getSchemaName()).with(properties));
			}
		}
		*/

		// process indexes
		String keyspaceName = entityMetadata.getSchemaName();
		String tableName = entityMetadata.getTableName();
		
		Set<String> requiredIndexes = getRequiredIndexedFields(entityMetadata);
		
		Set<String> existingIndexes = new HashSet<>(Collections2.transform(
				Collections2.filter(existingTable.getColumns(), new Predicate<ColumnMetadata>() {
					  @Override
					public boolean apply(ColumnMetadata cmd) {
						  return cmd.getIndex() != null;
					  }
				}), 
				new Function<ColumnMetadata, String>() {
					@Override
					public String apply(ColumnMetadata cmd) {
						return cmd.getName();
					}
				}));
		
		
		// find missing indexes
		Set<String> missingIndexes  = new HashSet<>(requiredIndexes);
		missingIndexes.removeAll(existingIndexes);
		
		for (String column : missingIndexes) {
			queryHandler.apply(CommandBuilder.createIndex().in(keyspaceName).named(createIndexName(keyspaceName, tableName, column)).on(tableName).column(column));
		}
		
		
		// find redundant indexes
		Set<String> redundantIndexes  = new HashSet<>(existingIndexes);
		redundantIndexes.removeAll(requiredIndexes);
		
		
		for (String column : redundantIndexes) {
			queryHandler.apply(CommandBuilder.dropIndex().named(createIndexName(keyspaceName, tableName, column)));
		}
		
	}
	
	
	
	private <E> void handle(Function<Query, Void> queryHandler, AlterTable command, EntityMetadata<E> entityMetadata, Map<String, Object> properties) {
		queryHandler.apply(command.named(entityMetadata.getTableName()).in(entityMetadata.getSchemaName()).with(properties));
	}
	
	/**
	 * Creates unique index name using format {@code KEYSPACE_TABLE_COLUMN_index}.
	 * 
	 * The index name is global in whole Cassandra server, so we have to use keyspace, 
	 * table name and column name when creating index name to avoid conflicts.
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
		return (Class<E>)getObjectWrapperFactory().unwrap(entity).getClass();
	}
	
	
	private <E> Set<String> getRequiredIndexedFields(EntityMetadata<E> entityMetadata) {
		Set<String> requiredIndexes = new HashSet<>();
		
		for(IndexMetadata<E> imd : entityMetadata.getIndexes()) {
			requiredIndexes.addAll(Collections2.transform(Arrays.asList(imd.getField()), new Function<FieldMetadata<E, ? extends Object>, String>() {
				@Override
				public String apply(FieldMetadata<E, ? extends Object> fmd) {
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
