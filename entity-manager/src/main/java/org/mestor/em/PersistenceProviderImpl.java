package org.mestor.em;

import static org.mestor.em.MestorProperties.DDL_GENERATION;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.ProviderUtil;

import org.mestor.context.EntityContext;
import org.mestor.util.CollectionUtils;
import org.mestor.util.Pair;
import org.mestor.util.SystemProperties;

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

	private final static Map<Pair<String, Map<?, ?>>, PersistenceUnitInfoImpl> puiCache = new HashMap<>();


	@Override
	public EntityManagerFactory createEntityManagerFactory(
			final String emName,
			@SuppressWarnings("rawtypes") final Map map) {

		final Map<String, String> props = allProps(map);
		final PersistenceUnitInfoImpl info = getPersistenceUnitInfo(emName, props);
		return createContainerEntityManagerFactoryImpl(info, info, props);
	}

	@Override
	public EntityManagerFactory createContainerEntityManagerFactory(
			final PersistenceUnitInfo info,
			@SuppressWarnings("rawtypes") final Map map) {

		final Map<String, String> props = allProps(map);
		final PersistenceUnitInfoImpl infoImpl = getPersistenceUnitInfo(info, props);
		return createContainerEntityManagerFactoryImpl(infoImpl, infoImpl, props);
	}

	private EntityManagerFactory createContainerEntityManagerFactoryImpl(final PersistenceUnitInfo info, final EntityContext ctx, final Map<String, String> props) {
		return new EntityManagerFactoryImpl(info, ctx, props);
	}




	@Override
	public void generateSchema(final PersistenceUnitInfo info, @SuppressWarnings("rawtypes") final Map map) {
		final Map<String, String> props = allProps(map);
		generateSchemaImpl(getPersistenceUnitInfo(info, props), props);

	}

	@Override
	public boolean generateSchema(final String persistenceUnitName, @SuppressWarnings("rawtypes") final Map map) {
		final Map<String, String> props = allProps(map);
		final PersistenceUnitInfoImpl info = getPersistenceUnitInfo(persistenceUnitName, props);
		generateSchemaImpl(info, props);
		return true;
	}

	@SuppressWarnings("unchecked")
	private void generateSchemaImpl(final PersistenceUnitInfoImpl info, @SuppressWarnings("rawtypes") final Map map) {
		final Map<Object, Object> all = new HashMap<>();
		all.putAll(info.getProperties());
		all.putAll(map);

		DDL_GENERATION.<SchemaMode>value(all).init(info);
	}



	@Override
	public ProviderUtil getProviderUtil() {
		// TODO Auto-generated method stub
		return null;
	}


	private PersistenceUnitInfoImpl getPersistenceUnitInfo(
			final String emName,
			@SuppressWarnings("rawtypes") final Map map) {

		final Pair<String, Map<?, ?>> puiKey = new Pair<String, Map<?, ?>>(emName, map);
		PersistenceUnitInfoImpl info = puiCache.get(puiKey);
		if (info == null) {
			info = new PersistenceUnitInfoImpl(emName, map);
			registerPeristenceUnit(puiKey, info, map);
		}
		return info;
	}

	private PersistenceUnitInfoImpl getPersistenceUnitInfo(
			final PersistenceUnitInfo info,
			@SuppressWarnings("rawtypes") final Map map) {

		final Pair<String, Map<?, ?>> puiKey = new Pair<String, Map<?, ?>>(info.getPersistenceUnitName(), info.getProperties());

		PersistenceUnitInfoImpl infoImpl = puiCache.get(puiKey);
		if (infoImpl == null) {
			infoImpl = info instanceof PersistenceUnitInfoImpl ? (PersistenceUnitInfoImpl)info : new PersistenceUnitInfoImpl(info);
			registerPeristenceUnit(puiKey, infoImpl, map);
		}

		return infoImpl;
	}

	private Map<String, String> allProps(@SuppressWarnings("rawtypes") final Map map) {
		@SuppressWarnings({ "unchecked" })
		final Map<String, String> props = CollectionUtils.merge(
				Maps.filterKeys(System.getenv(), predicate),
				Maps.filterKeys(Maps.fromProperties(SystemProperties.systemProperties()), predicate),
				map);

		return props;
	}

	private void registerPeristenceUnit(final Pair<String, Map<?, ?>> puiKey, final PersistenceUnitInfoImpl infoImpl, @SuppressWarnings("rawtypes") final Map map) {
		puiCache.put(puiKey, infoImpl);
		generateSchemaImpl(infoImpl, map);
	}
}
