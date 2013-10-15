package org.mestor.em;

import java.util.Map;
import java.util.regex.Pattern;

import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.ProviderUtil;

import org.mestor.util.CollectionUtils;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Maps;

public class PersistenceProviderImpl implements PersistenceProvider {
	// Multiple @SuppressWarnings are here because interface PersistenceProvider 
	// is defined without generics, however the rest of the application uses them
	// whenever it is required. 
	
	private final String prefix = MestorProperties.PREFIX.key();
	private final Pattern prefixPattern = Pattern.compile("^" + prefix + "\\.");
	private final Predicate<CharSequence> predicate = Predicates.contains(prefixPattern);
	

	@Override
	public EntityManagerFactory createEntityManagerFactory(
			String emName,
			@SuppressWarnings("rawtypes") Map map) {

		@SuppressWarnings({ "unchecked" })
		Map<String, String> props = CollectionUtils.merge(
				Maps.filterKeys(System.getenv(), predicate), 
				Maps.filterKeys(Maps.fromProperties(System.getProperties()), predicate), 
				map);
		return new EntityManagerFactoryImpl(new PersistenceUnitInfoImpl(emName, props), props);
	}

	@Override
	public EntityManagerFactory createContainerEntityManagerFactory(
			PersistenceUnitInfo info, 
			@SuppressWarnings("rawtypes") Map map) {
		
		@SuppressWarnings({ "cast", "unchecked" })
		Map<String, String> props = (Map<String, String>)map;
		return new EntityManagerFactoryImpl(info, props);
	}

	@Override
	public void generateSchema(PersistenceUnitInfo info, @SuppressWarnings("rawtypes") Map map) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean generateSchema(String persistenceUnitName, @SuppressWarnings("rawtypes") Map map) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ProviderUtil getProviderUtil() {
		// TODO Auto-generated method stub
		return null;
	}


}
