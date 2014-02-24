package org.mestor.persistence.cql;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.cassandra.locator.SimpleStrategy;
import org.codehaus.jackson.map.ObjectMapper;

import antlr.collections.List;

public class CqlPersistorProperties<P> {
	final static String CASSANDRA_PROP_ROOT = "org.mestor.cassandra";

	private final static Pattern split = Pattern.compile("\\s*,;\\s*");


	public static enum ThrowOnViolation {
		THROW_FIRST, THROW_ALL_TOGETHER
	}


	final static CqlPersistorProperties<String[]> CASSANDRA_HOSTS = new CqlPersistorProperties<String[]>(String[].class, CASSANDRA_PROP_ROOT + "." + "hosts", new String[] {"localhost"});
	final static CqlPersistorProperties<Integer> CASSANDRA_PORT = new CqlPersistorProperties<Integer>(Integer.class, CASSANDRA_PROP_ROOT + "." + "port", null);

	final static CqlPersistorProperties<ThrowOnViolation> SCHEMA_VALIDATION = new CqlPersistorProperties<ThrowOnViolation>(ThrowOnViolation.class, CASSANDRA_PROP_ROOT + "." + "schema.validation", ThrowOnViolation.THROW_ALL_TOGETHER);


	@SuppressWarnings({ "rawtypes", "serial" })
	final static CqlPersistorProperties<Map> CASSANDRA_KEYSPACE_PROPERTIES = new CqlPersistorProperties<Map>(Map.class, CASSANDRA_PROP_ROOT + "." + "keyspace.properties",
			Collections.<String, Object>singletonMap(
					"replication",
					new HashMap<String, Object>() {{
						put("class", SimpleStrategy.class.getSimpleName());
						put("replication_factor", 1);
					}}
			)
		);

	public final static CqlPersistorProperties<Object[]> PARTITION_KEY = new CqlPersistorProperties<Object[]>(Object[].class, CASSANDRA_PROP_ROOT + "." + "partition.key", new Object[] {"partition", 0});

	private final Class<P> type;
	private final String property;
	private final P defaultValue;


	private CqlPersistorProperties(final Class<P> type, final String property, final P defaultValue) {
		this.type = type;
		this.property = property;
		this.defaultValue = defaultValue;
	}

	public P getValue(final Map<String, Object> map) {
		Object rawValue = null;
		if (map != null) {
			rawValue = map.get(property);
		}
		if (rawValue == null) {
			return defaultValue;
		}
		return convertToType(type, rawValue);
	}

	/**
	 * Converts given raw value to required type.
	 * TODO: make this method more generic. Make it possible to register type converters etc.
	 * @param rawValue
	 * @return typed value
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected <T> T convertToType(final Class<T> type, final Object rawValue) {
		final Class<?> rawType = rawValue.getClass();

		if (type.isAssignableFrom(rawValue.getClass())) {
			return (T)rawValue;
		}

		if (!CharSequence.class.isAssignableFrom(rawType)) {
			throw new IllegalArgumentException("Cannot transform " + rawType + " to type");
		}

		final String strValue = (String)rawValue;
		// check primitives and primitive wrappers
		if (Integer.class.equals(type) || int.class.equals(type)) {
			return (T)Integer.getInteger(strValue);
		}
		if (Long.class.equals(type) || long.class.equals(type)) {
			return (T)Long.getLong(strValue);
		}
		if (Long.class.equals(type) || long.class.equals(type)) {
			return (T)Long.getLong(strValue);
		}
		if (Short.class.equals(type) || short.class.equals(type)) {
			return (T)new Short(Short.parseShort(strValue));
		}
		if (Byte.class.equals(type) || byte.class.equals(type)) {
			return (T)new Byte(Byte.parseByte(strValue));
		}
		if (Character.class.equals(type) || char.class.equals(type)) {
			if (strValue.length() != 0) {
				throw new IllegalArgumentException("Cannot transform string '" + strValue + "' to char");
			}
			return (T)new Character(strValue.charAt(0));
		}
		if(type.isEnum()) {
			return (T)Enum.valueOf((Class<? extends Enum>)type, strValue);
		}


		// limited support of arrays
		if (type.isArray()) {
			final String[] strArr = split.split(strValue);
			final int n = strArr.length;
			final Class<?> componentType = type.getComponentType();
			final Object typedArr = Array.newInstance(componentType, n);

			for (int i = 0; i < n; i++) {
				Array.set(typedArr, i, convertToType(componentType, strArr[i]));
			}

			return (T)typedArr;
		}

		// limited support of collections
		if (Collection.class.isAssignableFrom(type)) {
			final String[] strArr = split.split(strValue);
			final int n = strArr.length;
			final Class<?> componentType = type.getComponentType();

			final Object instance = createInstance(type);
			final Collection<Object> result = (Collection<Object>)instance;

			for (int i = 0; i < n; i++) {
				result.add(convertToType(componentType, strArr[i]));
			}

			return (T)result;
		}

		// limited support of maps
		if (Map.class.isAssignableFrom(type)) {
			try {
				return (T)new ObjectMapper().readValue(toJson(strValue).getBytes() , Map.class);
			} catch (final IOException e) {
				throw new IllegalArgumentException("Cannot parse value " + strValue, e);
			}
		}


		throw new IllegalArgumentException("Cannot transform string '" + strValue + "' to any supported type");
	}


	private static String toJson(final String cassandraSpec) {
		String json = cassandraSpec.replace("\'", "").replace('=', ':').replaceAll("([\\p{Alpha}_]+)", "\"$1\"");
		if (!json.matches("^\\s*\\{.*?\\}\\s*$")) {
			json = "{" + json + "}";
		}
		return json;
	}

	@SuppressWarnings({ "cast", "unchecked" })
	private <C> C createInstance(final Class<C> collectionClass) {
		if (collectionClass.getPackage().getName().startsWith("java.")) {
			try {
				return (C)collectionClass.newInstance();
			} catch (final ReflectiveOperationException e) {
				// do nothing here. Try to create some kind of collection anyway
			}
		}

		if (List.class.isAssignableFrom(collectionClass)) {
			return (C)new ArrayList<Object>();
		}
		if (Set.class.isAssignableFrom(collectionClass)) {
			return (C)new LinkedHashSet<Object>();
		}
		if (Map.class.isAssignableFrom(collectionClass)) {
			return (C)new LinkedHashMap<Object, Object>();
		}


		throw new IllegalArgumentException("Cannot create instance to clone " + collectionClass);
	}


	String property() {
		return property;
	}
}
