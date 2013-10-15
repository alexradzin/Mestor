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
import java.util.HashMap;
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
import org.mestor.reflection.ClassAccessor;
import org.mestor.util.CollectionUtils;
import org.mestor.wrap.ObjectWrapperFactory;
import org.mestor.wrap.javassist.JavassistObjectWrapperFactory;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ColumnMetadata;
import com.datastax.driver.core.KeyspaceMetadata;
import com.datastax.driver.core.Query;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
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
	private final EntityContext context;
	private Cluster cluster;
	private Session session;

	private Map<String, Object> defaultKeyspaceProperties;
	

	private final Function<Query, Void> sessionHandler = new Function<Query, Void>() {
		@Override
		public Void apply(final Query query) {
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
	public CqlPersistor(final EntityContext context) throws IOException {
		if (context == null) {
			throw new IllegalArgumentException(
					EntityContext.class.getSimpleName() + " cannot be null");
		}
		this.context = context;

		final Map<String, Object> properties = context.getProperties();
		// set defaults
		final Integer port = CqlPersistorProperties.CASSANDRA_PORT.getValue(properties);
		final String[] hosts = CqlPersistorProperties.CASSANDRA_HOSTS.getValue(properties);

		final Cluster.Builder clusterBuilder = Cluster.builder();
		if (port != null) {
			clusterBuilder.withPort(port);
		}
		connect(clusterBuilder,
				hosts,
				CqlPersistorProperties.CASSANDRA_KEYSPACE_PROPERTIES
						.getValue(properties));
	}

	private void connect(final Cluster.Builder clusterBuilder, final String[] hosts,
			final Map<String, Object> keyspaceProperties) throws IOException {
		try {
			cluster = clusterBuilder.addContactPoints(hosts).build();
			session = cluster.connect();
			defaultKeyspaceProperties = keyspaceProperties;
		} catch (final NoHostAvailableException e) {
			throw new IOException(e);
		}
	}

	
	@Override
	public <E> void store(final E entity) {
		for (final EntityMetadata<?> emd : getDownUpHierarchy(context.getEntityMetadata(getEntityClass(entity))).values()) {
			@SuppressWarnings("unchecked")
			final
			EntityMetadata<E> meta = (EntityMetadata<E>)emd;
			storeImpl(meta, entity);
			if (meta.getJoiner() == null) {
				break;
			}
		}
	}
	
	
	private <E> void storeImpl(final EntityMetadata<E> emd, final E entity) {
		final String table = emd.getTableName();
		final String keyspace = emd.getSchemaName();

		final Insert insert = insertInto(quote(keyspace), quote(table));
		
		
		Collection<FieldMetadata<E, Object, Object>> allFields = emd.getFields();
		
		final FieldMetadata<E, Object, Object> pkmd = emd.getPrimaryKey();
		if (pkmd != null) {
			final String pkColumn = pkmd.getColumn();
			if (emd.getField(pkColumn) == null) {
				allFields = new ArrayList<>(allFields);
				allFields.add(pkmd);
			}
		}
		
		for (final FieldMetadata<E, Object, ?> fmd : allFields) {
			String name = fmd.getColumn();
			if (name == null) {
				if(fmd.isDiscriminator()) {
					// find column name in parent class
					for (Class<?> clazz = emd.getEntityType().getSuperclass(); !Object.class.equals(clazz); clazz = clazz.getSuperclass()) {
						final EntityMetadata<?> meta = context.getEntityMetadata(clazz);
						if (meta == null) {
							continue;
						}
						if (!table.equals(meta.getTableName())) {
							break;
						}
						final FieldMetadata<?, ?, ?> discriminator = meta.getDiscrimintor();
						if (discriminator == null) {
							continue;
						}
						final String discriminatorColumn = discriminator.getColumn();
						if (discriminatorColumn != null) {
							name = discriminatorColumn;
							break;
						}
					}
				}
			}
			if (name == null) {
				continue;
			}
			if (!belongsTo(fmd, entity)) {
				// Ignore this fields since it arrived from other class. 
				// This is a typical case of entities inheritance with single or joined table.
				continue;
			}
			final Object value = convertValue(fmd, transformValue(fmd.getAccessor().getValue(entity)), new PushmiPullyuConverter() {
				@Override
				public Object convert(final ValueConverter<Object, Object> conveter, final Object v) {
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
	 * Finds all super classes of given class and creates map between table name and {@link EntityMetadata} of
	 * corresponding class. This means that the result contains entries only for different tables. 
	 * If for example hierarchy is stored in one table the result will contain one entry only.
	 * Each entry shell contain metadata of class lowest in the hierarchy.
	 * The map is sorted from bottom to top. 
	 * @param leafMeta
	 * @return table name to entity metadata map of hierarchy
	 */
	private <E> Map<String, EntityMetadata<?>> getDownUpHierarchy(final EntityMetadata<E> leafMeta) {
		final Map<String, EntityMetadata<?>> table2emd = new LinkedHashMap<>();
		for (Class<?> clazz = leafMeta.getEntityType(); clazz != null; clazz = clazz.getSuperclass()) {
			final EntityMetadata<?> emd = context.getEntityMetadata(clazz);
			if (emd == null) {
				continue;
			}
			final String table = emd.getTableName();
			if (table2emd.containsKey(table)) {
				continue;
			}
			// this is the first appearance of this table
			table2emd.put(emd.getTableName(), emd);
		}
		return table2emd;
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
	private <E> Object convertValue(final FieldMetadata<E, Object, ?> fmd, final Object obj, final PushmiPullyuConverter pushmiPullyu) {
		Object result = obj;
		
		
		if (obj instanceof Collection) {
			@SuppressWarnings("unchecked")
			final
			Collection<Object> collection = (Collection<Object>)obj;
			
			final Collection<Object> resCollection;
			if (obj instanceof List) {
				resCollection = new ArrayList<Object>();
			} else if (obj instanceof Set) {
				resCollection = new LinkedHashSet<Object>(); //LinkedHashSet to preserve the order just in case  
			} else {
				throw new IllegalArgumentException("Only List or Set are supported, got " + obj.getClass());
			}
			
			final ValueConverter<Object, Object> elementConverter = fmd.getConverter(1);
			for (final Object element : collection) {
				resCollection.add(pushmiPullyu.convert(elementConverter, element));
			}
			result = resCollection;
		} else if (obj instanceof Map) {
			@SuppressWarnings({ "rawtypes", "unchecked" })
			final Map<Object, Object> map = (Map)obj;
			final Map<Object, Object> resMap = new LinkedHashMap<>();
			
			final ValueConverter<Object, Object> keyConverter = fmd.getConverter(1);
			final ValueConverter<Object, Object> valueConverter = fmd.getConverter(2);
			
			for (final Entry<Object, Object> entry : map.entrySet()) {
				final Object key = entry.getKey();
				final Object value = entry.getValue();
				resMap.put(pushmiPullyu.convert(keyConverter, key), pushmiPullyu.convert(valueConverter, value));
			}
			
			result = resMap;
		}

		
		return pushmiPullyu.convert(fmd.getConverter(0), result);
	}
	
	
	
	private Object transformValue(final Object value) {
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
			final int n = Array.getLength(value);
			final List<Object> list = new ArrayList<Object>();
			for (int i = 0; i < n; i++) {
				list.add(Array.get(value, i));
			}
			return list;
		}
		return value;
	}


    @Override
	public <T, P> T fetch(final Class<T> entityClass, final Map<String, Object> criteria) {
		throw new UnsupportedOperationException("This method is not implemented yet");
	}
	
	
    @Override
    public <E, P> E fetch(final Class<E> clazz, final P primaryKey) {
        final EntityMetadata<E> requestedEntityMetadata = context.getEntityMetadata(clazz);
        EntityMetadata<E> emd = requestedEntityMetadata;

        Map<String, Class<?>[]> fieldTypes = new HashMap<>();
        Select.Selection select = selectFields(emd, fieldTypes);
        final List<Row> allResult = doSelectAll(emd, primaryKey).all();
        
        Iterator<Map<String, Object>> result = transform(allResult, new RowSplitter(fieldTypes)).iterator();
        
        if (!result.hasNext()) {
        	final String requestedEntityPkFieldName = emd.getPrimaryKey().getName();
        	for (final EntityMetadata<?> e : context.getEntityMetadata()) {
        		final Class<?> c = e.getEntityType();
        		if (clazz.isAssignableFrom(c) && !clazz.equals(c) && e.getFieldByName(emd.getPrimaryKey().getName()) != null) {
        	        fieldTypes = new HashMap<>();
        	        select = selectFields(e, fieldTypes);

        	        final String column = e.getFieldByName(requestedEntityPkFieldName).getColumn();
        	        
        			result = doSelect(select, e, column, primaryKey, fieldTypes).iterator();
                	if (result.hasNext()) {
            			@SuppressWarnings("unchecked")
						final
    					EntityMetadata<E> e2 = (EntityMetadata<E>)e;
                		emd = e2;
                		break;
                	}
        		}
        	}
        	
        	if (!result.hasNext()) {
        		return null;
        	}
        }

        Map<String, Object> data = column2field(emd, result.next());
        // TODO: should we throw exception if more than one record were
        // returned?

        
        
        final Class<? extends E> leafClazz = getRealClass(emd, data);
        final E entity = ClassAccessor.newInstance(leafClazz);
        
        if (!leafClazz.equals(clazz)) {
            @SuppressWarnings("unchecked")
			final
			EntityMetadata<E> leafMeta = (EntityMetadata<E>)context.getEntityMetadata(leafClazz);
            final Map<String, Class<?>[]> leafFieldTypes = new HashMap<>();
            selectFields(leafMeta, leafFieldTypes);
            
            
            final Map<String, Class<?>[]> allFieldTypes = new HashMap<>(fieldTypes);
            fieldTypes.putAll(leafFieldTypes);
            
            if (!fieldTypes.equals(leafFieldTypes)) {
	            result = transform(allResult, new RowSplitter(allFieldTypes)).iterator();
	            data = column2field(leafMeta, result.next());
	            emd = leafMeta;
            }
        }
        
        
        // set primary key. If we are here the primary key definitely equals to one that is 
        // in persisted entity.
        requestedEntityMetadata.getPrimaryKey().getAccessor().setValue(entity, primaryKey);
        
        
        final EntityMetadata<? extends E> leafMeta = context.getEntityMetadata(leafClazz);
        final Map<String, EntityMetadata<?>> downUpHierarchy = getDownUpHierarchy(leafMeta);

        
        final List<EntityMetadata<?>> downUpChain = new ArrayList<>();
        final List<EntityMetadata<?>> upDownChain = new ArrayList<>();

        final Iterator<EntityMetadata<?>> ihierarchy = downUpHierarchy.values().iterator();
        
        while(ihierarchy.hasNext()) {
        	final EntityMetadata<?> e = ihierarchy.next();
        	if (clazz.equals(e.getEntityType())) {
        		break;
        	}
        	upDownChain.add(e);
        }
        Collections.reverse(upDownChain);

        while(ihierarchy.hasNext()) {
        	final EntityMetadata<?> e = ihierarchy.next();
        	downUpChain.add(e);
        }        
        
        
        if (!downUpChain.isEmpty() && getJoinerColumn(emd) != null) {
	        Object pk = data.get(emd.getJoiner().getName());
	        for (final EntityMetadata<?> e : downUpChain) {
	            fieldTypes = new HashMap<>();
	            select = selectFields(e, fieldTypes);
	            result = doSelect(select, e, pk, fieldTypes).iterator();
	            
	            if (!result.hasNext()) {
	            	break;
	            }
	            
	            final Map<String, Object> record = result.next(); 
	            data.putAll(column2field(e, record));
	            
	            if (e.getJoiner() == null) {
	            	break;
	            }
	            
	            pk = record.get(e.getJoiner().getColumn());
	        }
        }

        Object key = primaryKey;
        for (final EntityMetadata<?> e : upDownChain) {
            fieldTypes = new HashMap<>();
            select = selectFields(e, fieldTypes);
            final String jc = getJoinerColumn(e);
            if (jc == null) {
            	break;
            }

            result = doSelect(select, e, jc, key, fieldTypes).iterator();
            
            if (!result.hasNext()) {
            	break;
            }
            
            final Map<String, Object> record = result.next(); 
            data.putAll(column2field(e, record));
            
            key = record.get(e.getJoiner().getColumn());
        }
        


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
        	populateData(entity, emd, data);
        } finally {
            if (fetchContextOriginator) {
            	fetchContext.remove();
            }
        }

        return getObjectWrapperFactory(clazz).wrap(entity);
    }

    
	private <E> String getJoinerColumn(final EntityMetadata<E> emd) {
        final FieldMetadata<?, ?, ?> joiner = emd.getJoiner();
        return joiner == null ? null : joiner.getColumn(); 
	}
    
    private <E>  Select.Selection selectFields(final EntityMetadata<E> emd, final Map<String, Class<?>[]> fieldTypes) {
		return selectFor(
				emd, 
				new Predicate<FieldMetadata<E, Object, Object>>(){
					@Override
					public boolean apply(final FieldMetadata<E, Object, Object> fmd) {
						return fmd.getColumn() != null && !fmd.isLazy();
					}
				}, 
				fieldTypes);
    }
    
	
	
	
	

	private <E> Map<String, Object> column2field(final EntityMetadata<E> emd, final Map<String, Object> data) {
		final Map<String, Object> field2value = new HashMap<>();
		
		for (final Entry<String, Object> col2val : data.entrySet()) {
			final String column = col2val.getKey();
			
			String field = findFieldNameByColumn(emd, column);
			
			if(field == null) {
				field = column;
			}
			field2value.put(field, col2val.getValue());
		}
		
		return field2value;
	}
	
	
	
	private <E> void populateData(final E entity, final EntityMetadata<E> emd, final Map<String, Object> data) {
		for (final Entry<String, Object> d : data.entrySet()) {
			final String fieldName = d.getKey();
			final Object columnValue = d.getValue();
			final FieldMetadata<E, Object, Object> fmd = findFieldMetadata(emd, fieldName);
			
			
			if (!belongsTo(fmd, entity) && isDefaultValue(fmd, columnValue)) {
				// Field metadata does not not belong to current entity. This can happen when working with single table, 
				// i.e. when different types a stored in one table. This is valid only if columnValue is null. 
				// Otherwise IllegalArgumentException will be thrown when trying to set value.
				continue;
			}
			
			
			final Object fieldValue = convertValue(fmd, columnValue, new PushmiPullyuConverter() {
				@Override
				public Object convert(final ValueConverter<Object, Object> conveter, final Object v) {
					return conveter.fromColumn(v);
				}
			});
			
			
			fmd.getAccessor().setValue(entity, fieldValue);
		}
	}
	
	/**
	 * Retrieves field metadata even if it is defined in super class' entity metadata. 
	 * @param emd
	 * @param name - either field or column name
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private <E> FieldMetadata<E, Object, Object> findFieldMetadata(final EntityMetadata<E> entityMetadata, final String name) {
		EntityMetadata<E> emd = entityMetadata;
		for (Class<?> clazz = emd.getEntityType(); !Object.class.equals(clazz); clazz = clazz.getSuperclass(), emd = (EntityMetadata<E>)context.getEntityMetadata(clazz)) {
			if (emd == null) {
				continue;
			}
			FieldMetadata<E, Object, Object> fmd = emd.getFieldByName(name);
			if (fmd == null) {
				// try to get metdata by column name for virtual columns that are not represented in entity
				// for example for discrimintator
				fmd = emd.getField(name); 
			}
			if (fmd != null) {
				return fmd;
			}
		}

		return null;
	}
	
	@SuppressWarnings("unchecked")
	private <E> String findFieldNameByColumn(final EntityMetadata<E> entityMetadata, final String column) {
		EntityMetadata<E> emd = entityMetadata;
		for (Class<?> clazz = emd.getEntityType(); !Object.class.equals(clazz); clazz = clazz.getSuperclass(), emd = (EntityMetadata<E>)context.getEntityMetadata(clazz)) {
			if (emd == null) {
				continue;
			}
			final FieldMetadata<E, Object, Object> fmd = emd.getField(column); 
			if (fmd != null) {
				return fmd.getName();
			}
		}

		return null;
	}
	
	private <E> Class<? extends E> getRealClass(final EntityMetadata<E> emd, final Map<String, Object> data) {
		final FieldMetadata<E, ?, ?> dmd = emd.getDiscrimintor();
		if (dmd == null) {
			return emd.getEntityType();
		}
		
		final String discriminatorColumn = dmd.getColumn();
		final Object discriminatorValue = data.get(discriminatorColumn);
		if (discriminatorValue == null) {
			return emd.getEntityType();
		}
		
		for (final EntityMetadata<?> e : context.getEntityMetadata()) {
			if (emd.getEntityType().isAssignableFrom(e.getEntityType()) && discriminatorValue.equals(e.getDiscrimintor().getDefaultValue())) {
				@SuppressWarnings("unchecked")
				final
				Class<? extends E> c =  (Class<? extends E>)e.getEntityType();
				return c;
			}
		}
		
		return emd.getEntityType();
	}


	@Override
	public <E> void remove(final E entity) {
		final Class<E> clazz = getEntityClass(entity);
		final EntityMetadata<E> emd = context.getEntityMetadata(clazz);
		final String table = emd.getTableName();
		final String keyspace = emd.getSchemaName();
		final Delete.Where where = QueryBuilder.delete()
				.from(quote(keyspace), quote(table)).where();

		final Object primaryKey = emd.getPrimaryKey().getAccessor().getValue(entity);

		for (final Clause clause : getPrimaryKeyClause(emd, primaryKey)) {
			where.and(clause);
		}

		session.execute(where);
		// TODO add support of cascade here.
	}

	
	private <E, P> Iterable<Clause> getPrimaryKeyClause(final EntityMetadata<E> entityMetadata, final P primaryKey) {
		final EntityMetadata<E> emd = entityMetadata;
		final FieldMetadata<E, Object, Object> dmd = emd.getDiscrimintor();
		
		boolean discriminator = false;
		final Collection<Clause> clause = new ArrayList<>();
		if (dmd != null && dmd.getColumn() != null) {
			final String discriminatorColumn = dmd.getColumn();
			final Object discriminatorValue = dmd.getDefaultValue();
			if (discriminatorValue != null && !isPrimaryKey(emd.getSchemaName(), emd.getTableName(), emd.getPrimaryKey().getColumn())) {
				clause.add(eq(quote(discriminatorColumn), discriminatorValue));
				discriminator = true;
			}
		}
		
		if (!discriminator) {
			final FieldMetadata<E, Object, Object> jmd = emd.getJoiner();
			if (jmd != null) {
				final String joinerColumn = jmd.getColumn();
				clause.add(eq(quote(joinerColumn), primaryKey));
			} 
		}
		
		
		
		final FieldMetadata<E, Object, Object> pkmd = emd.getPrimaryKey();
		final Object pkColumnValue = pkmd.getConverter().toColumn(primaryKey);
		final String pkName = emd.getPrimaryKey().getColumn();
		clause.add(eq(quote(pkName), pkColumnValue));
		return clause;
	}

	private boolean isPrimaryKey(final String keyspace, final String table, final String column) {
		for (final ColumnMetadata cmd : cluster.getMetadata().getKeyspace(keyspace).getTable(table).getPrimaryKey()) {
			if (column.equals(cmd.getName())) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public <E> boolean exists(final Class<E> entityClass, final Object primaryKey) {
		return count(entityClass, primaryKey).signum() == 1;
	}

	private <E> BigInteger count(final Class<E> entityClass, final Object primaryKey) {
		final EntityMetadata<E> emd = context.getEntityMetadata(entityClass);
		return (BigInteger)doSelect(
				select().countAll(),
				emd,
				primaryKey,
				Collections.<String, Class<?>[]> singletonMap("count", new Class[] { BigInteger.class })).
					iterator().next().get("count");
	}
	
	
	
	
	private <E> Select.Selection selectFor(final EntityMetadata<E> emd, final Predicate<FieldMetadata<E, Object, Object>> filter, final Map<String, Class<?>[]> fieldTypes) {
		return selectFor(emd, Collections2.filter(emd.getFields(), filter), fieldTypes);
	}
	
	
	private <E> Select.Selection selectFor(final EntityMetadata<E> emd, final Collection<FieldMetadata<E, Object, Object>> fields, final Map<String, Class<?>[]> fieldTypes) {
		final Select.Selection select = select();
		
		for (final FieldMetadata<E, ?, ?> fmd : fields) {
			final String column = fmd.getColumn();
			if (column != null) {
				select.column(quote(column));
				fieldTypes.put(fmd.getColumn(), emd.getColumnTypes(fmd.getColumn()));
			}
		}
		
		return select;
	}
	

	
	private <E, P> Iterable<Map<String, Object>> doSelect(
			final Select.Builder queryBuilder, 
			final EntityMetadata<E> emd,
			final P pk,
			final Map<String, Class<?>[]> fields) {
		return doSelect(queryBuilder, emd, emd.getPrimaryKey().getColumn(), pk, fields);
	}
	
	
	private <E, P> Iterable<Map<String, Object>> doSelect(
			final Select.Builder queryBuilder, 
			final EntityMetadata<E> emd,
			final String column,
			final P value,
			final Map<String, Class<?>[]> fields) {
		final String table = emd.getTableName();
		final String keyspace = emd.getSchemaName();

		final Where where = queryBuilder.from(quote(keyspace), quote(table)).allowFiltering().where();
		FieldMetadata<E, Object, Object> fmd = emd.getField(column);
		if (fmd == null) {
			fmd = emd.getPrimaryKey();
		}
		final Object columnValue = fmd.getConverter().toColumn(value);
		where.and(eq(quote(column), columnValue));

		return execute(where, fields);
	}

	
	private <E, P> ResultSet doSelectAll(final EntityMetadata<E> emd, final P pkValue) {
		final String table = emd.getTableName();
		final String keyspace = emd.getSchemaName();

		final Where where = select().all().from(quote(keyspace), quote(table)).allowFiltering().where();
		final FieldMetadata<E, Object, Object> fmd = emd.getPrimaryKey();
		final String column = fmd.getColumn();
		final Object columnValue = fmd.getConverter().toColumn(pkValue);
		where.and(eq(quote(column), columnValue));

		
		return session.execute(where);
	}
	
	
	
	@Override
	public <E> Object[] fetchProperty(final E entity, final String... propertyNames) {
		final Class<E> clazz = getEntityClass(entity);
		final EntityMetadata<E> emd = context.getEntityMetadata(clazz);
		final Object primaryKey = emd.getPrimaryKey().getAccessor().getValue(entity);

		final Set<String> propertyNamesSet = new HashSet<>(Arrays.asList(propertyNames));

		
		final Map<String, Class<?>[]> columnTypes = new LinkedHashMap<String, Class<?>[]>();
		final Select.Selection select = selectFor(
				emd, 
				new Predicate<FieldMetadata<E, Object, Object>>() {
					@Override
					public boolean apply(final FieldMetadata<E, Object, Object> fmd) {
						return propertyNamesSet.contains(fmd.getName());
					}
				}, 
				columnTypes);

		final List<String> columns = new ArrayList<>(columnTypes.keySet());
		final Object[] result = new Object[propertyNames.length];

		final Map<String, Object> rawResult = doSelect(select, emd, primaryKey, columnTypes).iterator().next();

		for (int i = 0; i < result.length; i++) {
			result[i] = rawResult.get(columns.get(i));
		}

		return result;
	}

	@Override
	public <T> ObjectWrapperFactory<T> getObjectWrapperFactory(final Class<T> type) {
		return new JavassistObjectWrapperFactory<T>(context);
	}

	@Override
	public void createSchema(final String name, final Map<String, Object> properties) {
		final Map<String, Object> props = properties == null || properties.isEmpty() ? defaultKeyspaceProperties : properties;
		session.execute(createKeyspace().named(name).with(props));
	}

	@Override
	public void dropSchema(final String name) {
		session.execute(dropKeyspace().named(name));
	}

	@Override
	public Iterable<String> getSchemaNames() {
		return Lists.transform(cluster.getMetadata().getKeyspaces(),
				new Function<KeyspaceMetadata, String>() {
					@Override
					public String apply(final KeyspaceMetadata input) {
						return input.getName();
					}
				});
	}

	@Override
	public void useSchema(final String name) {
		session.execute("USE " + name);
	}

	@Override
	public <E> void createTable(final EntityMetadata<E> entityMetadata,
			final Map<String, Object> properties) {
		createTable(sessionHandler, entityMetadata, properties);
	}

	private <E> void createTable(final Function<Query, Void> queryHandler,
			final EntityMetadata<E> entityMetadata, final Map<String, Object> properties) {
		final CreateTable table = CommandBuilder.createTable()
				.named(entityMetadata.getTableName())
				.in(entityMetadata.getSchemaName()).with(properties);

		final Collection<String> indexedColumns = getIndexedColumns(entityMetadata);
		
		final FieldMetadata<E, ?, ?> pkmd = entityMetadata.getPrimaryKey();
		if (pkmd != null) {
			final String pkColumn = pkmd.getColumn();
			if (entityMetadata.getField(pkColumn) == null) {
				addColumn(table, pkmd, indexedColumns);					
			}
		}
		
		for (final FieldMetadata<E, ?, ?> fmd : entityMetadata.getFields()) {
			addColumn(table, fmd, indexedColumns);					
		}

		queryHandler.apply(table);

		final String keyspaceName = entityMetadata.getSchemaName();
		final String tableName = entityMetadata.getTableName();

		// Index creation statement does not support specification of keyspace
		// as a part of index identifier using dot-notation.
		// So we have to specify current keyspace using "use keyspace"
		useSchema(keyspaceName);
		for (final String column : getRequiredIndexedFields(entityMetadata)) {
			queryHandler.apply(CommandBuilder.createIndex().in(keyspaceName)
					.named(createIndexName(keyspaceName, tableName, column))
					.on(tableName).column(column));
		}
	}
	
	private <E> void addColumn(final CreateTable table, final FieldMetadata<E, ?, ?> fmd, final Collection<String> indexedColumns) {
		final String column = fmd.getColumn();
		if (column == null) {
			return; // special case for fields that are not save as columns.
		}
		table.add(column, fmd.getColumnType(), fmd.getColumnGenericTypes()
				.toArray(new Class[0]),
				getFieldAttributes(fmd, indexedColumns));
	}

	@Override
	public <E> void updateTable(final EntityMetadata<E> entityMetadata,
			final Map<String, Object> properties) {
		processTable(sessionHandler, entityMetadata, properties);
	}

	@Override
	public <E> void validateTable(final EntityMetadata<E> entityMetadata,
			final Map<String, Object> properties) {
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
				public Void apply(final Query query) {
					errorMessages.add(query.toString());
					return null;
				}
			};
			break;
		case THROW_FIRST:
			validationHandler = new Function<Query, Void>() {
				@Override
				public Void apply(final Query query) {
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
	public <E> void dropTable(final String keyspace, final String tableName) {
		session.execute(CommandBuilder.dropTable().named(tableName)
				.in(keyspace));
	}

	@Override
	public Iterable<String> getTableNames(final String keyspace) {
		return Collections2.transform(
				cluster.getMetadata().getKeyspace(keyspace).getTables(),
				new Function<TableMetadata, String>() {
					@Override
					public String apply(final TableMetadata table) {
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

	private <E> FieldAttribute[] getFieldAttributes(final FieldMetadata<E, ?, ?> fmd,
			final Collection<String> indexedColumns) {
		final Collection<FieldAttribute> attrs = new ArrayList<>();
		if (fmd.isKey()) {
			attrs.add(FieldAttribute.PRIMARY_KEY);
		}
		if (indexedColumns.contains(fmd.getColumn())) {
			attrs.add(FieldAttribute.INDEX);
		}
		return attrs.toArray(new FieldAttribute[0]);
	}

	private <E> Collection<String> getIndexedColumns(
			final EntityMetadata<E> entityMetadata) {
		final Collection<String> indexedColumns = new LinkedHashSet<String>();
		for (final IndexMetadata<E> imd : entityMetadata.getIndexes()) {
			for (final FieldMetadata<E, ?, ?> fmd : imd.getField()) {
				indexedColumns.add(fmd.getColumn());
			}
		}
		return indexedColumns;
	}

	private <E> void processTable(final Function<Query, Void> queryHandler,
			final EntityMetadata<E> entityMetadata, final Map<String, Object> properties) {
		final String entityTable = entityMetadata.getTableName();
		String existingTableName = null;
		for (final String tableName : getTableNames(entityMetadata.getSchemaName())) {
			if (entityTable.equalsIgnoreCase(tableName)) {
				existingTableName = tableName;
			}
		}

		if (existingTableName == null) {
			createTable(queryHandler, entityMetadata, properties);
			return;
		}

		// table already exists

		final TableMetadata existingTable = cluster.getMetadata()
				.getKeyspace(entityMetadata.getSchemaName())
				.getTable(existingTableName);

		final Map<String, ColumnMetadata> existingColumns = new LinkedHashMap<>();
		for (final ColumnMetadata cmd : existingTable.getColumns()) {
			existingColumns.put(cmd.getName(), cmd);
		}

		final Collection<String> indexedColumns = getIndexedColumns(entityMetadata);

		// find fields that have to be added or altered
		for (final FieldMetadata<E, ?, ?> fmd : entityMetadata.getFields()) {
			// AlterTable table =
			// CommandBuilder.alterTable().named(entityMetadata.getTableName()).in(entityMetadata.getSchemaName()).with(properties);
			final String column = fmd.getColumn();
			if (column == null) {
				continue; // special case for fields that are not save as columns.
			}
			final Class<?> columnType = fmd.getColumnType();
			
			final ColumnMetadata existingColumn = existingColumns.get(column);
			if (existingColumn == null) {
				handle(queryHandler,
						CommandBuilder.alterTable().addColumn(column,
								columnType), entityMetadata, properties);
				continue;
			}

			final Class<?> existingType = existingColumn.getType().asJavaClass();

			final boolean existingColumnIsIndexed = existingColumn.getIndex() != null;
			final boolean fieldIsIndexed = indexedColumns.contains(fmd.getColumn());

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
		final String keyspaceName = entityMetadata.getSchemaName();
		final String tableName = entityMetadata.getTableName();

		final Set<String> requiredIndexes = getRequiredIndexedFields(entityMetadata);

		final Set<String> existingIndexes = new HashSet<>(Collections2.transform(
				Collections2.filter(existingTable.getColumns(),
						new Predicate<ColumnMetadata>() {
							@Override
							public boolean apply(final ColumnMetadata cmd) {
								return cmd.getIndex() != null;
							}
						}), new Function<ColumnMetadata, String>() {
					@Override
					public String apply(final ColumnMetadata cmd) {
						return cmd.getName();
					}
				}));

		// find missing indexes
		final Set<String> missingIndexes = new HashSet<>(requiredIndexes);
		missingIndexes.removeAll(existingIndexes);

		for (final String column : missingIndexes) {
			queryHandler.apply(CommandBuilder.createIndex().in(keyspaceName)
					.named(createIndexName(keyspaceName, tableName, column))
					.on(tableName).column(column));
		}

		// find redundant indexes
		final Set<String> redundantIndexes = new HashSet<>(existingIndexes);
		redundantIndexes.removeAll(requiredIndexes);

		for (final String column : redundantIndexes) {
			queryHandler.apply(CommandBuilder.dropIndex().named(
					createIndexName(keyspaceName, tableName, column)));
		}

	}

	private <E> void handle(final Function<Query, Void> queryHandler,
			final AlterTable command, final EntityMetadata<E> entityMetadata,
			final Map<String, Object> properties) {
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
	private String createIndexName(final String keyspace, final String table, final String column) {
		return Joiner.on('_').join(keyspace, table, column.replaceAll("[^a-zA-Z_]", "_"), "index");
	}

	@SuppressWarnings("unchecked")
	private <E> Class<E> getEntityClass(final E entity) {
		final ObjectWrapperFactory<E> wf = getObjectWrapperFactory((Class<E>) entity
				.getClass());
		final E obj = wf.isWrapped(entity) ? wf.unwrap(entity) : entity;
		return (Class<E>) obj.getClass();
	}

	/**
	 * Composes list of column names that must be indexed:
	 * <ul>
	 * 	<li>explicitly defined indexed</li>
	 * 	<li>discriminator column</li>
	 * 	<li>indexer column</li>
	 * 	<li>indexer column</li>
	 * 	<li>primary keys of subclasses that share the same table</li>
	 * </ul>
	 * @param entityMetadata
	 * @return
	 */
	private <E> Set<String> getRequiredIndexedFields(
			final EntityMetadata<E> entityMetadata) {
		final Set<String> requiredIndexes = new HashSet<>();

		for (final IndexMetadata<E> imd : entityMetadata.getIndexes()) {
			requiredIndexes.addAll(Collections2.transform(
					Arrays.asList(imd.getField()),
					new Function<FieldMetadata<E, ? extends Object, ? extends Object>, String>() {
						@Override
						public String apply(
								final FieldMetadata<E, ? extends Object, ? extends Object> fmd) {
							return fmd.getColumn();
						}
					}));
		}
		
		// Add indexes created for inherited entities 
		for (final FieldMetadata<E, ?, ?> fmd : entityMetadata.getFields()) {
			final String column = fmd.getColumn();
			if (column != null && (fmd.isDiscriminator() || fmd.isJoiner())) {
				requiredIndexes.add(column);
			}
		}
		
		final String tableName = entityMetadata.getTableName();
		final Class<E> clazz = entityMetadata.getEntityType();
		
		for (final EntityMetadata<?> e : context.getEntityMetadata()) {
			final Class<?> c = e.getEntityType();

			final FieldMetadata<?, ?, ?> fpk = e.getPrimaryKey();
			if (fpk == null) {
				continue;
			}
			final String pkColumn = fpk.getColumn();
			if (pkColumn == null) {
				continue;
			}
			
			// add index for PK of subclasses stored in the same table
			if (tableName.equals(e.getTableName()) && clazz.isAssignableFrom(c) && !clazz.equals(c)) {
				requiredIndexes.add(pkColumn);
			}
			
			// add indexes for PK of super classes if each class is stored in separate table
			if (c.isAssignableFrom(clazz) && !clazz.equals(c)) {
				final FieldMetadata<?, ?, ?> parentForeignKey = entityMetadata.getFieldByName(fpk.getName());
				final String fkColumn = parentForeignKey.getColumn();
				if (fkColumn == null) {
					continue;
				}
				requiredIndexes.add(pkColumn);
			}
			
		}

		
		return requiredIndexes;
	}

	// for testing
	public Cluster getCluster() {
		return cluster;
	}


	/**
	 * Checks whether given {@link FieldMetadata} belongs to given entity.
	 * @param fmd
	 * @param entity
	 * @return
	 */
	private <E> boolean belongsTo(final FieldMetadata<E, ?, ?> fmd, final E entity) {
		// TODO: should we perform some more sanity tests here based on discriminator column?
		return fmd.getClassType().isAssignableFrom(entity.getClass());
	}

	private <E, F> boolean isDefaultValue(final FieldMetadata<E, F, ?> fmd, final F value) {
		final Class<F> type = fmd.getType();
		if (!type.isPrimitive()) {
			return value == null;
		}
		// this is a primitive
		if (int.class.equals(type) || long.class.equals(type) || short.class.equals(type) || byte.class.equals(type)) {
			return ((Number)value).intValue() == 0;
		}
		if (float.class.equals(type)) {
			return ((Number)value).floatValue() == 0.0f;
		}
		if (double.class.equals(type)) {
			return ((Number)value).doubleValue() == 0.0;
		}
		if (boolean.class.equals(type)) {
			return ((Boolean)value).booleanValue() == false;
		}
		return false;
	}
}
