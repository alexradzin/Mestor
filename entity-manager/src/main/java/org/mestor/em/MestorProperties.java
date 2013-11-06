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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.regex.Pattern;

import org.mestor.metadata.jpa.JpaAnnotationsMetadataFactory;
import org.mestor.metadata.jpa.NamingStrategy;
import org.mestor.util.CollectionUtils;
import org.mestor.util.FileURLCreator;

public enum MestorProperties {
	PREFIX(null),

    PERSISTENCE_XML("persistencexml", "META-INF/persistence.xml"),

    JAR_URLS("jar.urls", System.getProperty("java.class.path")) {
		@SuppressWarnings("unchecked")
		@Override
		public <T> T  value(final Map<?,?> map) {
			final String str = super.value(map);
			return (T)CollectionUtils.nullableTransform(split(str, path), new FileURLCreator());
		}
	},

	ROOT_URL("root.url") {
		@Override
		@SuppressWarnings("unchecked")
		public <T> T  value(final Map<?,?> map) {
			try {
				final String str = super.value(map);
				return (T)new URL(str);
			} catch (final MalformedURLException e) {
				throw new IllegalArgumentException(e);
			}

		}
	},

    MAPPING_FILE_NAMES("mapping.files") {
		@SuppressWarnings("unchecked")
		@Override
		public <T> T  value(final Map<?,?> map) {
			final String str = super.value(map);
			return (T)CollectionUtils.nullsafeAsList(split(str, path));
		}
	},

    MANAGED_CLASS_NAMES("managed.entities") {
		@SuppressWarnings("unchecked")
		@Override
		public <T> T  value(final Map<?,?> map) {
			final String str = super.value(map);
			return (T)CollectionUtils.nullsafeAsList(split(str, path));
		}
	},

    MANAGED_CLASS_PACKAGE("managed.package", "") {
		@SuppressWarnings("unchecked")
		@Override
		public <T> T  value(final Map<?,?> map) {
			final String str = super.value(map);
			return (T)CollectionUtils.nullsafeAsList(split(str, path));
		}
	},


    PERSISTOR_CLASS("persistor.class", null) {
		@SuppressWarnings("unchecked")
		@Override
		public <T> T  value(final Map<?,?> map) {
			final String className = super.value(map);
			return (T)classForName(className);
		}
	},


    METADATA_FACTORY_CLASS("metadata.factory.class", JpaAnnotationsMetadataFactory.class.getName()) {
		@SuppressWarnings("unchecked")
		@Override
		public <T> T  value(final Map<?,?> map) {
			final String className = super.value(map);
			return (T)classForName(className);
		}
	},

    DDL_GENERATION("ddl.generation", SchemaMode.NONE.name()) {
		@SuppressWarnings("unchecked")
		@Override
		public <T> T  value(final Map<?,?> map) {
			final String property = super.value(map);
			return (T)SchemaMode.forProperty(property);
		}
	},

	//SchemaMode

	// The default value of naming strategy is null. MetadataFactory should define hard coded defaults itself.
	// This is done because of currently existing limitation (weakness :() of initialization of of MetadataFactory
	// that supports simple setters only and one setter per type. However there can be multiple naming strategies
	// (for entities, fields, columns, indexes...). So, right now we limit this configuration facility to one
	// strategy for all NamableItems.
	// TODO: improve initialization code.
	NAMING_STRATEGY("naming.strategy", null) {
		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Override
		public <T> T  value(final Map<?,?> map) {
			final String strategyName = super.value(map);
			if (strategyName == null) {
				return null;
			}
			try {
				return (T)((Class<NamingStrategy>)Class.forName(strategyName)).newInstance();
			} catch (final ClassNotFoundException e) {
				// assume that strategyName refers to enum member
				final String enumClassName = strategyName.replaceFirst("\\.\\w+$", "");
				try {
					final Class<?> clazz = Class.forName(enumClassName);
					if (!clazz.isEnum()) {
						throw new IllegalArgumentException(e);
					}
					final String[] arr = strategyName.split("\\.");
					final String enumMember = arr[arr.length - 1];

					return (T)Enum.<Enum>valueOf((Class<Enum>)clazz, enumMember);

				} catch (final ClassNotFoundException e1) {
					throw new IllegalArgumentException(e);
				}
			} catch (final ReflectiveOperationException e) {
				throw new IllegalArgumentException(e);
			}
		}
	},


	;

	private String key;
	private Object defaultValue;

	private final static String prefix = "org.mestor";
	@SuppressWarnings("unused") // for future use
	private final static Pattern csv = Pattern.compile("\\s*,\\s*");
	private final static Pattern path = Pattern.compile("\\s*" + File.pathSeparator + "\\s*");



	private MestorProperties(final String key) {
		this(key, null);
	}

	private MestorProperties(final String key, final Object defaultValue) {
		this.key = (key == null || "".equals(key)) ? prefix : prefix + "." + key;
		this.defaultValue = defaultValue;
	}

	public String key() {
		return key;
	}

	@SuppressWarnings("unchecked")
	public <T> T value(final Map<?,?> map) {
		final T value = map != null ? (T)map.get(key) : null;
		return value == null ? (T)defaultValue : value;
	}

	protected String[] split(final String str, final Pattern p) {
		return str == null ? null : p.split(str);
	}

	@SuppressWarnings("unchecked")
	protected <T> Class<T> classForName(final String className) {
		try {
			return className == null ? null : (Class<T>)Class.forName(className);
		} catch (final ClassNotFoundException e) {
			throw new IllegalStateException("Cannot load persistor implementation " + className, e);
		}
	}
}
