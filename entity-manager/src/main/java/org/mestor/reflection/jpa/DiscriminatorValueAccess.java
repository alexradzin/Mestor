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

package org.mestor.reflection.jpa;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Modifier;

import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.mestor.reflection.Access;

@DiscriminatorColumn
public class DiscriminatorValueAccess<T, D> implements Access<T, D, AccessibleObject> {
	private final Class<T> clazz;
	private final DiscriminatorValue discriminatorValue;
	private final DiscriminatorColumn discriminatorColumn;
	private final String entityName;
	
	public DiscriminatorValueAccess(Class<T> clazz) {
		this.clazz = clazz;
		discriminatorValue = clazz.getAnnotation(DiscriminatorValue.class);
		discriminatorColumn = getDiscriminatorColumn(clazz);
		Entity entity = clazz.getAnnotation(Entity.class);
		entityName = entity != null && !"".equals(entity.name()) ? entity.name() : clazz.getSimpleName(); 
	}
	

	@SuppressWarnings("unchecked")
	@Override
	public D get(T instance) {
		if (discriminatorValue != null) {
			return (D)discriminatorValue.value();
		}
		
		DiscriminatorType discriminatorType = discriminatorColumn.discriminatorType();
		
		switch (discriminatorType) {
			case STRING:
				return (D)entityName;
			case CHAR:
				throw new IllegalArgumentException("Cannot create implicit discriminator of type ");
			case INTEGER:
				return (D)Integer.valueOf(clazz.hashCode());
			default: 
				throw new IllegalArgumentException("Unsupported DiscriminatorType " + discriminatorType);
		}
	}

	@Override
	public void set(T instance, D parameter) {
		// Nothing to do. Class name is a fake field. 
	}

	@Override
	public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
		return clazz.isAnnotationPresent(annotationClass);
	}

	@Override
	public <N extends Annotation> N getAnnotation(Class<N> annotationClass) {
		return clazz.getAnnotation(annotationClass);
	}

	@Override
	public Annotation[] getAnnotations() {
		return clazz.getAnnotations();
	}

	@Override
	public Annotation[] getDeclaredAnnotations() {
		return clazz.getDeclaredAnnotations();
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


	@Override
	public Class<?> getDeclaringClass() {
		return clazz;
	}


	@Override
	public String getName() {
		return clazz.getName();
	}


	@Override
	public int getModifiers() {
		return Modifier.PUBLIC;
	}


	@Override
	public boolean isSynthetic() {
		return true;
	}

}
