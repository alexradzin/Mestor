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

import org.mestor.reflection.PropertyAccessor;

public class FieldMetadata<T, F> {
	private Class<T> classType;
	private Class<F> type;
	private String name;
	private String column;
	private boolean nullable;
	private boolean key;
	private boolean lazy = false;

	private PropertyAccessor<T, F> accessor;

	
	public FieldMetadata(Class<T> classType, Class<F> type, String name) {
		accessor = new PropertyAccessor<T, F>(classType, name, null, null, null);
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
		return key;
	}


	public void setKey(boolean key) {
		this.key = key;
	}


	public void setField(Field field) {
		accessor = new PropertyAccessor<T, F>(accessor.getType(), accessor.getName(), field, accessor.getGetter(), accessor.getSetter());
	}


	public void setGetter(Method getter) {
		accessor = new PropertyAccessor<T, F>(accessor.getType(), accessor.getName(), accessor.getField(), getter, accessor.getSetter());
	}


	public void setSetter(Method setter) {
		accessor = new PropertyAccessor<T, F>(accessor.getType(), accessor.getName(), accessor.getField(), accessor.getGetter(), setter);
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
	
	
}
