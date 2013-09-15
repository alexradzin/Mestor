package org.mestor.em;

import java.util.Map;

import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.PersistenceUnitInfo;

public class PersistenceProviderImpl implements PersistenceProvider {
	@Override
	public EntityManagerFactory createEntityManagerFactory(
			String emName,
			@SuppressWarnings("rawtypes") Map map) {
		return new EntityManagerFactoryImpl(new PersistenceUnitInfoImpl(emName, map), map);
	}

	@Override
	public EntityManagerFactory createContainerEntityManagerFactory(
			PersistenceUnitInfo info, 
			@SuppressWarnings("rawtypes") Map map) {
		
		return new EntityManagerFactoryImpl(info, map);
	}


}
