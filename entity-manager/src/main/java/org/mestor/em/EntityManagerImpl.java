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

import static org.mestor.em.MestorProperties.DDL_GENERATION;
import static org.mestor.em.MestorProperties.MANAGED_CLASS_PACKAGE;
import static org.mestor.em.MestorProperties.METADATA_FACTORY_CLASS;
import static org.mestor.em.MestorProperties.NAMING_STRATEGY;
import static org.mestor.em.MestorProperties.PERSISTOR_CLASS;

import java.lang.reflect.Constructor;
import java.net.URL;
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
import org.mestor.metadata.ClassNameClassScanner;
import org.mestor.metadata.ClassScanner;
import org.mestor.metadata.EntityMetadata;
import org.mestor.metadata.FieldMetadata;
import org.mestor.metadata.MetadataFactory;
import org.mestor.metadata.jpa.JpaAnnotatedClassScanner;
import org.mestor.metadata.jpa.NamingStrategy;
import org.mestor.wrap.ObjectWrapperFactory;

import com.google.common.collect.Maps;

//TODO use setProperty() (introduced in JPA 2.0) method for initialization
public class EntityManagerImpl implements EntityManager, EntityContext {
	private final PersistenceUnitInfo info;
	private final Map<String, Object> properties;
	private final Map<Class<?>, EntityMetadata<?>> entityClasses;
	private boolean open = false;
	
	private FlushModeType flushMode;
	
	private Persistor persistor; 
	
	
	public EntityManagerImpl(PersistenceUnitInfo info, Map<String, Object> properties, Class<?>... classes) {
		this.info = info;
		this.properties = Collections.unmodifiableMap(new LinkedHashMap<String, Object>(properties));
		
		Map<String, Object> allParams = getAllParameters(info, properties);
		
		this.entityClasses = getEntityClasses(info, properties, allParams);
		open = true;
		persistor = createPersistor(info, properties, allParams);
		DDL_GENERATION.<SchemaMode>value(allParams).init(this);
	}

	@Override
	public void clear() {
		checkOpen();
	}

	@Override
	public void close() {
		open = false;
		//TODO close persistor (?)
	}


	@Override
	public Query createNamedQuery(String name) {
		checkOpen();
		return null;
	}

	@Override
	public Query createNativeQuery(String sqlString) {
		checkOpen();
		return null;
	}

	@Override
	public Query createNativeQuery(String sqlString, @SuppressWarnings("rawtypes") Class resultClass) {
		checkOpen();
		return null;
	}

	@Override
	public Query createNativeQuery(String sqlString, String resultSetMapping) {
		checkOpen();
		return null;
	}

	@Override
	public Query createQuery(String qlString) {
		checkOpen();
		return null;
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
	public <T> T find(Class<T> entityClass, Object primaryKey) {
		checkOpen();
		checkEntityClass(entityClass);
		checkPrimaryKey(entityClass, primaryKey);		

		return persistor.fetch(entityClass, primaryKey);
	}

	/**
    * @throws EntityNotFoundException if the entity state
    *    cannot be accessed
	 */
	@Override
	public <T> T getReference(Class<T> entityClass, Object primaryKey) {
		checkOpen();
		checkEntityClass(entityClass);
		checkPrimaryKey(entityClass, primaryKey);
		
		boolean exists =  persistor.exists(entityClass, primaryKey);
		if (exists) {
			return getObjectWrapperFactory(entityClass).makeLazy(entityClass, primaryKey);
		}
		
		throw new EntityNotFoundException("Entity " + entityClass + " identified by primary key " + primaryKey + " does not exist");
	}
	
	
	
	@Override
	public boolean contains(Object entity) {
		checkOpen();
		
		Class<?> entityClass = entity.getClass();
		checkEntityClass(entityClass);
		
		@SuppressWarnings({ "rawtypes", "unchecked" })
		Object primaryKey = new EntityMetadata(entityClass).getPrimaryKey().getAccessor().getValue(entity);
		return persistor.exists(entityClass, primaryKey);
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
	public void lock(Object entity, LockModeType lockMode) {
		throw new UnsupportedOperationException("Locking is not supported");
	}

	@Override
	public <T> T merge(T entity) {
		checkOpen();
		if (!contains(entity)) {
			throw new IllegalArgumentException(entity + " is not an entity or is removed entity");
		}
		persist(entity);
		
		return entity; //TODO should it be wrapped here?
	}

	@Override
	public void persist(Object entity) {
		checkOpen();
		persistor.store(entity);
	}

	@Override
	public void refresh(Object entity) {
		doRefresh(entity);
	}
	

	private <E> void doRefresh(E entity) {
		@SuppressWarnings("unchecked")
		Class<E> clazz = (Class<E>)entity.getClass();
		EntityMetadata<E> emd = getEntityMetadata(clazz);
		E existing = find(entity);
		emd.copy(existing, entity);
	}
	
	
	
	private <E> E find(E entity) {
		@SuppressWarnings("unchecked")
		Class<E> clazz = (Class<E>)entity.getClass();
		EntityMetadata<E> emd = getEntityMetadata(clazz);
		Object primaryKey = emd.getPrimaryKey().getAccessor().getValue(entity);
		return find(clazz, primaryKey);
	}

	@Override
	public void remove(Object entity) {
		checkOpen();
		persistor.remove(entity);
	}

	@Override
	public void setFlushMode(FlushModeType flushMode) {
		this.flushMode = flushMode;
	}

	private void checkOpen() {
		if(!open) {
			throw new IllegalStateException("Entity manager is closed");
		}
	}
	
	private <T> void checkEntityClass(Class<T> entityClass) {
		@SuppressWarnings("unchecked")
		EntityMetadata<T> emeta = (EntityMetadata<T>)entityClasses.get(entityClass);
		
		if (emeta == null) {
			throw new IllegalArgumentException("Class " + entityClass + " is not a registered entity");
		}
	}
	
	public <T> void checkPrimaryKey(Class<T> entityClass, Object primaryKey) {
		@SuppressWarnings("unchecked")
		EntityMetadata<T> emeta = (EntityMetadata<T>)entityClasses.get(entityClass);
		
		FieldMetadata<?, ?, ?> pkMeta = emeta.getPrimaryKey();
		if (pkMeta == null) {
			throw new IllegalStateException("Unable to retrieve entity " + entityClass + " using primary key because it does not have primary key");
		}
		
		Class<?> pkType = emeta.getEntityType();
		if (primaryKey == null) {
			if (!pkMeta.isNullable()) {
				throw new IllegalArgumentException("Unable to retrieve entity " + entityClass + " using null primary key because it is not nullable");
			}
		} else {
			if (!pkType.isAssignableFrom(primaryKey.getClass())) {
				throw new IllegalArgumentException("Class " + primaryKey.getClass() + " is not compatible with primary key " + pkType + " for class " + entityClass);
			}
		}
	}
	
	
	
	

	private Map<Class<?>, EntityMetadata<?>> getEntityClasses(PersistenceUnitInfo info, Map<String, Object> properties, Map<String, Object> allParams) {
		ClassLoader cl = info.getClassLoader();
		List<URL> jarFiles = info.getJarFileUrls();
		String puName = info.getPersistenceUnitName();
		//URL puRoot = info.getPersistenceUnitRootUrl();
		
		List<String> packages = MANAGED_CLASS_PACKAGE.value(allParams); 
		
		List<String> mgmtClassNames = info.getManagedClassNames();
		boolean excludeUnlistedClasses = info.excludeUnlistedClasses();
		
		
		if (cl == null) {
			cl = Thread.currentThread().getContextClassLoader();
		}
		
		final ClassScanner cs;
		if (excludeUnlistedClasses) {
			if (mgmtClassNames == null) {
				throw new IllegalArgumentException("Explicit entity class list is required but not found");
			}
			cs = new ClassNameClassScanner(cl, mgmtClassNames);
		} else {
			cs = new JpaAnnotatedClassScanner(cl, jarFiles, packages);
		}
		
		final MetadataFactory mdf;
		try {
			Class<MetadataFactory> mdfClass = METADATA_FACTORY_CLASS.value(allParams);
			//TODO: add support of different naming strategies for tables, entities, fields, columns, indexes etc. 
			NamingStrategy namingStrategy = NAMING_STRATEGY.value(allParams);
			mdf = mdfClass.newInstance();
			mdf.setSchema(puName);
			
			setProperty(mdf, NamingStrategy.class, namingStrategy);
			setProperty(mdf, EntityContext.class, this);
		} catch (ReflectiveOperationException e) {
			throw new IllegalArgumentException(e);
		}
		
		
		Map<Class<?>, EntityMetadata<?>> class2metadata = new HashMap<>();
		for (Class<?> c : cs.scan()) {
			EntityMetadata<?> md = mdf.create(c);
			if (md == null) {
				throw new IllegalArgumentException("Class " + c + " is not a JPA entity");
			}
			class2metadata.put(c, md);
		}
		mdf.update(class2metadata);
		
		return class2metadata;
	}

	
	private <M extends MetadataFactory, P> void setProperty(M mdf, Class<P> parameterType, P parameterValue) {
		@SuppressWarnings("unchecked")
		Class<M> mdfClass = (Class<M>)mdf.getClass();
		EntityMetadata<M> mdfemd = new EntityMetadata<>(mdfClass);
		Collection<String> parameterFields = mdfemd.getFieldNamesByType(parameterType);
		int n = parameterFields.size();
		if (n > 1) {
			throw new IllegalStateException("Cannot set parameter of type " + parameterType + " to " + mdfClass + ": there are more than 1 properties of this type");
		}
		if(n == 1) {
			String namingStrategyField = parameterFields.iterator().next();
			mdfemd.getField(namingStrategyField).getAccessor().setValue(mdf, parameterValue);
		}
		
		// property of give type is unsupported. Ignore it. 
	}
	
	
	private Persistor createPersistor(PersistenceUnitInfo info, Map<String, Object> properties, Map<String, Object> allParams) {
		
		Map<Object, Object> map = new HashMap<>();
		map.putAll(info.getProperties());
		map.putAll(properties);
		
		
		Class<Persistor> clazz = PERSISTOR_CLASS.value(allParams);
		
		if (clazz == null) {
			throw new IllegalArgumentException("No persistor class configured");
		}
		
		if (!Persistor.class.isAssignableFrom(clazz)) {
			throw new IllegalArgumentException(clazz + " must implement "  + Persistor.class + "interface to be a persistor");
		}
		
		Constructor<Persistor> c = null;
		try {
			try {
				c = clazz.getConstructor(EntityContext.class);
				Persistor persistor = c.newInstance(this);
				return persistor;
			} catch (NoSuchMethodException e) {
				try {
					c = clazz.getConstructor();
					return c.newInstance();
				} catch (NoSuchMethodException | SecurityException e1) {
					throw new IllegalArgumentException(
							clazz + " must provide either constructor that accepts " + Map.class + 
							" argument or default constructor to be a " + Persistor.class);
				}
			}
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException("Cannot create persistor " + clazz, e);
		}
	}
	
	private Map<String, Object> getAllParameters(PersistenceUnitInfo info, Map<String, Object> properties) {
		Map<String, Object> all = new HashMap<>();
		if (info != null && info.getProperties() != null) {
			Properties infoProps = info.getProperties();
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
	private <T> ObjectWrapperFactory<T> getObjectWrapperFactory(Class<T> entityClass) {
		return persistor.getObjectWrapperFactory(entityClass);
	}

	// Implementation of EntityContext
	
	@Override
	public Map<String, Object> getProperties() {
		return getAllParameters(info, properties);
	}
	
	@Override
	public Collection<EntityMetadata<?>> getEntityMetadata() {
		return entityClasses.values();
	}
	
	@Override
	public Collection<Class<?>> getEntityClasses() {
		return entityClasses.keySet();
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <T> EntityMetadata<T> getEntityMetadata(Class<T> clazz) {
		return (EntityMetadata<T>)entityClasses.get(clazz);
	}

	@Override
	public Persistor getPersistor() {
		return persistor;
	}

	@Override
	public DirtyEntityManager getDirtyEntityManager() {
		return EntityTransactionImpl.getDirtyEntityManager();
	}

	@Override
	public <T> T find(Class<T> entityClass, Object primaryKey, Map<String, Object> properties) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T find(Class<T> entityClass, Object primaryKey, LockModeType lockMode) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T find(Class<T> entityClass, Object primaryKey, LockModeType lockMode, Map<String, Object> properties) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void lock(Object entity, LockModeType lockMode, Map<String, Object> properties) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void refresh(Object entity, Map<String, Object> properties) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void refresh(Object entity, LockModeType lockMode) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void refresh(Object entity, LockModeType lockMode, Map<String, Object> properties) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void detach(Object entity) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public LockModeType getLockMode(Object entity) {
		// TODO Auto-generated method stub
		return LockModeType.NONE;
	}

	@Override
	public void setProperty(String propertyName, Object value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public <T> TypedQuery<T> createQuery(CriteriaQuery<T> criteriaQuery) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Query createQuery(@SuppressWarnings("rawtypes") CriteriaUpdate updateQuery) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Query createQuery(@SuppressWarnings("rawtypes") CriteriaDelete deleteQuery) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> TypedQuery<T> createQuery(String qlString, Class<T> resultClass) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> TypedQuery<T> createNamedQuery(String name, Class<T> resultClass) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public StoredProcedureQuery createNamedStoredProcedureQuery(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public StoredProcedureQuery createStoredProcedureQuery(String procedureName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public StoredProcedureQuery createStoredProcedureQuery(String procedureName, @SuppressWarnings("rawtypes") Class... resultClasses) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public StoredProcedureQuery createStoredProcedureQuery(String procedureName, String... resultSetMappings) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isJoinedToTransaction() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public <T> T unwrap(Class<T> cls) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public EntityManagerFactory getEntityManagerFactory() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CriteriaBuilder getCriteriaBuilder() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Metamodel getMetamodel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> EntityGraph<T> createEntityGraph(Class<T> rootType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public EntityGraph<?> createEntityGraph(String graphName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public EntityGraph<?> getEntityGraph(String graphName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> List<EntityGraph<? super T>> getEntityGraphs(Class<T> entityClass) {
		// TODO Auto-generated method stub
		return null;
	}
}
