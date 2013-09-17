package org.mestor.em;

import java.util.Map;

import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.PersistenceUnitInfo;

public class PersistenceProviderImpl implements PersistenceProvider {
	// Multiple @SuppressWarnings are here because interface PersistenceProvider 
	// is defined without generics, however the rest of the application uses them
	// whenever it is required. 
	
	@Override
	public EntityManagerFactory createEntityManagerFactory(
			String emName,
			@SuppressWarnings("rawtypes") Map map) {

		@SuppressWarnings({ "cast", "unchecked" })
		Map<String, String> props = (Map<String, String>)map;
		return new EntityManagerFactoryImpl(new PersistenceUnitInfoImpl(emName, map), props);
	}

	@Override
	public EntityManagerFactory createContainerEntityManagerFactory(
			PersistenceUnitInfo info, 
			@SuppressWarnings("rawtypes") Map map) {
		
		@SuppressWarnings({ "cast", "unchecked" })
		Map<String, String> props = (Map<String, String>)map;
		return new EntityManagerFactoryImpl(info, props);
	}


}
