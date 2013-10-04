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

import java.util.Comparator;

import org.mestor.context.EntityContext;
import org.mestor.wrap.ObjectWrapperFactory;

public class EntityComparator<E> implements Comparator<E> {
	private final EntityContext context;
	
	public EntityComparator(EntityContext context) {
		this.context = context;
	}
	

	@Override
	public int compare(E one, E two) {
		if(one == null) {
			return two == null ? 0 : -1;
		}
		if(two == null) {
			return 1; // one is definitely not null here
		}
		// both not null
		Class<E> c1 = getRealClass(one);
		Class<E> c2 = getRealClass(two);
		
		// since Class is not Comparable we ought to compare full class names  
		int clazzComp = c1.getName().compareTo(c2.getName());
		if (clazzComp != 0) {
			return clazzComp;
		}
		
		EntityMetadata<E> m1 = getPrimaryKeyMetadata(c1, one);
		EntityMetadata<E> m2 = getPrimaryKeyMetadata(c2, two);
		
		if (m1 == null || m2 == null) {
			// at least one of them is not entity. Compare as regular objects
			return compareImpl(one, two);
		}
		
		FieldMetadata<E, ?, ?> pkMeta1 = m1.getPrimaryKey();
		FieldMetadata<E, ?, ?> pkMeta2 = m2.getPrimaryKey();

		if (pkMeta1 == null || pkMeta2 == null) {
			// at least one of them does not have primary key. Compare as regular objects
			return compareImpl(one, two);
		}
		
		// both are entities that have primary keys
		Object pk1 = pkMeta1.getAccessor().getValue(one);
		Object pk2 = pkMeta2.getAccessor().getValue(two);
		
		return compareImpl(pk1, pk2);
	}

	@SuppressWarnings("unchecked")
	public <T> int compareImpl(T one, T two) {
		if (one instanceof Comparable) {
			return ((Comparable<T>) one).compareTo(two);
		}
		return one.hashCode() - two.hashCode();
	}
	
	/**
	 * Retrieves metadata of not null primary key for specific class. 
	 * If primary key of current entity is null looks for primary key in its super class etc. 
	 * @param entityClass
	 * @param entity
	 * @return
	 */
	private EntityMetadata<E> getPrimaryKeyMetadata(Class<E> entityClass, E entity) {
		for (Class<?> c = entityClass; !Object.class.equals(c); c = c.getSuperclass()) {
			EntityMetadata<E> eMeta = context.getEntityMetadata(entityClass);
			FieldMetadata<E, ?, ?> pkMeta = eMeta.getPrimaryKey();
			Object pk = pkMeta.getAccessor().getValue(entity);
			if (pk != null) {
				return eMeta;
			}
		}
		
		return null;
	}
	
	private Class<E> getRealClass(E entity) {
		@SuppressWarnings("unchecked")
		Class<E> clazz = (Class<E>)entity.getClass();		
		ObjectWrapperFactory<E> owf = context.getPersistor().getObjectWrapperFactory(clazz);
		E instance = entity;
		if(owf.isWrapped(entity)) {
			instance = owf.unwrap(entity);
		}
		@SuppressWarnings("unchecked")
		Class<E> realClazz = (Class<E>)instance.getClass();
		return realClazz;
	}
}
