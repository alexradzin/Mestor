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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
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

import org.mestor.persistencexml.Persistence;
import org.mestor.persistencexml.Persistence.PersistenceUnit;
import org.mestor.util.CollectionUtils;
import org.mestor.util.FromStringFunction;

public class PersistenceUnitInfoImpl implements PersistenceUnitInfo {
	private final String name;
	private final Map<?, ?> params;
	
	private PersistenceUnit persistenceUnit;
	
	PersistenceUnitInfoImpl(final String name, final Map<? ,?> params) {
		this.name = name;
		this.params = params;
		parsePersistenceXml(name);
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
		final Properties properties = new Properties();
		if (persistenceUnit != null) {
			final Persistence.PersistenceUnit.Properties props = persistenceUnit.getProperties();
			if (props != null) {
				for (final Persistence.PersistenceUnit.Properties.Property prop : props.getProperty()) {
					properties.setProperty(prop.getName(), prop.getValue());				
				}
			}
		}
		
		if (params != null) {
			properties.putAll(params);
		}
		
		return properties;
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
			throw new IllegalArgumentException("persistence.xml doesn't containe defined persistence unit element: " + puName);
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

}
