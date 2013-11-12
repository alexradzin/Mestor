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

import static org.mestor.em.MestorProperties.COLUMN_NAMING_STRATEGY;
import static org.mestor.em.MestorProperties.ENTITY_NAMING_STRATEGY;
import static org.mestor.em.MestorProperties.ID_GENERATOR;
import static org.mestor.em.MestorProperties.MANAGED_CLASS_PACKAGE;
import static org.mestor.em.MestorProperties.METADATA_FACTORY_CLASS;
import static org.mestor.em.MestorProperties.NAMING_STRATEGY;
import static org.mestor.em.MestorProperties.PERSISTOR_CLASS;
import static org.mestor.em.MestorProperties.TABLE_NAMING_STRATEGY;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Properties;

import javax.persistence.SharedCacheMode;
import javax.persistence.ValidationMode;
import javax.persistence.spi.ClassTransformer;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.PersistenceUnitTransactionType;
import javax.sql.DataSource;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.mestor.context.DirtyEntityManager;
import org.mestor.context.EntityContext;
import org.mestor.context.Persistor;
import org.mestor.metadata.BeanMetadataFactory;
import org.mestor.metadata.ClassNameClassScanner;
import org.mestor.metadata.ClassScanner;
import org.mestor.metadata.EntityMetadata;
import org.mestor.metadata.FieldMetadata;
import org.mestor.metadata.IdGeneratorMetadata;
import org.mestor.metadata.MetadataFactory;
import org.mestor.metadata.jpa.JpaAnnotatedClassScanner;
import org.mestor.metadata.jpa.NamableItem;
import org.mestor.metadata.jpa.NamingStrategy;
import org.mestor.persistence.query.JpqlParser;
import org.mestor.persistencexml.Persistence;
import org.mestor.persistencexml.Persistence.PersistenceUnit;
import org.mestor.query.CriteriaLanguageParser;
import org.mestor.reflection.ClassAccessor;
import org.mestor.util.CollectionUtils;
import org.mestor.util.FromStringFunction;
import com.google.common.collect.Maps;

public class PersistenceUnitInfoImpl implements PersistenceUnitInfo, EntityContext {
	private final String name;
	private final Map<String, ?> params;
	private final Map<String, Object> allParams;

	private PersistenceUnit persistenceUnit;

	private final Map<Class<?>, EntityMetadata<?>> entityClasses = new HashMap<Class<?>, EntityMetadata<?>>();
	private final Map<String, EntityMetadata<?>> entityNames = new HashMap<String, EntityMetadata<?>>();
	private final Map<String, String> namedQueries = new HashMap<>();

	private final Persistor persistor;

	private final Map<String, Iterator<?>> idGenerators = new HashMap<>();


	PersistenceUnitInfoImpl(final PersistenceUnitInfo ref) {
		this(ref.getPersistenceUnitName(), Maps.fromProperties(ref.getProperties()));
	}

	PersistenceUnitInfoImpl(final String name, final Map<String, ?> params) {

		this.name = name;
		this.params = params;
		parsePersistenceXml(name);
		allParams = getAllParameters(this, persistenceUnit, params);
		persistor = createPersistor();
		fillEntityClasses();

		for (final EntityMetadata<?> emd : this.entityClasses.values()) {
			entityNames.put(emd.getEntityName(), emd);
		}

	}

	@Override
	public String getPersistenceUnitName() {
		return name;
	}

	@Override
	public String getPersistenceProviderClassName() {
		return PersistenceProviderImpl.class.getName();
	}

	@Override
	public PersistenceUnitTransactionType getTransactionType() {
		return persistenceUnit == null || persistenceUnit.getTransactionType() == null ?
			null :
			PersistenceUnitTransactionType.valueOf(persistenceUnit.getTransactionType().name());
	}

	@Override
	public DataSource getJtaDataSource() {
		return null;
	}

	@Override
	public DataSource getNonJtaDataSource() {
		return null;
	}

	@Override
	public List<String> getMappingFileNames() {
		final List<String> mappings = MestorProperties.MAPPING_FILE_NAMES.value(params);
		if (mappings != null) {
			return mappings;
		}

		return persistenceUnit.getMappingFile();
	}

	@Override
	public List<URL> getJarFileUrls() {
		final Collection<URL> urls = MestorProperties.JAR_URLS.value(params);
		if (urls != null) {
			return urls instanceof List ? (List<URL>)urls : new ArrayList<URL>(urls);
		}

		final Collection<URL> persistenceXmlJarUrls = CollectionUtils.nullableTransform(
				persistenceUnit.getJarFile(),
				new FromStringFunction<URL>(URL.class));

		if (persistenceXmlJarUrls == null) {
			return null;
		}

		return new ArrayList<URL>(persistenceXmlJarUrls);
	}

	@Override
	public URL getPersistenceUnitRootUrl() {
		return MestorProperties.ROOT_URL.value(params);
	}

	@Override
	public List<String> getManagedClassNames() {
		final List<String> classNames = MestorProperties.MANAGED_CLASS_NAMES.value(params);
		if (classNames != null) {
			return classNames;
		}

		return persistenceUnit.getClazz();
	}

	@Override
	public boolean excludeUnlistedClasses() {
		if (MestorProperties.MANAGED_CLASS_NAMES.value(params) != null) {
			return true;
		}

		if (persistenceUnit == null) {
			return false;
		}

		final Boolean explicitExcludeUnlistedClasses =  persistenceUnit.isExcludeUnlistedClasses();
		if (explicitExcludeUnlistedClasses != null) {
			return explicitExcludeUnlistedClasses;
		}

		if(persistenceUnit.getJarFile().isEmpty() && persistenceUnit.getMappingFile().isEmpty() && !persistenceUnit.getClazz().isEmpty()) {
			return true;
		}

		return false;
	}

	@Override
	public Properties getProperties() {
		return (Properties)fillProperties(new Properties());
	}

	@Override
	public ClassLoader getClassLoader() {
		return Thread.currentThread().getContextClassLoader();
	}

	@Override
	public void addTransformer(final ClassTransformer transformer) {
		//
	}

	@Override
	public ClassLoader getNewTempClassLoader() {
		return new ClassLoader(getClassLoader()) {
			// no implementation here.
			// We need inner class only because class loader is an abstract class.
		};
	}

	private void parsePersistenceXml(final String puName) {
		final String persistenceXmlResource = MestorProperties.PERSISTENCE_XML.value(params);
		try(
				final InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(persistenceXmlResource)
			){
			if (in == null) {
				return;
			}
			final JAXBContext jaxbContext = JAXBContext.newInstance(Persistence.class.getPackage().getName());
			final Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			final Persistence persistence = (Persistence) jaxbUnmarshaller.unmarshal(in);

			// find current persistence unit
			for (final PersistenceUnit pu : persistence.getPersistenceUnit()) {
				if (puName.equals(pu.getName())) {
					persistenceUnit = pu;
					return;
				}
			}
			throw new IllegalArgumentException("persistence.xml doesn't contain defined persistence unit element: " + puName);
		} catch (final JAXBException | IOException e) {
			throw new IllegalStateException("Parsing of persistence.xml located at " + persistenceXmlResource + " failed ", e);
		}
	}

	@Override
	public SharedCacheMode getSharedCacheMode() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ValidationMode getValidationMode() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getPersistenceXMLSchemaVersion() {
		// TODO Auto-generated method stub
		return null;
	}


	private void fillEntityClasses() {
		ClassLoader cl = getClassLoader();
		final List<URL> jarFiles = getJarFileUrls();
		final String puName = getPersistenceUnitName();
		//URL puRoot = info.getPersistenceUnitRootUrl();

		final List<String> packages = MANAGED_CLASS_PACKAGE.value(allParams);

		final boolean excludeUnlistedClasses = excludeUnlistedClasses();

//		final List<String> mgmtClassNames = info.getManagedClassNames();
//		final boolean excludeUnlistedClasses = info.excludeUnlistedClasses();


		if (cl == null) {
			cl = Thread.currentThread().getContextClassLoader();
		}

		final ClassScanner cs;
		if (excludeUnlistedClasses) {
			final List<String> mgmtClassNames = getManagedClassNames();
			if (mgmtClassNames == null) {
				//FIXME: never happens
				throw new IllegalArgumentException("Explicit entity class list is required but not found");
			}
			cs = new ClassNameClassScanner(cl, mgmtClassNames);
		} else {
			cs = new JpaAnnotatedClassScanner(cl, jarFiles, packages);
		}

		final MetadataFactory mdf;
		final Class<MetadataFactory> mdfClass = METADATA_FACTORY_CLASS.value(allParams);
		try {
			//TODO: add support of different naming strategies for tables, entities, fields, columns, indexes etc.
			mdf = mdfClass.newInstance();
			mdf.setSchema(puName);


			final NamingStrategy namingStrategy = NAMING_STRATEGY.value(allParams);
			if (namingStrategy != null) {
				setProperty(mdf, NamingStrategy.class, namingStrategy);
			}
			setProperty(mdf, EntityContext.class, this);
		} catch (final ReflectiveOperationException e) {
			throw new IllegalArgumentException(e);
		}



		// Set specific naming strategy per NameableItem
		Method namingStrategySetter;
		try {
			namingStrategySetter = mdfClass.getMethod("setNamingStrategy", NamableItem.class, NamingStrategy.class);
			for(final MestorProperties namingStrategyProp : new MestorProperties[] {ENTITY_NAMING_STRATEGY, TABLE_NAMING_STRATEGY, COLUMN_NAMING_STRATEGY}) {
				final NamingStrategy namingStrategy = namingStrategyProp.value(allParams);
				if (namingStrategy != null) {
					final String key = namingStrategyProp.localKey();
					final String[] keyParts = key.split("\\.");
					final String item = keyParts[0].toUpperCase();
					namingStrategySetter.invoke(mdf, NamableItem.valueOf(item), namingStrategy);
				}
			}
		} catch (final NoSuchMethodException e) {
			// it is OK setNamingStrategy() is not supported here.
		} catch (final ReflectiveOperationException e) {
			throw new IllegalArgumentException(e);
		}


		// initialize ID generator


		Map<String, String> idGeneratorsConfig = ID_GENERATOR.value(allParams);

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


			for(FieldMetadata<?, ?, ?> fmd : md.getFields()) {
				if (!fmd.isKey() || fmd.getIdGenerator() == null) {
					continue;
				}

				IdGeneratorMetadata<?, ?> idGeneratorMd = fmd.getIdGenerator();
				String generatorName = idGeneratorMd.getGenerator();


				String idGeneratorConfig = null;
				if (generatorName == null || "".equals(generatorName)) {
					final String entityClassName = c.getName();
					idGeneratorConfig = idGeneratorsConfig.get(entityClassName + "#" + fmd.getName());
					if (idGeneratorConfig == null) {
						idGeneratorConfig = idGeneratorsConfig.get(entityClassName);
					}
					if (idGeneratorConfig == null) {
						idGeneratorConfig = idGeneratorsConfig.get(md.getPrimaryKey().getType().getName());
					}
					if (idGeneratorConfig == null) {
						idGeneratorConfig = idGeneratorsConfig.get(null);
					}
				} else {
					idGeneratorConfig = idGeneratorsConfig.get(generatorName);
				}


				if (idGeneratorConfig == null) {
					idGeneratorConfig = idGeneratorsConfig.get(null);
				}

				if (idGeneratorConfig != null) {
					// Treat idGeneratorConfig as a class name.
					String idGeneratorKey = c.getName() + "#" + fmd.getName();
					idGenerators.put(idGeneratorKey, (Iterator<?>)ClassAccessor.newInstance(idGeneratorConfig));
				}

			}
		}

		mdf.update(entityClasses);
	}

	private <M extends MetadataFactory, P> void setProperty(final M mdf, final Class<P> parameterType, final P parameterValue) {
		@SuppressWarnings("unchecked")
		final Class<M> mdfClass = (Class<M>)mdf.getClass();
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

		// property of given type is unsupported. Ignore it.
	}


	///////////////////////////////////////



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
	public <T> EntityMetadata<T> getEntityMetadata(final Class<T> clazz) {
		return (EntityMetadata<T>)entityClasses.get(clazz);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> EntityMetadata<T> getEntityMetadata(final String entityName) {
		return (EntityMetadata<T>)entityNames.get(entityName);
	}

	@Override
	public String getNamedQuery(final String name) {
		return namedQueries.get(name);
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
	public Collection<Class<?>> getNativeTypes() {
		return persistor.getNativeTypes();
	}

	@Override
	public CriteriaLanguageParser getCriteriaLanguageParser() {
		return new JpqlParser();
	}

	@Override
	public Map<String, Object> getParameters() {
		return fillProperties(new HashMap<String, Object>());
	}


	@SuppressWarnings("unchecked")
	private <K, V> Map<K, V> fillProperties(final Map<K, V> properties) {
		if (persistenceUnit != null) {
			final Persistence.PersistenceUnit.Properties props = persistenceUnit.getProperties();
			if (props != null) {
				for (final Persistence.PersistenceUnit.Properties.Property prop : props.getProperty()) {
					properties.put((K)prop.getName(), (V)prop.getValue());
				}
			}
		}

		if (params != null) {
			properties.putAll((Map<K, V>)params);
		}

		return properties;
	}


	@Override
	public int hashCode() {
		return Objects.hash(name, params);
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null || !(obj instanceof PersistenceUnitInfoImpl)) {
			return false;
		}
		final PersistenceUnitInfoImpl other = (PersistenceUnitInfoImpl)obj;
		return Objects.equals(name, other.name) && Objects.equals(params, other.params);
	}


	private Persistor createPersistor() {

//		final Map<Object, Object> map = new HashMap<>();
//		map.putAll(info.getProperties());
//		map.putAll(properties);

//		final Map<String, Object> allParams = getAllParameters(this, persistenceUnit, params);

		final Class<Persistor> clazz = PERSISTOR_CLASS.value(allParams);

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
				final Persistor persistor = c.newInstance(this);
				return persistor;
			} catch (final NoSuchMethodException e) {
				try {
					c = clazz.getConstructor();
					return c.newInstance();
				} catch (NoSuchMethodException | SecurityException e1) {
					throw new IllegalArgumentException(
							clazz + " must provide either constructor that accepts " + Map.class +
							" argument or default constructor to be a " + Persistor.class);
				}
			}
		} catch (final ReflectiveOperationException e) {
			throw new IllegalStateException("Cannot create persistor " + clazz, e);
		}
	}

	private Map<String, Object> getAllParameters(final PersistenceUnitInfo info, final PersistenceUnit unit, final Map<String, ?> properties) {
		final Map<String, Object> all = new HashMap<>();
		if (info != null) {
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

	@Override
	public <E, ID> ID getNextId(final Class<E> clazz, String fieldName) {
		final FieldMetadata<E, ID, ?> idFmd = getEntityMetadata(clazz).getFieldByName(fieldName);
		if (!idFmd.isKey()) {
			throw new IllegalArgumentException(clazz.getName() + "#" + fieldName + " is not an ID");
		}
		@SuppressWarnings("cast")
		final Class<ID> idClass = (Class<ID>)idFmd.getType();
		return findIdGenerator(clazz, fieldName, idClass).next();
	}

	@SuppressWarnings("unchecked")
	private <E, ID> Iterator<ID> findIdGenerator(final Class<E> clazz, final String fieldName, final Class<ID> idClass) {
		String[] generatorIds = new String[] {
				clazz.getName() + "#" + fieldName,
				clazz.getName(),
				idClass.getName()
		};

		for (String generatorId : generatorIds) {
			Iterator<ID> generator = (Iterator<ID>)idGenerators.get(generatorId);
			if (generator != null) {
				return generator;
			}

		}

		throw new IllegalStateException("ID generator for entity " + clazz + " is undefined");
	}
}
