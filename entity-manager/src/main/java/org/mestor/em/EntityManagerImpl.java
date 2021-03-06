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

package org.mestor.em;

import java.io.Closeable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityNotFoundException;
import javax.persistence.EntityTransaction;
import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.persistence.StoredProcedureQuery;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.spi.PersistenceUnitInfo;

import org.mestor.context.DirtyEntityManager;
import org.mestor.context.EntityContext;
import org.mestor.context.Persistor;
import org.mestor.metadata.EntityMetadata;
import org.mestor.metadata.FieldMetadata;
import org.mestor.persistence.metamodel.MetamodelImpl;
import org.mestor.persistence.query.CriteriaBuilderImpl;
import org.mestor.persistence.query.JpqlParser;
import org.mestor.persistence.query.QueryImpl;
import org.mestor.query.CriteriaLanguageParser;
import org.mestor.util.TypeUtil;
import org.mestor.wrap.ObjectWrapperFactory;

import com.google.common.collect.Maps;

public class EntityManagerImpl implements EntityManager, EntityContext, Closeable {
//	private final static Logger logger = LoggerFactory.getLogger(EntityManagerImpl.class);

	private final PersistenceUnitInfo info;
	private final EntityContext delegateEntityContext;
	private final EntityManagerFactory entityManagerFactory;
	private final Map<String, Object> properties;
//	private final Map<Class<?>, EntityMetadata<?>> entityClasses;
//	private final Map<String, EntityMetadata<?>> entityNames;
//	private final Map<String, String> namedQueries = new HashMap<>();
	private boolean open = false;

	private FlushModeType flushMode;

//	private final Persistor persistor;

	private final Metamodel metamodel;

	public EntityManagerImpl(final PersistenceUnitInfo info, final EntityContext ctx, final EntityManagerFactory entityManagerFactory, final Map<String, Object> properties, final Map<Class<?>, EntityMetadata<?>> entityClasses) {
		this.info = info;
		this.delegateEntityContext = ctx;
		this.entityManagerFactory = entityManagerFactory;
		this.properties = Collections.unmodifiableMap(new LinkedHashMap<String, Object>(properties));

//		final Map<String, Object> allParams = getAllParameters(info, properties);

//		this.entityClasses = entityClasses == null ? new HashMap<Class<?>, EntityMetadata<?>>() : new HashMap<Class<?>, EntityMetadata<?>>(entityClasses);

//		persistor = createPersistor(info, properties, allParams);

//		fillEntityClasses(info, properties, allParams);
//		this.entityNames = new HashMap<String, EntityMetadata<?>>();
//		for (final EntityMetadata<?> emd : this.entityClasses.values()) {
//			entityNames.put(emd.getEntityName(), emd);
//		}
//		DDL_GENERATION.<SchemaMode>value(allParams).init(this);

		metamodel = new MetamodelImpl(getEntityMetadata());
		open = true;
	}

	@Override
	public void clear() {
		checkOpen();
	}

	@Override
	public void close() {
		open = false;
//		if (persistor != null) {
//			try {
//				persistor.close();
//			} catch (final IOException e) {
//				logger.error("Failed to close persistor " + persistor, e);
//			}
//		}
	}


	@Override
	public Query createNamedQuery(final String name) {
		checkOpen();
		return createNamedQuery(name, Object.class);
	}


	@Override
	public Query createNativeQuery(final String cqlString) {
		checkOpen();
		return createNativeQuery(cqlString, Object[].class);
	}

	@Override
	public Query createNativeQuery(final String cqlString, @SuppressWarnings("rawtypes") final Class resultClass) {
		checkOpen();
		return null;
	}

	@Override
	public Query createNativeQuery(final String sqlString, final String resultSetMapping) {
		checkOpen();
		return null;
	}

	@Override
	public Query createQuery(final String qlString) {
		checkOpen();
		return createQuery(qlString, Object.class);
	}

	///////////
    /**
     * Find by primary key.
     * @param entityClass
     * @param primaryKey
     * @return the found entity instance or null
     *    if the entity does not exist
     * @throws IllegalStateException if this EntityManager has been closed.
     * @throws IllegalArgumentException if the first argument does
     *    not denote an entity type or the second
     *    argument is not a valid type for that
     *    entity's primary key
     */
	@Override
	public <T> T find(final Class<T> entityClass, final Object primaryKey) {
		checkOpen();
		checkEntityClass(entityClass);
		checkPrimaryKey(entityClass, primaryKey);

		return find(entityClass, primaryKey, LockModeType.NONE);
	}

	/**
    * @throws EntityNotFoundException if the entity state
    *    cannot be accessed
	 */
	@Override
	public <T> T getReference(final Class<T> entityClass, final Object primaryKey) {
		checkOpen();
		checkEntityClass(entityClass);
		checkPrimaryKey(entityClass, primaryKey);

		final boolean exists =  getPersistor().exists(entityClass, primaryKey);
		if (exists) {
			return getObjectWrapperFactory(entityClass).makeLazy(entityClass, primaryKey);
		}

		throw new EntityNotFoundException("Entity " + entityClass + " identified by primary key " + primaryKey + " does not exist");
	}



	@Override
	public boolean contains(final Object entity) {
		checkOpen();

		final Class<?> entityClass = entity.getClass();
		checkEntityClass(entityClass);

		final Object primaryKey = getPrimaryKeyValue(entity);
		return getPersistor().exists(entityClass, primaryKey);
	}

	@SuppressWarnings("unchecked")
	private <T> Object getPrimaryKeyValue(final T entity) {
		return getEntityMetadata((Class<T>)entity.getClass()).getPrimaryKey().getAccessor().getValue(entity);
	}




	@Override
	public void flush() {
		checkOpen();
		getTransaction().commit();
	}

	@Override
	public Object getDelegate() {
		checkOpen();
		return this;
	}

	@Override
	public FlushModeType getFlushMode() {
		checkOpen();
		return flushMode;
	}


	@Override
	public EntityTransaction getTransaction() {
		return EntityTransactionImpl.getTransaction(this);
	}

	@Override
	public boolean isOpen() {
		return open;
	}

	@Override
	public void joinTransaction() {
		checkOpen();
		throw new UnsupportedOperationException("Joining transaction is not currently supported");
	}

	@Override
	public void lock(final Object entity, final LockModeType lockMode) {
		throw new UnsupportedOperationException("Locking is not supported");
	}

	@Override
	public <T> T merge(final T entity) {
		checkOpen();

		@SuppressWarnings("unchecked")
		final Class<T> entityClass = (Class<T>)entity.getClass();
		checkEntityClass(entityClass);

		final ObjectWrapperFactory<T> owf = getPersistor().getObjectWrapperFactory(entityClass);
		if (owf.isWrapped(entity) && owf.isRemoved(entity)) {
			throw new IllegalArgumentException(entity + " is removed entity");
		}

		persist(entity);

		return entity; //TODO should it be wrapped here?
	}

	@Override
	public void persist(final Object entity) {
		checkOpen();
		getPersistor().store(entity);
	}

	@Override
	public void refresh(final Object entity) {
		refresh(entity, LockModeType.NONE, null);
	}


	private <E> void doRefresh(final E entity) {
		@SuppressWarnings("unchecked")
		final
		Class<E> clazz = (Class<E>)entity.getClass();
		final EntityMetadata<E> emd = getEntityMetadata(clazz);
		final E existing = find(entity);
		emd.copy(existing, entity);
	}



	private <E> E find(final E entity) {
		@SuppressWarnings("unchecked")
		final
		Class<E> clazz = (Class<E>)entity.getClass();
		final EntityMetadata<E> emd = getEntityMetadata(clazz);
		final Object primaryKey = emd.getPrimaryKey().getAccessor().getValue(entity);
		return find(clazz, primaryKey);
	}

	@Override
	public void remove(final Object entity) {
		checkOpen();
		getPersistor().remove(entity);
	}

	@Override
	public void setFlushMode(final FlushModeType flushMode) {
		this.flushMode = flushMode;
	}

	private void checkOpen() {
		if(!open) {
			throw new IllegalStateException("Entity manager is closed");
		}
	}

	private <T> void checkEntityClass(final Class<T> entityClass) {
		final ObjectWrapperFactory<T> owf = getPersistor().getObjectWrapperFactory(entityClass);
		Class<T> clazz = owf.isWrappedType(entityClass) ? owf.getRealType(entityClass) : entityClass;

		final EntityMetadata<T> emeta = delegateEntityContext.getEntityMetadata(clazz);

		if (emeta == null) {
			throw new IllegalArgumentException("Class " + clazz + " is not a registered entity");
		}
	}

	public <T> void checkPrimaryKey(final Class<T> entityClass, final Object primaryKey) {
		final EntityMetadata<T> emeta = delegateEntityContext.getEntityMetadata(entityClass);

		final FieldMetadata<?, ?, ?> pkMeta = emeta.getPrimaryKey();
		if (pkMeta == null) {
			throw new IllegalStateException("Unable to retrieve entity " + entityClass + " using primary key because it does not have primary key");
		}

		final Class<?> pkType = pkMeta.getColumnType();//emeta.getEntityType();

//		final Class<?> pkType = emeta.getEntityType();

		if (primaryKey == null) {
			if (!pkMeta.isNullable()) {
				throw new IllegalArgumentException("Unable to retrieve entity " + entityClass + " using null primary key because it is not nullable");
			}
		} else {
			if (!compareTypes(pkType, primaryKey.getClass())) {
				throw new IllegalArgumentException("Class " + primaryKey.getClass() + " is not compatible with primary key " + pkType + " for class " + entityClass);
			}
		}
	}

	private boolean compareTypes(final Class<?> declaredType, final Class<?> actualType){
		return TypeUtil.compareTypes(declaredType, actualType);
	}

	private void checkLockMode(final LockModeType lockMode) {
		if (!LockModeType.NONE.equals(lockMode)) {
			throw new IllegalArgumentException("Lock mode " + lockMode + " is unsupported");
		}
	}


/*
	private void fillEntityClasses(final PersistenceUnitInfo info, final Map<String, Object> properties, final Map<String, Object> allParams) {
		ClassLoader cl = info.getClassLoader();
		final List<URL> jarFiles = info.getJarFileUrls();
		final String puName = info.getPersistenceUnitName();
		//URL puRoot = info.getPersistenceUnitRootUrl();

		final List<String> packages = MANAGED_CLASS_PACKAGE.value(allParams);

		final boolean excludeUnlistedClasses = info.excludeUnlistedClasses();

//		final List<String> mgmtClassNames = info.getManagedClassNames();
//		final boolean excludeUnlistedClasses = info.excludeUnlistedClasses();


		if (cl == null) {
			cl = Thread.currentThread().getContextClassLoader();
		}

		final ClassScanner cs;
		if (excludeUnlistedClasses) {
			final List<String> mgmtClassNames = info.getManagedClassNames();
			if (mgmtClassNames == null) {
				//FIXME: never happens
				throw new IllegalArgumentException("Explicit entity class list is required but not found");
			}
			cs = new ClassNameClassScanner(cl, mgmtClassNames);
		} else {
			cs = new JpaAnnotatedClassScanner(cl, jarFiles, packages);
		}

		final MetadataFactory mdf;
		try {
			final Class<MetadataFactory> mdfClass = METADATA_FACTORY_CLASS.value(allParams);
			//TODO: add support of different naming strategies for tables, entities, fields, columns, indexes etc.
			final NamingStrategy namingStrategy = NAMING_STRATEGY.value(allParams);
			mdf = mdfClass.newInstance();
			mdf.setSchema(puName);

			if (namingStrategy != null) {
				setProperty(mdf, NamingStrategy.class, namingStrategy);
			}
			setProperty(mdf, EntityContext.class, this);
		} catch (final ReflectiveOperationException e) {
			throw new IllegalArgumentException(e);
		}


		for (final Class<?> c : cs.scan()) {
			final EntityMetadata<?> md = mdf.create(c);
			if (md == null) {
				throw new IllegalArgumentException("Class " + c + " is not a JPA entity");
			}
			entityClasses.put(c, md);

			for (final Entry<String, String> query : md.getNamedQueries().entrySet()) {
				final String name = query.getKey();
				final String ql = query.getValue();
				if (namedQueries.containsKey(name)) {
					throw new IllegalArgumentException("Duplicate named query " + name);
				}
				namedQueries.put(name, ql);
			}
		}

		mdf.update(entityClasses);
	}
*/
/*
	private <M extends MetadataFactory, P> void setProperty(final M mdf, final Class<P> parameterType, final P parameterValue) {
		@SuppressWarnings("unchecked")
		final
		Class<M> mdfClass = (Class<M>)mdf.getClass();
		final MetadataFactory bmf = new BeanMetadataFactory();
		final EntityMetadata<M> mdfemd = bmf.create(mdfClass);

		final Collection<String> parameterFields = mdfemd.getFieldNamesByType(parameterType);
		final int n = parameterFields.size();
		if (n > 1) {
			throw new IllegalStateException("Cannot set parameter of type " + parameterType + " to " + mdfClass + ": there are more than 1 properties of this type");
		}
		if(n == 1) {
			final String namingStrategyField = parameterFields.iterator().next();
			mdfemd.getFieldByName(namingStrategyField).getAccessor().setValue(mdf, parameterValue);
		}

		// property of give type is unsupported. Ignore it.
	}
*/

//	private Persistor createPersistor(final PersistenceUnitInfo info, final Map<String, Object> properties, final Map<String, Object> allParams) {
//
//		final Map<Object, Object> map = new HashMap<>();
//		map.putAll(info.getProperties());
//		map.putAll(properties);
//
//
//		final Class<Persistor> clazz = PERSISTOR_CLASS.value(allParams);
//
//		if (clazz == null) {
//			throw new IllegalArgumentException("No persistor class configured");
//		}
//
//		if (!Persistor.class.isAssignableFrom(clazz)) {
//			throw new IllegalArgumentException(clazz + " must implement "  + Persistor.class + "interface to be a persistor");
//		}
//
//		Constructor<Persistor> c = null;
//		try {
//			try {
//				c = clazz.getConstructor(EntityContext.class);
//				final Persistor persistor = c.newInstance(this);
//				return persistor;
//			} catch (final NoSuchMethodException e) {
//				try {
//					c = clazz.getConstructor();
//					return c.newInstance();
//				} catch (NoSuchMethodException | SecurityException e1) {
//					throw new IllegalArgumentException(
//							clazz + " must provide either constructor that accepts " + Map.class +
//							" argument or default constructor to be a " + Persistor.class);
//				}
//			}
//		} catch (final ReflectiveOperationException e) {
//			throw new IllegalStateException("Cannot create persistor " + clazz, e);
//		}
//	}

	private Map<String, Object> getAllParameters(final PersistenceUnitInfo info, final Map<String, Object> properties) {
		final Map<String, Object> all = new HashMap<>();
		if (info != null && info.getProperties() != null) {
			final Properties infoProps = info.getProperties();
			if(infoProps != null) {
				all.putAll(Maps.fromProperties(infoProps));
			}
		}
		if (properties != null) {
			all.putAll(properties);
		}
		return all;
	}


	/**
	 * Retrieves instance of {@link ObjectWrapperFactory}
	 * @return object wrapper factory
	 */
	private <T> ObjectWrapperFactory<T> getObjectWrapperFactory(final Class<T> entityClass) {
		return getPersistor().getObjectWrapperFactory(entityClass);
	}

	@Override
	public Map<String, Object> getProperties() {
		return getParameters();
	}


	// Implementation of EntityContext

	@Override
	public Map<String, Object> getParameters() {
		return getAllParameters(info, properties);
	}

	@Override
	public Collection<EntityMetadata<?>> getEntityMetadata() {
		return delegateEntityContext.getEntityMetadata();
	}

	@Override
	public Collection<Class<?>> getEntityClasses() {
		return delegateEntityContext.getEntityClasses();
	}

	@Override
//	@SuppressWarnings("unchecked")
	public <T> EntityMetadata<T> getEntityMetadata(final Class<T> clazz) {
		return delegateEntityContext.getEntityMetadata(clazz);
	}

	@Override
//	@SuppressWarnings("unchecked")
	public <T> EntityMetadata<T> getEntityMetadata(final String entityName) {
		return delegateEntityContext.getEntityMetadata(entityName);
	}


	@Override
	public Persistor getPersistor() {
		return delegateEntityContext.getPersistor();
	}

	@Override
	public DirtyEntityManager getDirtyEntityManager() {
		return EntityTransactionImpl.getDirtyEntityManager();
	}

	@Override
	public <T> T find(final Class<T> entityClass, final Object primaryKey, final Map<String, Object> props) {
		return find(entityClass, primaryKey, LockModeType.NONE, props);
	}

	@Override
	public <T> T find(final Class<T> entityClass, final Object primaryKey, final LockModeType lockMode) {
		return find(entityClass, primaryKey, LockModeType.NONE, null);
	}

	@Override
	public <T> T find(final Class<T> entityClass, final Object primaryKey, final LockModeType lockMode, final Map<String, Object> props) {
		checkLockMode(lockMode);
		checkOpen();
		checkEntityClass(entityClass);
		checkPrimaryKey(entityClass, primaryKey);

		return getPersistor().fetch(entityClass, primaryKey);
	}

	@Override
	public void lock(final Object entity, final LockModeType lockMode, final Map<String, Object> properties) {
		checkLockMode(lockMode);
		// do nothing right now. Anyway we don't support locking
	}

	@Override
	public void refresh(final Object entity, final Map<String, Object> props) {
		refresh(entity, LockModeType.NONE, props);
	}

	@Override
	public void refresh(final Object entity, final LockModeType lockMode) {
		refresh(entity, lockMode, null);
	}

	@Override
	public void refresh(final Object entity, final LockModeType lockMode, final Map<String, Object> props) {
		checkLockMode(lockMode);
		doRefresh(entity);
	}

	@Override
	public void detach(final Object entity) {
		throw new UnsupportedOperationException("Detach entity is not implemented yet");
	}

	@Override
	public LockModeType getLockMode(final Object entity) {
		return LockModeType.NONE;
	}

	@Override
	public void setProperty(final String name, final Object value) {
		properties.put(name, value);
	}

	@Override
	public <T> TypedQuery<T> createQuery(final CriteriaQuery<T> criteriaQuery) {
		checkOpen();
		return new QueryImpl<T>(criteriaQuery, this);
	}

	@Override
	public Query createQuery(@SuppressWarnings("rawtypes") final CriteriaUpdate updateQuery) {
		throw new UnsupportedOperationException("Update query is not supported right now. TBD");
	}

	@Override
	public Query createQuery(@SuppressWarnings("rawtypes") final CriteriaDelete deleteQuery) {
		throw new UnsupportedOperationException("Delete query is not supported right now. TBD");
	}

	@Override
	public <T> TypedQuery<T> createQuery(final String qlString, final Class<T> resultClass) {
		return new QueryImpl<T>(getCriteriaLanguageParser().createCriteria(qlString, resultClass), this);
	}

	@Override
	public <T> TypedQuery<T> createNamedQuery(final String name, final Class<T> resultClass) {
		checkOpen();
		return createQuery(getNamedQuery(name), resultClass);

	}

	@Override
	public StoredProcedureQuery createNamedStoredProcedureQuery(final String name) {
		throw new UnsupportedOperationException("Stored procedures are not suppported");
	}

	@Override
	public StoredProcedureQuery createStoredProcedureQuery(final String procedureName) {
		throw new UnsupportedOperationException("Stored procedures are not suppported");
	}

	@Override
	public StoredProcedureQuery createStoredProcedureQuery(final String procedureName, @SuppressWarnings("rawtypes") final Class... resultClasses) {
		throw new UnsupportedOperationException("Stored procedures are not suppported");
	}

	@Override
	public StoredProcedureQuery createStoredProcedureQuery(final String procedureName, final String... resultSetMappings) {
		throw new UnsupportedOperationException("Stored procedures are not suppported");
	}

	@Override
	public boolean isJoinedToTransaction() {
		return false;
	}

	@Override
	public <T> T unwrap(final Class<T> cls) {
        if (cls.isAssignableFrom(this.getClass())) {
            // unwraps any proxy to Query, JPAQuery or EJBQueryImpl
        	@SuppressWarnings("unchecked")
			final
			T unwrapped = (T) this;
            return unwrapped;
        }

        throw new PersistenceException("Could not unwrap entity manager to: " + cls);
	}

	@Override
	public EntityManagerFactory getEntityManagerFactory() {
		return entityManagerFactory;
	}

	@Override
	public CriteriaBuilder getCriteriaBuilder() {
		checkOpen();
		return new CriteriaBuilderImpl(this);
	}

	@Override
	public Metamodel getMetamodel() {
		checkOpen();
		return metamodel;
	}




	@Override
	public Collection<Class<?>> getNativeTypes() {
		return getPersistor().getNativeTypes();
	}

	@Override
	public <T> EntityGraph<T> createEntityGraph(final Class<T> rootType) {
		throw new UnsupportedOperationException("EntityGraphs are not supported right now");
	}

	@Override
	public EntityGraph<?> createEntityGraph(final String graphName) {
		throw new UnsupportedOperationException("EntityGraphs are not supported right now");
	}

	@Override
	public EntityGraph<?> getEntityGraph(final String graphName) {
		throw new UnsupportedOperationException("EntityGraphs are not supported right now");
	}

	@Override
	public <T> List<EntityGraph<? super T>> getEntityGraphs(final Class<T> entityClass) {
		throw new UnsupportedOperationException("EntityGraphs are not supported right now");
	}


	@Override
	public String getNamedQuery(final String name) {
		return delegateEntityContext.getNamedQuery(name);
	}

	@Override
	public CriteriaLanguageParser getCriteriaLanguageParser() {
		return new JpqlParser();
	}

	@Override
	public <E, ID> ID getNextId(final Class<E> clazz, String fieldName) {
		return delegateEntityContext.getNextId(clazz, fieldName);
	}

}
