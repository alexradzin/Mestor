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

package org.mestor.persistence.metamodel;

import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManagerFactory;
import javax.persistence.metamodel.EmbeddableType;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.spi.PersistenceUnitInfo;

import org.mestor.metadata.EntityMetadata;

public class MetamodelImpl implements Metamodel {
	private final Map<Class<?>, EntityMetadata<?>> entityClasses = null;
	//TODO go over the context's entity metadata and create all ManagedTypes.
	// then iterate over just created Managed types and update reference to ManagedType
	// in all attributes: this is relevant for inheritance.
	
	public MetamodelImpl(PersistenceUnitInfo info, EntityManagerFactory entityManagerFactory, Map<String, Object> properties) {
		//entityClasses = getEntityClasses(info, properties);
	}

	@Override
	public <X> EntityType<X> entity(Class<X> cls) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <X> ManagedType<X> managedType(Class<X> cls) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <X> EmbeddableType<X> embeddable(Class<X> cls) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<ManagedType<?>> getManagedTypes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<EntityType<?>> getEntities() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<EmbeddableType<?>> getEmbeddables() {
		// TODO Auto-generated method stub
		return null;
	}

/*	
	private Map<Class<?>, EntityMetadata<?>> getEntityClasses(PersistenceUnitInfo info, Map<String, Object> properties) {
		ClassLoader cl = info.getClassLoader();
		List<URL> jarFiles = info.getJarFileUrls();
		String puName = info.getPersistenceUnitName();
		//URL puRoot = info.getPersistenceUnitRootUrl();
		
		List<String> packages = MANAGED_CLASS_PACKAGE.value(properties); 
		
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
			Class<MetadataFactory> mdfClass = METADATA_FACTORY_CLASS.value(properties);
			//TODO: add support of different naming strategies for tables, entities, fields, columns, indexes etc. 
			NamingStrategy namingStrategy = NAMING_STRATEGY.value(properties);
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
			
//			for (Entry<String, String> query : md.getNamedQueries().entrySet()) {
//				String name = query.getKey();
//				String ql = query.getValue();
//				if (namedQueries.containsKey(name)) {
//					throw new IllegalArgumentException("Duplicate named query " + name);
//				}
//				namedQueries.put(name, ql);
//			}
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

 */
	public Map<Class<?>, EntityMetadata<?>> getEntityClasses() {
		return entityClasses;
	}
}
