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
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nullable;
import javax.persistence.AttributeConverter;
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
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.mestor.context.EntityContext;
import org.mestor.metadata.BeanMetadataFactory;
import org.mestor.metadata.DummyValueConverter;
import org.mestor.metadata.EntityMetadata;
import org.mestor.metadata.FieldMetadata;
import org.mestor.metadata.jpa.conversion.BeanConverter;
import org.mestor.metadata.jpa.conversion.DummyAttributeConverter;
import org.mestor.metadata.jpa.conversion.EnumNameConverter;
import org.mestor.metadata.jpa.conversion.EnumOrdinalConverter;
import org.mestor.metadata.jpa.conversion.IndexedFieldConverter;
import org.mestor.metadata.jpa.conversion.PrimaryKeyConverter;
import org.mestor.metadata.jpa.conversion.SerializableConverter;
import org.mestor.metadata.jpa.conversion.ValueAttributeConverter;
import org.mestor.reflection.Access;
import org.mestor.reflection.CompositePropertyAccessor;
import org.mestor.reflection.FieldAccessor;
import org.mestor.reflection.MethodAccessor;
import org.mestor.reflection.PropertyAccessor;
import org.mestor.reflection.jpa.DiscriminatorValueAccess;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.primitives.Primitives;

@DiscriminatorColumn
@Inheritance
public class JpaAnnotationsMetadataFactory extends BeanMetadataFactory {
	private final Map<NamableItem, NamingStrategy> namingStrategies = new EnumMap<NamableItem, NamingStrategy>(NamableItem.class) {{
		for(NamableItem i : NamableItem.values()) {
			put(i, StandardNamingStrategy.LOWER_CASE_UNDERSCORE);
		}
	}};
	private EntityContext context;

	public JpaAnnotationsMetadataFactory(final Map<NamableItem, NamingStrategy> namingStrategies) {
		this();
		this.namingStrategies.putAll(namingStrategies);
	}
	
	public JpaAnnotationsMetadataFactory() {
	}

	public void setNamingStrategy(NamableItem item, NamingStrategy namingStrategy) {
		this.namingStrategies.put(item, namingStrategy);
	}
	
	public void setEntityContext(EntityContext context) {
		this.context = context;
	}

	@Override
	public <T> EntityMetadata<T> create(final Class<T> clazz) {
		final Entity entity = clazz.getAnnotation(Entity.class);
		if (entity == null) {
			return null;
		}
		
		EntityMetadata<T> emeta = getOrCreate(clazz);
		
		// discover entity name
		emeta.setEntityName(extractName(entity, namingStrategies.get(NamableItem.ENTITY).getEntityName(clazz)));
		
		Table table = clazz.getAnnotation(Table.class);
		emeta.setTableName(extractName(new Object[] {table, entity}, namingStrategies.get(NamableItem.TABLE).getTableName(clazz)));
		if (table != null) {
			String schema = table.schema();
			if (Strings.isNullOrEmpty(schema)) {
				schema = this.getSchema();
			}
			
			emeta.setSchemaName(schema); 
		}

		// name to metadata
		Map<String, FieldMetadata<T, Object, Object>> fields = new LinkedHashMap<>();
		
		for (Field f : FieldAccessor.getFields(clazz)) {
			if (isTransient(f)) {
				continue;
			}
			
			@SuppressWarnings("unchecked")
			Class<Object> type = (Class<Object>)f.getType();
			final String name = getFieldName(f);
			FieldMetadata<T, Object, Object> fmeta = create(clazz, type, name);
			
			fmeta.setField(f);
			initMeta(fmeta, f, name, clazz, type);
			
			fields.put(name, fmeta);
			
		}
		
		for (Method m : clazz.getMethods()) {
			if(!MethodAccessor.isGetter(m)) {
				continue;
			}

			String fieldName = getFieldName(m);
			FieldMetadata<T, Object, Object> fmeta = fields.get(fieldName);
			
			
			if (isTransient(m)) {
				if (fmeta != null) {
					fields.remove(fieldName);
				}
				continue;
			}
			
			if (fmeta == null) {
				@SuppressWarnings("unchecked")
				Class<Object> type = (Class<Object>)m.getReturnType();
				fmeta = create(clazz, type, fieldName);
				initMeta(fmeta, m, fieldName, clazz, type);
			}
			fmeta.setGetter(m);
		}
		
		for (Method m : clazz.getMethods()) {
			if(!MethodAccessor.isSetter(m)) {
				continue;
			}
			String fieldName = getFieldName(m);
			FieldMetadata<T, Object, Object> fmeta = fields.get(fieldName);
			
			if (fmeta == null) {
				continue;
			}
			fmeta.setSetter(m);
		}

		
		Collection<String> primaryKeyFields = new ArrayList<>();
		Collection<PropertyAccessor<T, ? extends Object>> primaryKeyAccessors = new ArrayList<>();
		
		for (Entry<String, FieldMetadata<T, Object, Object>> entry : fields.entrySet()) {
			FieldMetadata<T, ? extends Object, ? extends Object> fmeta = entry.getValue();
					
			if (fmeta.isKey()) {
				primaryKeyFields.add(entry.getKey());
				primaryKeyAccessors.add(fmeta.getAccessor());
			}
		}

		
		if (primaryKeyFields.isEmpty()) {
			throw new IllegalArgumentException("Entity " + clazz + " does not have primary key");
		}

		
		for (FieldMetadata<T, ?, ?> fmd : fields.values()) {
			emeta.addField(fmd);
		}


		emeta.setPrimaryKey(fields.get(primaryKeyFields.iterator().next()));
		
		if (primaryKeyFields.size() > 1) {
			final IdClass idClass = clazz.getAnnotation(IdClass.class);
			if (idClass == null) {
				throw new IllegalArgumentException("Entity " + clazz + " has ");
			}
			
			@SuppressWarnings("unchecked")
			Class<Object> idClazz = idClass.value();
			
			String name = Joiner.on("_").join(primaryKeyFields);
			
			@SuppressWarnings("unchecked")
			FieldMetadata<T, Object, Object> keyMetadata = new FieldMetadata<>(clazz, idClass.value(), name);
			
			@SuppressWarnings("unchecked")
			PropertyAccessor<T, Object>[] primaryKeyAccessorsArr = primaryKeyAccessors.toArray(new PropertyAccessor[primaryKeyAccessors.size()]); 
			keyMetadata.setAccessor(new CompositePropertyAccessor<T, Object>(clazz, idClazz, name, primaryKeyAccessorsArr));
			
			emeta.setPrimaryKey(keyMetadata);
		}

		
		emeta.addAllIndexes(findIndexes(emeta));
		
		return emeta;
	}

	
	private <T, F, C> void setCollectionConverter(FieldMetadata<T, F, C> fmd) {
		AttributeConverter<F, C> converter = fmd.isKey() ?  
				new PrimaryKeyConverter<F, C>(fmd.getType(), context) :
				new IndexedFieldConverter<F, C>(fmd.getType(), fmd.getName(), context);
		
		fmd.setConverter(
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
	private <T, P, C> void parseCollectionElementType(Map<Class<?>, EntityMetadata<?>> metadata, EntityMetadata<T> emd, FieldMetadata<T, P, C> fmd) {
		Class<?> collectionType = fmd.getType();
		if (!Collection.class.isAssignableFrom(collectionType)) {
			return;
		}

		
		Type collectionGenericType = fmd.getAccessor().getGenericType();
		Type discoveredType = null;
		if (collectionGenericType instanceof ParameterizedType) {
			discoveredType = ((ParameterizedType)collectionGenericType).getActualTypeArguments()[0];
		}
		
		
		Type explicitType = null;
		String mappedBy = "";
		
		OneToMany oneToMany = fmd.getAccessor().getAnnotation(OneToMany.class);
		ManyToMany manyToMany = fmd.getAccessor().getAnnotation(ManyToMany.class);
		ElementCollection elementCollection = fmd.getAccessor().getAnnotation(ElementCollection.class);
		
		if (!(oneToMany != null ^ manyToMany != null ^ elementCollection != null)) {
			throw new IllegalArgumentException("Only one of OneToMany, ManyToMany, ElementCollection can be used for one field (" + fmd.getName() + ")");
		}
		
		
//		Class<?> collectionElementClass = null;
		
		
		if (oneToMany != null) {
			explicitType = oneToMany.targetEntity();
			mappedBy = oneToMany.mappedBy();
		} else if (manyToMany != null) {
			explicitType = manyToMany.targetEntity();
			mappedBy = manyToMany.mappedBy();
		} else if (elementCollection != null) {
			explicitType = elementCollection.targetClass();
		} else if (fmd.getAccessor().getAnnotation(Convert.class) != null) {
			return; //this collection has explicitly defined converter.  
		} else if (fmd.getAccessor().getAnnotation(Embedded.class) != null && discoveredType != null) {
			//collectionElementClass = castTypeToClass(collectionElementType);
		} else {
			throw new IllegalArgumentException("Cannot identify type of collection " + fmd.getClassType() + "." + fmd.getName());
		}
		
		
		if (!"".equals(mappedBy)) {
			@SuppressWarnings("unchecked")
			EntityMetadata<Object> ref = (EntityMetadata<Object>)metadata.get(explicitType);
			ref.addIndex(ref.getFieldByName(mappedBy)); 
		}
		
		if (explicitType != null && !void.class.equals(explicitType)) {
			discoveredType = explicitType;
		}

		
		
		Class<?> elementType = castTypeToClass(discoveredType);
		fmd.setGenericTypes(elementType);
		
		
		AttributeConverter<P, C> converter = null;
		
		if (oneToMany != null || manyToMany != null) {
			FieldMetadata<T, ?, ?> filterField = getCollectionElementBackReferenceField(metadata, emd, fmd);
			setCollectionConverter(filterField);
			
			converter = fmd.isKey() ?  
					new PrimaryKeyConverter<P, C>(fmd.getType(), context) :
					new IndexedFieldConverter<P, C>(fmd.getType(), fmd.getName(), context);
			
		}

		if (elementCollection != null) {
			List<AttributeConverter<?, ?>> converters = findConverters(fmd);
			if (converters != null && !converters.isEmpty()) {
				@SuppressWarnings("unchecked")
				AttributeConverter<P, C> castConv = (AttributeConverter<P, C>)converters.get(0);
				converter = castConv;
			}
		}
		
		
		if (converter != null) {
			fmd.setConverter(new DummyValueConverter<P, C>(), new ValueAttributeConverter<>(converter));
		}
	}

	//TOOD: add support of all attributes of ManyToOne
	private <T, P> void parseCollectionElementBackReference(Map<Class<?>, EntityMetadata<?>> metadata, FieldMetadata<T, P, Object> fmd) {
		ManyToOne manyToOne = fmd.getAccessor().getAnnotation(ManyToOne.class);
		if (manyToOne == null) {
			return;
		}
		
		Class<P> fieldType = fmd.getType();
		
		Class<?> type = manyToOne.targetEntity(); 
		if (!void.class.equals(type)) {
			fieldType = castTypeToClass(type);
		}
		
		// TODO: do the same for OneToMany and ManyToMany

		fmd.setConverter(new ValueAttributeConverter<P, Object>(new PrimaryKeyConverter<P, Object>(fieldType, context)));
		FieldMetadata<P, Object, Object> secondSidePK = context.getEntityMetadata(fieldType).getPrimaryKey();
		
		
		String columnName = fmd.getColumn() + "_" + secondSidePK.getColumn();
		fmd.setColumn(columnName);
		fmd.setColumnType(secondSidePK.getColumnType());
	}
	

	private <T, P> FieldMetadata<T, Object, Object> getCollectionElementBackReferenceField(Map<Class<?>, EntityMetadata<?>> metadata, EntityMetadata<T> emd, FieldMetadata<T, P, ?> fmd) {
		JoinColumn joinColumn = fmd.getAccessor().getAnnotation(JoinColumn.class);
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
	private <C> Class<C> castTypeToClass(Type type) {
		return (Class<C>)type;
	}

	private <T> EntityMetadata<T> getOrCreate(final Class<T> clazz) {
		EntityMetadata<T> meta = context.getEntityMetadata(clazz);
		if (meta == null) {
			meta = new EntityMetadata<T>(clazz); 
		}
		return meta;
	}

	
	private String extractName(Object nameHolder, String defaultValue) {
		try {
			String name = nameHolder == null ? null : (String)nameHolder.getClass().getMethod("name").invoke(nameHolder);
			return extractParameter(name, defaultValue);
		} catch (ReflectiveOperationException e) {
			throw new IllegalArgumentException(nameHolder.getClass() + " does not provide method name()");
		}
	}
	
	private String extractName(Object[] nameHolders, String defaultValue) {
			if (nameHolders == null || nameHolders.length == 0) {
				return defaultValue;
			}
			for (Object nameHolder : nameHolders) {
				try {
					String name = nameHolder == null ? null : (String)nameHolder.getClass().getMethod("name").invoke(nameHolder);
					if(!Strings.isNullOrEmpty(name)) {
						return name;
					}
				} catch (ReflectiveOperationException e) {
					throw new IllegalArgumentException(nameHolder.getClass() + " does not provide method name()");
				}
			}
			return defaultValue;
	}
	
	
	
	private String extractParameter(String value, String defaultValue) {
		return Strings.isNullOrEmpty(value) ? defaultValue : value;
	}
	

	
	
	private <T, F, C> void initMeta(FieldMetadata<T, F, C> fmeta, AccessibleObject ao, String memberName, Class<T> clazz, Class<F> memberType) {
		fmeta.setColumn(extractName(ao.getAnnotation(Column.class), namingStrategies.get(NamableItem.COLUMN).getColumnName(ao)));
		fmeta.setKey(ao.getAnnotation(Id.class) != null);
		
		boolean nullable = ao.getAnnotation(Nullable.class) != null;
		
		if (nullable && memberType.isPrimitive()) {
			throw new IllegalArgumentException("Primitive field " + memberName + " cannot be nullable");
		}
		
		fmeta.setNullable(nullable || !memberType.isPrimitive());
		
		
		List<AttributeConverter<?, ?>> converters = findConverters(fmeta); 
		
		if(converters != null && !converters.isEmpty()) {
			@SuppressWarnings("unchecked")
			AttributeConverter<F, C> converter = (AttributeConverter<F, C>)converters.remove(0);  
			fmeta.setConverter(new ValueAttributeConverter<>(converter), wrapConverters(converters).toArray(new ValueAttributeConverter[0]));
			
			Class<C> columnType = getColumnType(converter);
			if (columnType != null) {
				fmeta.setColumnType(columnType);
			}
			
			if (!converters.isEmpty()) {
				fmeta.setColumnGenericTypes(getColumnGenericTypes(converters));
			}
		}
	}
	
	private <F, C> Collection<ValueAttributeConverter<?,?>> wrapConverters(List<AttributeConverter<?, ?>> attributeConverters) {
		if (attributeConverters == null || attributeConverters.isEmpty()) {
			return Collections.emptyList();
		}
		
		Collection<ValueAttributeConverter<?,?>> valueConverters = new ArrayList<>();
		for (AttributeConverter<?, ?> ac : attributeConverters) {
			valueConverters.add(new ValueAttributeConverter<>(ac));
		}
		
		return valueConverters;
	}
	
	
	private <C> Class<C> getColumnType(AttributeConverter<?, C> converter) {
		for (Type iface : converter.getClass().getGenericInterfaces()) {
			if (!(iface instanceof ParameterizedType)) {
				continue;
			}
			ParameterizedType piface = (ParameterizedType)iface;
			if (!AttributeConverter.class.equals(piface.getRawType())) {
				continue;
			}
			Type typeArgument = piface.getActualTypeArguments()[1];
			if (typeArgument instanceof Class) {
				@SuppressWarnings("unchecked")
				Class<C> columnType = (Class<C>)piface.getActualTypeArguments()[1];
				return columnType;
			}
			return null;
		}
		throw new IllegalArgumentException("Cannot extract column type ");
	}
	
	
	private Collection<Class<?>> getColumnGenericTypes(Collection<AttributeConverter<?, ?>> converters) {
		Collection<Class<?>> types = new ArrayList<>();
		
		for (AttributeConverter<?, ?> converter : converters) {
			types.add(getColumnType(converter));
		}
		
		return types;
	}
	
	
	private boolean isTransient(AccessibleObject ao) {
		int mod = ((Member)ao).getModifiers();
		Transient tr = ao.getAnnotation(Transient.class);
		return Modifier.isTransient(mod) || tr != null;
	}
	
	@Override
	public void update(Map<Class<?>, EntityMetadata<?>> metadata) {
		for (EntityMetadata<?> emd : metadata.values()) {
			updateInheritance(metadata, emd);
			updateRelationships(metadata, emd);
		}
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
	

	private <E> void updateInheritance(Map<Class<?>, EntityMetadata<?>> metadata, EntityMetadata<E> emd) {
		Class<E> clazz = emd.getEntityType();
		Inheritance inheritance = getInheritance(clazz);
		InheritanceType inheritenceType = inheritance.strategy();

		if (InheritanceType.TABLE_PER_CLASS.equals(inheritenceType) && Modifier.isAbstract(clazz.getModifiers())) {
			emd.setTableName(null); // abstract base classes in TABLE_PER_CLASS hierarchy do not need their table.  
		}
		
		
		DiscriminatorValue discriminatorValue = clazz.getAnnotation(DiscriminatorValue.class);
		if (discriminatorValue == null) {
			return;
		}
		String discriminatorValueValue = discriminatorValue.value();
		DiscriminatorColumn discriminatorColumn = getDiscriminatorColumn(clazz);
		DiscriminatorType dtype = discriminatorColumn.discriminatorType();
		
		String dname = discriminatorColumn.name();
		
		//TODO: check whether this condition is enough to recognize sub class.
		boolean isSubClass = clazz.getAnnotation(Inheritance.class) == null;
		
		
		if (InheritanceType.SINGLE_TABLE.equals(inheritenceType) && isSubClass) { 
			dname = null; // sub class when single table is used. 
		}
		
		
		Access<E, Object, AccessibleObject> discriminatorAccess = new DiscriminatorValueAccess<E, Object>(clazz);
		@SuppressWarnings({ "rawtypes", "unchecked" })
		FieldMetadata<E, Object, Object> discriminator = new FieldMetadata(clazz, getDiscriminatorType(dtype), dname);
		discriminator.setAccessor(new PropertyAccessor<E, Object>(clazz, Object.class, dname, 
				null, null, null, // field, getter, setter 
				discriminatorAccess, discriminatorAccess));
		discriminator.setDiscriminator(true);
		discriminator.setDefaultValue(discriminatorValueValue);

		
		emd.addField(discriminator);
		
		if (InheritanceType.JOINED.equals(inheritenceType) && isSubClass) {
			FieldMetadata<E, Object, Object> joiner = createJoiner(metadata, clazz);
			emd.addField(joiner);
		}
	}
	
	
	
	private DiscriminatorColumn getDiscriminatorColumn(Class<?> type) {
		Class<?>[] classes = new Class[] {type, type.getSuperclass(), getClass()};
		
		for (Class<?> clazz : classes) {
			DiscriminatorColumn discriminatorColumn = clazz.getAnnotation(DiscriminatorColumn.class);
			if (discriminatorColumn != null) {
				return discriminatorColumn;
			}
		}
		
		throw new IllegalStateException("Cannot locate DiscriminatorColumn for class " + type);
	}
	
	private Inheritance getInheritance(Class<?> type) {
		for (Class<?> c = type; !Object.class.equals(c); c = c.getSuperclass()) {
			Inheritance inheritance = c.getAnnotation(Inheritance.class);
			if (inheritance != null) {
				return inheritance;
			}
		}
		return getClass().getAnnotation(Inheritance.class);
		
	}
	
	private Class<?> getDiscriminatorType(DiscriminatorType dtype) {
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
	
	private <E> FieldMetadata<E, Object, Object> createJoiner(Map<Class<?>, EntityMetadata<?>> metadata, Class<E> clazz) {
		Class<?> parent = clazz.getSuperclass();
		@SuppressWarnings("unchecked")
		EntityMetadata<E> parentMeta = (EntityMetadata<E>)metadata.get(parent);
		FieldMetadata<E, Object, ?> parentPk = parentMeta.getPrimaryKey();
		String column = parentMeta.getTableName().toLowerCase() + "_" + parentPk.getName();
		
		FieldMetadata<E, Object, Object> joiner = new FieldMetadata<>(clazz, parentPk.getType(), column);
		joiner.setColumn(column);
		joiner.setJoiner(true);
		
		return joiner;
	}

	
	private <E> void updateRelationships(Map<Class<?>, EntityMetadata<?>> metadata, EntityMetadata<E> emd) {
		// references to collections.
		for (FieldMetadata<E, Object, Object> fmd : emd.getFields()) {
			parseCollectionElementType(metadata, emd, fmd);
			parseCollectionElementBackReference(metadata, fmd);
		}
	}


	private <E, F, C> List<AttributeConverter<?, ?>> findConverters(FieldMetadata<E, F, C> fmeta) {
		Convert convert = fmeta.getAccessor().getAnnotation(Convert.class);
		List<AttributeConverter<?, ?>> converters;
		if(convert != null) {
			@SuppressWarnings("unchecked")
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
	private <E, F, C> List<AttributeConverter<?, ?>> findConverters(FieldMetadata<E, F, C> fmeta, Class<F> type) {
		Convert convert = fmeta.getAccessor().getAnnotation(Convert.class);
		if(convert != null) {
			Class<AttributeConverter<F, C>> converterClass = convert.converter();
			return Collections.<AttributeConverter<?, ?>>singletonList(createConverterInstance(converterClass, fmeta.getType()));
		}
		if (type.isEnum()) {
			Class<? extends Enum<?>> enumClass = (Class<? extends Enum<?>>)type;
			Enumerated enumerated = fmeta.getAccessor().getAnnotation(Enumerated.class);
			
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
			return Collections.<AttributeConverter<?, ?>>singletonList(new BeanConverter<>(type, context));		
		}

		//TODO: throw exception?
		//TODO: change the method to return 1 type or use this method for collections and maps: in this case it will return collection of 2 elements and 3 elements respetidly
		return null;
	}

	
	private <F, C> AttributeConverter<F, C> createConverterInstance(Class<AttributeConverter<F, C>> converterClass, Class<?> type) {
		try {
			try {
				return converterClass.getConstructor().newInstance();
			} catch (NoSuchMethodException | SecurityException e) {
				return converterClass.getConstructor(Class.class).newInstance(type);
			}
		} catch (ReflectiveOperationException e1) {
			throw new IllegalArgumentException(e1);
		}
		
	}
}