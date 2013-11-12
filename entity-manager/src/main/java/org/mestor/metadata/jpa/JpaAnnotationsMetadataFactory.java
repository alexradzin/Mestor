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

package org.mestor.metadata.jpa;

import java.io.Serializable;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Nullable;
import javax.persistence.AttributeConverter;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.mestor.context.EntityContext;
import org.mestor.metadata.BeanMetadataFactory;
import org.mestor.metadata.CascadeOption;
import org.mestor.metadata.DummyValueConverter;
import org.mestor.metadata.EntityMetadata;
import org.mestor.metadata.FieldMetadata;
import org.mestor.metadata.IdGeneratorMetadata;
import org.mestor.metadata.IdGeneratorMetadata.GenerationType;
import org.mestor.metadata.jpa.conversion.BeanConverter;
import org.mestor.metadata.jpa.conversion.DummyAttributeConverter;
import org.mestor.metadata.jpa.conversion.EnumNameConverter;
import org.mestor.metadata.jpa.conversion.EnumOrdinalConverter;
import org.mestor.metadata.jpa.conversion.IndexedFieldConverter;
import org.mestor.metadata.jpa.conversion.PrimaryKeyConverter;
import org.mestor.metadata.jpa.conversion.SerializableConverter;
import org.mestor.metadata.jpa.conversion.ValueAttributeConverter;
import org.mestor.query.ClauseInfo;
import org.mestor.query.CriteriaLanguageParser;
import org.mestor.query.OrderByInfo;
import org.mestor.query.QueryInfo;
import org.mestor.reflection.Access;
import org.mestor.reflection.CompositePropertyAccessor;
import org.mestor.reflection.FieldAccessor;
import org.mestor.reflection.MethodAccessor;
import org.mestor.reflection.PropertyAccessor;
import org.mestor.reflection.jpa.DiscriminatorValueAccess;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Collections2;
import com.google.common.primitives.Primitives;

@DiscriminatorColumn
@Inheritance
public class JpaAnnotationsMetadataFactory extends BeanMetadataFactory {
	private final Map<NamableItem, NamingStrategy> namingStrategies = new EnumMap<NamableItem, NamingStrategy>(NamableItem.class) {{
		for(final NamableItem i : NamableItem.values()) {
			put(i, StandardNamingStrategy.LOWER_CASE_UNDERSCORE);
		}
		put(NamableItem.ENTITY, StandardNamingStrategy.UPPER_CAMEL_CASE);
	}};

	private final static Map<Object, Collection<CascadeOption>> cascade = new HashMap<Object, Collection<CascadeOption>>() {{
		put(CascadeType.PERSIST, Collections.singleton(CascadeOption.PERSIST));
		put(CascadeType.MERGE, Collections.singleton(CascadeOption.MERGE));
		put(CascadeType.REMOVE, Collections.singleton(CascadeOption.REMOVE));
		put(CascadeType.REFRESH, Collections.singleton(CascadeOption.REFRESH));
		put(CascadeType.ALL, Arrays.asList(CascadeOption.PERSIST, CascadeOption.MERGE, CascadeOption.REMOVE, CascadeOption.REFRESH));
		put(FetchType.EAGER, Collections.singleton(CascadeOption.FETCH));
	}};


	private EntityContext entityContext;


	public JpaAnnotationsMetadataFactory(final Map<NamableItem, NamingStrategy> namingStrategies) {
		this();
		this.namingStrategies.putAll(namingStrategies);
	}

	public JpaAnnotationsMetadataFactory() {
	}

	public JpaAnnotationsMetadataFactory(final EntityContext context) {
		setEntityContext(context);
	}

	public void setNamingStrategy(final NamingStrategy namingStrategy) {
		for(final NamableItem item : NamableItem.values()) {
			setNamingStrategy(item, namingStrategy);
		}
	}

	public void setNamingStrategy(final NamableItem item, final NamingStrategy namingStrategy) {
		this.namingStrategies.put(item, namingStrategy);
	}

	public void setEntityContext(final EntityContext context) {
		this.entityContext = context;
	}

	@Override
	public <T> EntityMetadata<T> create(final Class<T> clazz) {
		final Entity entity = clazz.getAnnotation(Entity.class);
		if (entity == null) {
			return null;
		}

		final EntityMetadata<T> emeta = new EntityMetadata<T>(clazz);

		// discover entity name
		emeta.setEntityName(extractName(entity, namingStrategies.get(NamableItem.ENTITY).getEntityName(clazz)));

		final Table table = clazz.getAnnotation(Table.class);
		emeta.setTableName(extractName(new Object[] {table, entity}, namingStrategies.get(NamableItem.TABLE).getTableName(clazz)));
		String schema = null;
		if (table != null) {
			schema = table.schema();
		}

		if (Strings.isNullOrEmpty(schema)) {
			schema = this.getSchema();
		}

		emeta.setSchemaName(schema);

		processNamedQueries(emeta);


		// name to metadata
		final Map<String, FieldMetadata<T, Object, Object>> fields = new LinkedHashMap<>();

		for (final Field f : FieldAccessor.getFields(clazz)) {
			if (isTransient(f) || Modifier.isStatic(f.getModifiers())) {
				continue;
			}

			@SuppressWarnings("unchecked")
			final Class<Object> type = (Class<Object>)f.getType();
			final String name = getFieldName(f);
			final FieldMetadata<T, Object, Object> fmeta = create(clazz, type, name);

			fmeta.setField(f);
			initMeta(fmeta, f, name, clazz, type);

			fields.put(name, fmeta);

		}

		for (final Method m : clazz.getMethods()) {
			if(!MethodAccessor.isGetter(m)) {
				continue;
			}

			final String fieldName = getFieldName(m);
			FieldMetadata<T, Object, Object> fmeta = fields.get(fieldName);


			if (isTransient(m)) {
				if (fmeta != null) {
					fields.remove(fieldName);
				}
				continue;
			}

			if (fmeta == null) {
				@SuppressWarnings("unchecked")
				final Class<Object> type = (Class<Object>)m.getReturnType();
				fmeta = create(clazz, type, fieldName);
				if (initMeta(fmeta, m, fieldName, clazz, type)) {
					fields.put(fieldName, fmeta);
				}
			}
			fmeta.setGetter(m);
		}

		for (final Method m : clazz.getMethods()) {
			if(!MethodAccessor.isSetter(m)) {
				continue;
			}
			final String fieldName = getFieldName(m);
			final FieldMetadata<T, Object, Object> fmeta = fields.get(fieldName);

			if (fmeta == null) {
				continue;
			}
			fmeta.setSetter(m);
		}


		final Collection<String> primaryKeyFields = new ArrayList<>();
		final Collection<PropertyAccessor<T, ? extends Object>> primaryKeyAccessors = new ArrayList<>();

		for (final Entry<String, FieldMetadata<T, Object, Object>> entry : fields.entrySet()) {
			final FieldMetadata<T, ? extends Object, ? extends Object> fmeta = entry.getValue();

			if (fmeta.isKey()) {
				primaryKeyFields.add(entry.getKey());
				primaryKeyAccessors.add(fmeta.getAccessor());
			}
		}


		if (primaryKeyFields.isEmpty()) {
			throw new IllegalArgumentException("Entity " + clazz + " does not have primary key");
		}


		for (final FieldMetadata<T, ?, ?> fmd : fields.values()) {
			emeta.addField(fmd);
		}


		emeta.setPrimaryKey(fields.get(primaryKeyFields.iterator().next()));

		if (primaryKeyFields.size() > 1) {
			final IdClass idClass = clazz.getAnnotation(IdClass.class);
			if (idClass == null) {
				throw new IllegalArgumentException("Entity " + clazz + " has " + primaryKeyFields.size() + " @Id fields but does not have @IdClass");
			}

			@SuppressWarnings("unchecked")
			final
			Class<Object> idClazz = idClass.value();

			final String name = Joiner.on("_").join(primaryKeyFields);

			@SuppressWarnings("unchecked")
			final
			FieldMetadata<T, Object, Object> keyMetadata = new FieldMetadata<>(clazz, idClass.value(), name);

			@SuppressWarnings("unchecked")
			final
			PropertyAccessor<T, Object>[] primaryKeyAccessorsArr = primaryKeyAccessors.toArray(new PropertyAccessor[primaryKeyAccessors.size()]);
			keyMetadata.setAccessor(new CompositePropertyAccessor<T, Object>(clazz, idClazz, name, primaryKeyAccessorsArr));

			emeta.setPrimaryKey(keyMetadata);
		}

		updateIdGenerator(emeta);

		//emeta.addAllIndexes(findIndexes(emeta));

		updateFieldAttributes(emeta);

		return emeta;
	}


	private <E, T, F, C> void setCollectionConverter(final FieldMetadata<E, F, C> foreignKey, final FieldMetadata<T, F, C> collectionField) {
		@SuppressWarnings("unchecked")
		final
		Class<F> type = (Class<F>)collectionField.getGenericTypes().iterator().next();

		final AttributeConverter<F, C> converter = foreignKey.isKey() ?
				new PrimaryKeyConverter<F, C>(type, entityContext) :
				new IndexedFieldConverter<F, C>(type, foreignKey.getName(), entityContext);

		collectionField.setConverter(
				new DummyValueConverter<F, C>(),
				new ValueAttributeConverter<F, C>(converter)
		);
	}


	/**
	 * Retrieves collection element type (if specified {@link FieldMetadata} represents {@link Collection}
	 * or {code null} otherwise.
	 * @param fmd
	 * @return
	 */
	@ElementCollection // used for default value
	private <T, P, C> void parseCollectionElementType(final Map<Class<?>, EntityMetadata<?>> metadata, final EntityMetadata<T> emd, final FieldMetadata<T, P, C> fmd) {
		final Class<?> collectionType = fmd.getType();
		if (!Collection.class.isAssignableFrom(collectionType) && !Map.class.isAssignableFrom(collectionType)) {
			return;
		}


		final Type collectionGenericType = fmd.getAccessor().getGenericType();
		Type[] discoveredType = new Type[0];
		if (collectionGenericType instanceof ParameterizedType) {
			discoveredType = ((ParameterizedType)collectionGenericType).getActualTypeArguments();
		}


		Type explicitType = null;
		String mappedBy = "";

		final OneToMany oneToMany = fmd.getAccessor().getAnnotation(OneToMany.class);
		final ManyToMany manyToMany = fmd.getAccessor().getAnnotation(ManyToMany.class);
		ElementCollection elementCollection = fmd.getAccessor().getAnnotation(ElementCollection.class);
		if (oneToMany == null && manyToMany == null && elementCollection == null) {
			try {
				// although javadoc of ElementCollection says that this annotation "Must be specified if the collection is to be mapped"
				// we saw that at least eclipse link creates BLOB field for such collections even when this annotation does not exist, so we
				// have to follow this concept here.
				//TODO: probably make this feature configurable.
				elementCollection = getClass().getDeclaredMethod("parseCollectionElementType", Map.class, EntityMetadata.class, FieldMetadata.class).getAnnotation(ElementCollection.class);
			} catch (final NoSuchMethodException e) {
				throw new IllegalStateException("This method must be annotated using ElementCollection to get default value");
			}
		}


		if (oneToMany != null || manyToMany != null || elementCollection != null) {
			if (!(oneToMany != null ^ manyToMany != null ^ elementCollection != null)) {
				throw new IllegalArgumentException("Only one of OneToMany, ManyToMany, ElementCollection can be used for one field (" + fmd.getName() + ")");
			}
		}


//		Class<?> collectionElementClass = null;

		Set<CascadeOption> cascadeOptions = new HashSet<CascadeOption>();

		if (oneToMany != null) {
			explicitType = oneToMany.targetEntity();
			mappedBy = oneToMany.mappedBy();

			addCascadeOption(cascadeOptions, oneToMany.fetch());
			addCascadeOption(cascadeOptions, oneToMany.cascade());
			if (oneToMany.orphanRemoval()) {
				cascadeOptions.add(CascadeOption.ORPHAN_REMOVAL);
			}
		} else if (manyToMany != null) {
			explicitType = manyToMany.targetEntity();
			mappedBy = manyToMany.mappedBy();

			addCascadeOption(cascadeOptions, manyToMany.fetch());
			addCascadeOption(cascadeOptions, manyToMany.cascade());
		} else if (elementCollection != null) {
			explicitType = elementCollection.targetClass();
			addCascadeOption(cascadeOptions, elementCollection.fetch());
		} else if (fmd.getAccessor().getAnnotation(Convert.class) != null) {
			return; //this collection has explicitly defined converter.
		} else if (fmd.getAccessor().getAnnotation(Embedded.class) != null && discoveredType.length != 0) {
			//collectionElementClass = castTypeToClass(collectionElementType);
		} else {
			throw new IllegalArgumentException("Cannot identify type of collection " + fmd.getClassType() + "." + fmd.getName());
		}

		cascadeOptions.remove(null);

		for (CascadeOption cascadeOption : cascadeOptions) {
			fmd.setCascade(cascadeOption, true);
		}


		if (explicitType != null && !void.class.equals(explicitType)) {
			discoveredType[discoveredType.length -1] = explicitType;
		}


		if (!"".equals(mappedBy)) {
			@SuppressWarnings("unchecked")
			final EntityMetadata<Object> ref = (EntityMetadata<Object>)metadata.get(discoveredType[discoveredType.length -1]);
			ref.addIndex(ref.getFieldByName(mappedBy));
		}


		final Class<?>[] elementType = castTypeToClass(discoveredType);
		fmd.setGenericTypes(elementType);


		AttributeConverter<P, C> converter = null;

		if (oneToMany != null || manyToMany != null) {
			@SuppressWarnings("unchecked")
			final FieldMetadata<T, P, C> filterField = (FieldMetadata<T, P, C>)getCollectionElementBackReferenceField(metadata, emd, fmd);
			setCollectionConverter(filterField, fmd);

			fmd.setColumnGenericTypes(Collections.<Class<?>>singleton(Primitives.wrap(filterField.getType())));

			return;
		}

		if (elementCollection != null) {
			// TODO: add support of convertors for Maps
			if(Collection.class.isAssignableFrom(collectionType)) {
				@SuppressWarnings("unchecked")
				final Class<P> elementClass = (Class<P>)elementType[elementType.length - 1];
				final List<AttributeConverter<?, ?>> converters = findConverters(fmd, elementClass);
				if (converters != null && !converters.isEmpty()) {
					@SuppressWarnings("unchecked")
					final AttributeConverter<P, C> castConv = (AttributeConverter<P, C>)converters.get(0);
					converter = castConv;
				}
			}
			fmd.setColumnGenericTypes(Arrays.<Class<?>>asList(elementType));
		}


		if (converter != null) {
			fmd.setConverter(new DummyValueConverter<P, C>(), new ValueAttributeConverter<>(converter));
			final List<Class<?>> columenGenericTypes = new ArrayList<>(getColumnGenericTypes(Collections.<AttributeConverter<?, ?>>singleton(converter)));
			final Collection<Class<?>> existingColumenGenericTypes = fmd.getColumnGenericTypes();

			final List<Class<?>> mergedColumnGenericTypes = new ArrayList<>(existingColumenGenericTypes);

			for (int i = 0; i < columenGenericTypes.size(); i++) {
				final Class<?> g = columenGenericTypes.get(i);
				if (g != null) {
					mergedColumnGenericTypes.set(i, g);
				}
			}

			fmd.setColumnGenericTypes(mergedColumnGenericTypes);
		}
	}

	//TODO: add support of all attributes of ManyToOne
	private <T, P> void parseCollectionElementBackReference(final Map<Class<?>, EntityMetadata<?>> metadata, final EntityMetadata<T> emd, final FieldMetadata<T, P, Object> fmd) {
		final ManyToOne manyToOne = fmd.getAccessor().getAnnotation(ManyToOne.class);
		if (manyToOne == null) {
			return;
		}

		Class<P> fieldType = fmd.getType();

		final Class<?> type = manyToOne.targetEntity();
		if (!void.class.equals(type)) {
			fieldType = castTypeToClass(type);
		}

		Set<CascadeOption> cascadeOptions = new HashSet<CascadeOption>();
		addCascadeOption(cascadeOptions, manyToOne.fetch());
		addCascadeOption(cascadeOptions, manyToOne.cascade());
		cascadeOptions.remove(null);

		for (CascadeOption cascadeOption : cascadeOptions) {
			fmd.setCascade(cascadeOption, true);
		}



		// TODO: do the same for OneToMany and ManyToMany

		fmd.setConverter(new ValueAttributeConverter<P, Object>(new PrimaryKeyConverter<P, Object>(fieldType, entityContext)));
		final FieldMetadata<P, Object, Object> secondSidePK = entityContext.getEntityMetadata(fieldType).getPrimaryKey();

		final String oldColumn = fmd.getColumn();

		final String columnName = oldColumn + "_" + secondSidePK.getColumn();
		fmd.setColumn(columnName);
		fmd.setColumnType(secondSidePK.getColumnType());


		emd.updateField(fmd, fmd.getName(), oldColumn);

	}


	private <T, P> FieldMetadata<T, Object, Object> getCollectionElementBackReferenceField(final Map<Class<?>, EntityMetadata<?>> metadata, final EntityMetadata<T> emd, final FieldMetadata<T, P, ?> fmd) {
		final JoinColumn joinColumn = fmd.getAccessor().getAnnotation(JoinColumn.class);
		String column = "";
		if (joinColumn != null) {
			column = joinColumn.name();
		}
		if ("".equals(column) || "_".equals(column)) {
			return emd.getPrimaryKey();
		}
		return emd.getField(column);
	}



	@SuppressWarnings("unchecked")
	private <C> Class<C> castTypeToClass(final Type type) {
		return (Class<C>)type;
	}

	private <C> Class<C>[] castTypeToClass(final Type[] types) {
		@SuppressWarnings("unchecked")
		final
		Class<C>[] classes = new Class[types.length];
		for (int i = 0; i < types.length; i++) {
			classes[i] = castTypeToClass(types[i]);
		}
		return classes;
	}


	private String extractName(final Object nameHolder, final String defaultValue) {
		try {
			final String name = nameHolder == null ? null : (String)nameHolder.getClass().getMethod("name").invoke(nameHolder);
			return extractParameter(name, defaultValue);
		} catch (final ReflectiveOperationException e) {
			throw new IllegalArgumentException(nameHolder.getClass() + " does not provide method name()");
		}
	}

	private String extractName(final Object[] nameHolders, final String defaultValue) {
			if (nameHolders == null || nameHolders.length == 0) {
				return defaultValue;
			}
			for (final Object nameHolder : nameHolders) {
				try {
					final String name = nameHolder == null ? null : (String)nameHolder.getClass().getMethod("name").invoke(nameHolder);
					if(!Strings.isNullOrEmpty(name)) {
						return name;
					}
				} catch (final ReflectiveOperationException e) {
					throw new IllegalArgumentException(nameHolder.getClass() + " does not provide method name()");
				}
			}
			return defaultValue;
	}



	private String extractParameter(final String value, final String defaultValue) {
		return Strings.isNullOrEmpty(value) ? defaultValue : value;
	}




	private <T, F, C> boolean initMeta(final FieldMetadata<T, F, C> fmeta, final AccessibleObject ao, final String memberName, final Class<T> clazz, final Class<F> memberType) {
		final Column column = ao.getAnnotation(Column.class);
		fmeta.setColumn(extractName(column, namingStrategies.get(NamableItem.COLUMN).getColumnName(ao)));
		fmeta.setKey(ao.getAnnotation(Id.class) != null);

		final boolean nullable = ao.getAnnotation(Nullable.class) != null;

		if (nullable && memberType.isPrimitive()) {
			throw new IllegalArgumentException("Primitive field " + memberName + " cannot be nullable");
		}

		fmeta.setNullable(nullable || !memberType.isPrimitive());


		final List<AttributeConverter<?, ?>> converters = findConverters(fmeta);

		if(converters != null && !converters.isEmpty()) {
			@SuppressWarnings("unchecked")
			final
			AttributeConverter<F, C> converter = (AttributeConverter<F, C>)converters.remove(0);
			fmeta.setConverter(new ValueAttributeConverter<>(converter), wrapConverters(converters).toArray(new ValueAttributeConverter[0]));

			final Class<C> columnType = getColumnType(converter);
			if (columnType != null) {
				fmeta.setColumnType(columnType);
			}

			if (!converters.isEmpty()) {
				fmeta.setColumnGenericTypes(getColumnGenericTypes(converters));
			}
		}

		return column != null;
	}

	private <F, C> Collection<ValueAttributeConverter<?,?>> wrapConverters(final List<AttributeConverter<?, ?>> attributeConverters) {
		if (attributeConverters == null || attributeConverters.isEmpty()) {
			return Collections.emptyList();
		}

		final Collection<ValueAttributeConverter<?,?>> valueConverters = new ArrayList<>();
		for (final AttributeConverter<?, ?> ac : attributeConverters) {
			valueConverters.add(new ValueAttributeConverter<>(ac));
		}

		return valueConverters;
	}


	private <C> Class<C> getColumnType(final AttributeConverter<?, C> converter) {
		for (final Type iface : converter.getClass().getGenericInterfaces()) {
			if (!(iface instanceof ParameterizedType)) {
				continue;
			}
			final ParameterizedType piface = (ParameterizedType)iface;
			if (!AttributeConverter.class.equals(piface.getRawType())) {
				continue;
			}
			final Type typeArgument = piface.getActualTypeArguments()[1];
			if (typeArgument instanceof Class) {
				@SuppressWarnings("unchecked")
				final
				Class<C> columnType = (Class<C>)piface.getActualTypeArguments()[1];
				return columnType;
			}
			return null;
		}
		throw new IllegalArgumentException("Cannot extract column type ");
	}


	private Collection<Class<?>> getColumnGenericTypes(final Collection<AttributeConverter<?, ?>> converters) {
		final Collection<Class<?>> types = new ArrayList<>();

		for (final AttributeConverter<?, ?> converter : converters) {
			types.add(getColumnType(converter));
		}

		return types;
	}


	private boolean isTransient(final AccessibleObject ao) {
		final int mod = ((Member)ao).getModifiers();
		final Transient tr = ao.getAnnotation(Transient.class);
		return Modifier.isTransient(mod) || tr != null;
	}

	@Override
	public void update(final Map<Class<?>, EntityMetadata<?>> metadata) {
		for (final EntityMetadata<?> emd : metadata.values()) {
			updateInheritance(metadata, emd);
			updateRelationships(metadata, emd);
			emd.updateFields();
		}

		for (final EntityMetadata<?> emd : metadata.values()) {
			updateIndexes(emd);
		}
	}

	private <T> void updateIndexes(final EntityMetadata<T> emd) {
		emd.addAllIndexes(findIndexes(emd));
	}


	//TODO: inheritance:
	// 1. single table:
	//    Base Class:
	//        add prefix to each field of each subclass
	//        create discriminator with column name TABLE_NAME + "_discriminator"
	//    Sub class:
	//        concatentate field of the class itself and its super class
	//        add prefix to each field
	//        create discriminator with null column name
	// 2. Separate tables:
	//    Base class:
	//        add prefix to each field
	//        create discriminator with column name TABLE_NAME + "_discriminator"
	//    Sub class:
	//        add prefix to each field
	//        create discriminator with null column name
	// 3. Joined table
	//    Base class:
	//        create discriminator with column name TABLE_NAME + "_discriminator"
	//    Sub class:
	//        create discriminator with column name TABLE_NAME + "_discriminator"
	//        create joiner


	// Add prefix:
	// 1. single and separate tables
	// Concatenate fields:
	// 1. single table: base class: add all fields of all subclasses
	// 2. separate table: sub classes: add all fields of super class
	// 3. when super class is marked with @MappedSuperclass
	// Discriminator with not-null name
	// 1. base class
	// 2. sub class if joined table
	// Discriminator with null name
	// 1. sub class if single table
	// Joiner
	// 1. sub class if joined table.


	private <E> void updateInheritance(final Map<Class<?>, EntityMetadata<?>> metadata, final EntityMetadata<E> emd) {
		final Class<E> clazz = emd.getEntityType();
		final Inheritance inheritance = getInheritance(clazz);
		final InheritanceType inheritenceType = inheritance.strategy();

		if (InheritanceType.TABLE_PER_CLASS.equals(inheritenceType) && Modifier.isAbstract(clazz.getModifiers())) {
			emd.setTableName(null); // abstract base classes in TABLE_PER_CLASS hierarchy do not need their table.
		}

		for (Class<?> c = clazz.getSuperclass(); !Object.class.equals(c); c = c.getSuperclass()) {
			final EntityMetadata<?> superEmd = metadata.get(c);
			if (superEmd != null) {
				@SuppressWarnings("unchecked")
				final
				EntityMetadata<? super E> supertype = (EntityMetadata<? super E>)superEmd;
				emd.setSupertype(supertype);
			}
		}

		final DiscriminatorValue discriminatorValue = clazz.getAnnotation(DiscriminatorValue.class);
		if (discriminatorValue == null) {
			return;
		}
		final String discriminatorValueValue = discriminatorValue.value();
		final DiscriminatorColumn discriminatorColumn = getDiscriminatorColumn(clazz);
		final DiscriminatorType dtype = discriminatorColumn.discriminatorType();

		String dname = discriminatorColumn.name();

		//TODO: check whether this condition is enough to recognize sub class.
		final boolean isSubClass = clazz.getAnnotation(Inheritance.class) == null;


		if (InheritanceType.SINGLE_TABLE.equals(inheritenceType) && isSubClass) {
			dname = null; // sub class when single table is used.
		}


		final Access<E, Object, AccessibleObject> discriminatorAccess = new DiscriminatorValueAccess<E, Object>(clazz);
		@SuppressWarnings({ "rawtypes", "unchecked" })
		final
		FieldMetadata<E, Object, Object> discriminator = new FieldMetadata(clazz, getDiscriminatorType(dtype), dname);
		discriminator.setAccessor(new PropertyAccessor<E, Object>(clazz, Object.class, dname,
				null, null, null, // field, getter, setter
				discriminatorAccess, discriminatorAccess));
		discriminator.setDiscriminator(true);
		discriminator.setDefaultValue(discriminatorValueValue);


		emd.addField(discriminator);

		if (InheritanceType.JOINED.equals(inheritenceType) && isSubClass) {
			final FieldMetadata<E, Object, Object> joiner = createJoiner(metadata, clazz);
			emd.addField(joiner);
		}
	}



	private DiscriminatorColumn getDiscriminatorColumn(final Class<?> type) {
		final Class<?>[] classes = new Class[] {type, type.getSuperclass(), getClass()};

		for (final Class<?> clazz : classes) {
			final DiscriminatorColumn discriminatorColumn = clazz.getAnnotation(DiscriminatorColumn.class);
			if (discriminatorColumn != null) {
				return discriminatorColumn;
			}
		}

		throw new IllegalStateException("Cannot locate DiscriminatorColumn for class " + type);
	}

	private Inheritance getInheritance(final Class<?> type) {
		for (Class<?> c = type; !Object.class.equals(c); c = c.getSuperclass()) {
			final Inheritance inheritance = c.getAnnotation(Inheritance.class);
			if (inheritance != null) {
				return inheritance;
			}
		}
		return getClass().getAnnotation(Inheritance.class);

	}

	private Class<?> getDiscriminatorType(final DiscriminatorType dtype) {
		switch (dtype) {
			case CHAR:
				return char.class;
			case STRING:
				return String.class;
			case INTEGER:
				return int.class;
			default: throw new IllegalArgumentException("Unsupported discriminator type " + dtype);
		}
	}

	private <E> FieldMetadata<E, Object, Object> createJoiner(final Map<Class<?>, EntityMetadata<?>> metadata, final Class<E> clazz) {
		final Class<?> parent = clazz.getSuperclass();
		@SuppressWarnings("unchecked")
		final
		EntityMetadata<E> parentMeta = (EntityMetadata<E>)metadata.get(parent);
		final FieldMetadata<E, Object, ?> parentPk = parentMeta.getPrimaryKey();
		final String column = parentMeta.getTableName().toLowerCase() + "_" + parentPk.getName();

		final FieldMetadata<E, Object, Object> joiner = new FieldMetadata<>(clazz, parentPk.getType(), column);
		joiner.setColumn(column);
		joiner.setJoiner(true);

		return joiner;
	}


	private <E> void updateRelationships(final Map<Class<?>, EntityMetadata<?>> metadata, final EntityMetadata<E> emd) {
		// references to collections.
		for (final FieldMetadata<E, Object, Object> fmd : emd.getFields()) {
			parseCollectionElementType(metadata, emd, fmd);
			parseCollectionElementBackReference(metadata, emd, fmd);
		}
	}


	private <E, F, C> List<AttributeConverter<?, ?>> findConverters(final FieldMetadata<E, F, C> fmeta) {
		final Convert convert = fmeta.getAccessor().getAnnotation(Convert.class);
		List<AttributeConverter<?, ?>> converters;
		if(convert != null) {
			@SuppressWarnings("unchecked")
			final
			Class<AttributeConverter<F, C>> converterClass = convert.converter();
			converters = Collections.<AttributeConverter<?, ?>>singletonList((createConverterInstance(converterClass, fmeta.getType())));
		} else {
			converters = findConverters(fmeta, fmeta.getType());
		}

		// wrap by ArrayList to make list modifiable
		return converters == null ? null : new ArrayList<>(converters);
	}



	//TODO: add support of default converters and not-default constructors of converters
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private <E, F, C> List<AttributeConverter<?, ?>> findConverters(final FieldMetadata<E, F, C> fmeta, final Class<F> type) {
		final Convert convert = fmeta.getAccessor().getAnnotation(Convert.class);
		if(convert != null) {
			final Class<AttributeConverter<F, C>> converterClass = convert.converter();
			return Collections.<AttributeConverter<?, ?>>singletonList(createConverterInstance(converterClass, fmeta.getType()));
		}

		final Collection<Class<?>> nativeTypes = entityContext.getNativeTypes();
		if(nativeTypes.contains(type)) {
			return null;
		}

		if (type.isEnum()) {
			final Class<? extends Enum<?>> enumClass = (Class<? extends Enum<?>>)type;
			final Enumerated enumerated = fmeta.getAccessor().getAnnotation(Enumerated.class);

			EnumType enumType = EnumType.ORDINAL;
			if (enumerated != null) {
				enumType = enumerated.value();
			}

			AttributeConverter<F, C> converter = null;
			switch(enumType) {
				case ORDINAL:
					converter = new EnumOrdinalConverter(enumClass);
					break;
				case STRING:
					converter = new EnumNameConverter(enumClass);
					break;
				default: throw new IllegalArgumentException("Illegal value of EnumType: " + enumType);
			}
			return Collections.<AttributeConverter<?, ?>>singletonList(converter);
		}



		//TODO: add support of other types of Cassandra.
		//The problem is that this layer "does not know" Cassandra.
		//Probably add such method to persistor or add mechanism that registers default converters.
		if(type.isPrimitive() || String.class.equals(type) || Primitives.isWrapperType(type)) {
			return Collections.<AttributeConverter<?, ?>>singletonList(new DummyAttributeConverter<>());
		}

		if (Serializable.class.isAssignableFrom(type)) {
			return Collections.<AttributeConverter<?, ?>>singletonList(new SerializableConverter<>());
		}


		if (fmeta.getAccessor().getAnnotation(Embedded.class) != null && type.getAnnotation(Embeddable.class) != null) {
			return Collections.<AttributeConverter<?, ?>>singletonList(new BeanConverter<>(type, entityContext));
		}

		//TODO: throw exception?
		//TODO: change the method to return 1 type or use this method for collections and maps: in this case it will return collection of 2 elements and 3 elements respectively
		return null;
	}


	private <F, C> AttributeConverter<F, C> createConverterInstance(final Class<AttributeConverter<F, C>> converterClass, final Class<?> type) {
		try {
			try {
				return converterClass.getConstructor().newInstance();
			} catch (NoSuchMethodException | SecurityException e) {
				return converterClass.getConstructor(Class.class).newInstance(type);
			}
		} catch (final ReflectiveOperationException e1) {
			throw new IllegalArgumentException(e1);
		}

	}

	private <T> void processNamedQueries(final EntityMetadata<T> emeta) {
		final Class<T> clazz = emeta.getEntityType();
		final NamedQuery namedQuery = clazz.getAnnotation(NamedQuery.class);
		if (namedQuery != null) {
			processNamedQuery(emeta, namedQuery);
		}
		final NamedQueries namedQueries = clazz.getAnnotation(NamedQueries.class);
		if (namedQueries != null) {
			for (final NamedQuery nq : namedQueries.value()) {
				processNamedQuery(emeta, nq);
			}
		}
	}


	private <T> void processNamedQuery(final EntityMetadata<T> emeta, final NamedQuery namedQuery) {
		//TODO add support of other attributes of NamedQuery
		emeta.addNamedQuery(namedQuery.name(), namedQuery.query());
	}


	private <T> void updateIdGenerator(final EntityMetadata<T> entityMetadata) {
		for (FieldMetadata<T, Object, Object> fmd : entityMetadata.getFields()) {
			if (!fmd.isKey()) {
				continue;
			}
			GeneratedValue generatedValue = fmd.getAccessor().getAnnotation(GeneratedValue.class);
			if (generatedValue == null) {
				continue;
			}

			String generator = generatedValue.generator();
			GenerationType generationType = GenerationType.valueOf(generatedValue.strategy().name());
			fmd.setIdGenerator(new IdGeneratorMetadata<>(fmd.getClassType(), fmd.getType(), generationType, generator));
		}
	}


	private <T> void updateFieldAttributes(final EntityMetadata<T> entityMetadata) {
		final CriteriaLanguageParser parser = entityContext.getCriteriaLanguageParser();
		final Collection<String> filterFields = new HashSet<>();
		final Collection<String> sorterFields = new HashSet<>();

		for (final String query : entityMetadata.getNamedQueries().values()) {
			final QueryInfo queryInfo = parser.createCriteria(query, entityMetadata.getEntityType());
			filterFields.addAll(findFilterFields(queryInfo.getWhere()));

			final Collection<OrderByInfo> orders = queryInfo.getOrders();
			if (orders != null) {
				sorterFields.addAll(Collections2.transform(orders, new Function<OrderByInfo, String>() {
					@Override
					public String apply(final OrderByInfo info) {
						return info.getField();
					}
				}));
			}
		}

		for (final String filterField : filterFields) {
			entityMetadata.getFieldByName(filterField).setFilter(true);
		}

		for (final String sorterField : sorterFields) {
			entityMetadata.getFieldByName(sorterField).setSorter(true);
		}

	}

	private Collection<String> findFilterFields(final ClauseInfo clause) {
		return findFilterFields(clause, new LinkedHashSet<String>());
	}

	private Collection<String> findFilterFields(final ClauseInfo clause, final Set<String> fields) {
		if (clause == null) {
			return fields;
		}
		final String field = clause.getField();
		if (field != null) {
			fields.add(field);
		}
		final Object expression = clause.getExpression();
		if (expression == null) {
			return fields;
		}
		if (expression instanceof ClauseInfo) {
			findFilterFields((ClauseInfo)expression, fields);
		}
		if(expression.getClass().isArray()) {
			final int n = Array.getLength(expression);
			for (int i = 0; i < n; i++) {
				final Object element = Array.get(expression, i);
				if (element instanceof ClauseInfo) {
					findFilterFields((ClauseInfo)element, fields);
				}
			}
		}
		return fields;
	}


	private <E extends Enum<?>> void addCascadeOption(Set<CascadeOption> cascadeOptions, E[] jpaOptions) {
		if(jpaOptions == null) {
			return;
		}

		for (E option : jpaOptions) {
			addCascadeOption(cascadeOptions, option);
		}
	}


	private <E extends Enum<?>> void addCascadeOption(Set<CascadeOption> cascadeOptions, E jpaOption) {
		if(jpaOption == null) {
			return;
		}
		Collection<CascadeOption> options = cascade.get(jpaOption);
		if(options == null) {
			return;
		}
		cascadeOptions.addAll(options);
	}
}
