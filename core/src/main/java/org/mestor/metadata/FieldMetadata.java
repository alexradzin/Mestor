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

package org.mestor.metadata;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.mestor.reflection.PropertyAccessor;

public class FieldMetadata<T, F, C> {
	private Class<T> classType;
	private Class<F> type;
	private Class<C> columnType;
	private String name;
	private String column;
	private boolean nullable;
	private Set<FieldRole> role = EnumSet.noneOf(FieldRole.class);
	private boolean lazy = false;
	private Collection<Class<?>> genericTypes = new ArrayList<Class<?>>();
	private Collection<Class<?>> columnGenericTypes = new ArrayList<Class<?>>();
	private List<ValueConverter<?, ?>> converters = new ArrayList<>();
	private F defaultValue;

	private PropertyAccessor<T, F> accessor;

	public FieldMetadata(Class<T> classType, Class<F> type, String name) {
		this(classType, type, name, null, null, null);
	}
	
	@SuppressWarnings("unchecked")
	public FieldMetadata(Class<T> classType, Class<F> type, String name, Field field, Method getter, Method setter) {
		this(classType, type, (Class<C>)type, name, field, getter, setter, new DummyValueConverter<F, C>(), new DummyValueConverter<F, C>(), new DummyValueConverter<F, C>());
	}
	
	@SafeVarargs
	public FieldMetadata(Class<T> classType, Class<F> type, Class<C> columnType, String name, Field field, Method getter, Method setter, ValueConverter<F, C> ...converters) {
		this(classType, type, columnType, new PropertyAccessor<T, F>(classType, type, name, field, getter, setter), converters);
		this.name = name;
	}

	@SafeVarargs
	public FieldMetadata(Class<T> classType, Class<F> type, Class<C> columnType, PropertyAccessor<T, F> accessor, ValueConverter<F, C> ...converters) {
		this.accessor = accessor;
		this.classType = classType;
		this.type = type;
		this.columnType = columnType;
		this.converters.addAll(Arrays.asList(converters));
	}
	
	

	public Class<T> getClassType() {
		return classType;
	}


	public void setClassType(Class<T> classType) {
		this.classType = classType;
	}

	public Class<F> getType() {
		return type;
	}


	public void setType(Class<F> type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public String getColumn() {
		return column;
	}


	public void setColumn(String column) {
		this.column = column;
	}


	public boolean isNullable() {
		return nullable;
	}


	public void setNullable(boolean nullable) {
		this.nullable = nullable;
	}

	
	public boolean isKey() {
		return isFieldInRole(FieldRole.PRIMARY_KEY);
	}


	public void setKey(boolean key) {
		setFieldRole(FieldRole.PRIMARY_KEY, key);
	}


	public boolean isDiscriminator() {
		return isFieldInRole(FieldRole.DISCRIMINATOR);
	}


	public void setDiscriminator(boolean discriminator) {
		setFieldRole(FieldRole.DISCRIMINATOR, discriminator);
	}

	public boolean isJoiner() {
		return isFieldInRole(FieldRole.JOINER);
	}


	public void setJoiner(boolean discriminator) {
		setFieldRole(FieldRole.JOINER, discriminator);
	}
	
	
	private void setFieldRole(FieldRole fieldRole, boolean onoff) {
		if (onoff) {
			role.add(fieldRole);
		} else {
			role.remove(fieldRole);
		}
	}
	
	private boolean isFieldInRole(FieldRole fieldRole) {
		return role.contains(fieldRole);
	}
	
	public void setField(Field field) {
		accessor = new PropertyAccessor<T, F>(accessor.getType(), accessor.getPropertyType(), accessor.getName(), field, accessor.getGetter(), accessor.getSetter());
	}


	public void setGetter(Method getter) {
		accessor = new PropertyAccessor<T, F>(accessor.getType(), accessor.getPropertyType(), accessor.getName(), accessor.getField(), getter, accessor.getSetter());
	}


	public void setSetter(Method setter) {
		accessor = new PropertyAccessor<T, F>(accessor.getType(), accessor.getPropertyType(), accessor.getName(), accessor.getField(), accessor.getGetter(), setter);
	}


	public PropertyAccessor<T, F> getAccessor() {
		return accessor;
	}


	public void setAccessor(PropertyAccessor<T, F> accessor) {
		this.accessor = accessor;
	}


	public boolean isLazy() {
		return lazy;
	}


	public void setLazy(boolean lazy) {
		this.lazy = lazy;
	}


	public Collection<Class<?>> getGenericTypes() {
		return genericTypes;
	}


	public void setGenericTypes(Collection<Class<?>> genericTypes) {
		this.genericTypes = genericTypes;
	}

	public Class<C> getColumnType() {
		return columnType;
	}

	public void setColumnType(Class<C> columnType) {
		this.columnType = columnType;
	}

	
	public Collection<Class<?>> getColumnGenericTypes() {
		return columnGenericTypes;
	}


	public void setColumnGenericTypes(Collection<Class<?>> columnGenericTypes) {
		this.columnGenericTypes = columnGenericTypes;
	}
	
	
	
	public ValueConverter<F, C> getConverter() {
		return getConverter(0);
	}
	
	@SuppressWarnings("unchecked")
	public <F1, C1> ValueConverter<F1, C1> getConverter(int i) {
		return (ValueConverter<F1, C1>)converters.get(i);
	}

	public void setConverter(ValueConverter<F, C> primary, ValueConverter<?, ?> ... secondary) {
		converters.clear();
		converters.add(primary);
		if (secondary != null) {
			converters.addAll(Arrays.asList(secondary));
		}
	}
	
	public void setDefaultValue(F defaultValue) {
		this.defaultValue = defaultValue;
	}
	
	public <E> Object getDefaultValue() {
		if (defaultValue != null) {
			return defaultValue;
		}
		// default value was not explicitly defined, so we have to discover it. 
		if (!type.isPrimitive()) {
			return null;
		}
		// this is a primitive
		if (int.class.equals(type) || long.class.equals(type) || short.class.equals(type) || byte.class.equals(type)) {
			return 0;
		}
		if (float.class.equals(type)) {
			return 0.0f;
		}
		if (double.class.equals(type)) {
			return 0.0;
		}
		if (boolean.class.equals(type)) {
			return false;
		}
		return null;
	}
	
}
