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

package org.mestor.metadata.jpa.conversion;

import java.lang.reflect.AccessibleObject;

import org.mestor.context.EntityContext;
import org.mestor.metadata.EntityMetadata;
import org.mestor.metadata.FieldMetadata;
import org.mestor.reflection.Access;
import org.mestor.reflection.ClassAccessor;

/**
 * Implementation of {@link SelfReferenceConverter} and {@link Access} that converts between
 * primary key and corresponding entity instance.
 * 
 * <i>Not in use for the moment</i>
 * 
 * @deprecated This class is not used right now and will probably be removed. 
 * 
 * @author alexr
 *
 * @param <E>
 * @param <P>
 * @param <K>
 */
@Deprecated
public class SelfReferenceConverter<E, P, K> extends PrimaryKeyConverter<P, K> implements Access<E, P, AccessibleObject> {
	/**
	 * This constructor is needed to simplify generic access to functionality of this
	 * converter. The convention is that converter can be either stateless and implement 
	 * default constructor or be dependent on the entity type and context. 
	 * @param clazz
	 * @param context
	 */
	public SelfReferenceConverter(Class<P> clazz, EntityContext context) {
		super(clazz, context);
	}
	
	
	@Override
	public K convertToDatabaseColumn(P entity) {
		if (entity == null) {
			return null;
		}
		// store record in parent table
		persistor.store(get(entity, entity.getClass()));
		
		@SuppressWarnings("unchecked")
		K key = (K)getEntityMetadata().getPrimaryKey().getAccessor().getValue(entity);
		
		return key;
	}

	@Override
	public P convertToEntityAttribute(K primaryKey) {
		return null;
	}

	
	@SuppressWarnings("unchecked")
	@Override
	public P get(E entity) {
		return (P)get(entity, entity.getClass().getSuperclass());
	}

	
	private Object get(Object entity, Class<?> type) {
		// store record in parent table
		for (Class<?> c = type; !Object.class.equals(c); c = c.getSuperclass()) {
			@SuppressWarnings("unchecked")
			EntityMetadata<Object> parentEmd = (EntityMetadata<Object>)context.getEntityMetadata(c);
			if (parentEmd == null) {
				continue;
			}
			Object parent = ClassAccessor.newInstance(c);
			copyFields(entity, parent, parentEmd);
			return parent;
		}
		
		throw new IllegalArgumentException("Parent entity for " + entity.getClass() + " is not found");
	}
	
	
	
	
	@Override
	public void set(E entity, P parentKey) {
		for (Class<?> c = entity.getClass().getSuperclass(); !Object.class.equals(c); c = c.getSuperclass()) {
			@SuppressWarnings("unchecked")
			EntityMetadata<Object> parentEmd = (EntityMetadata<Object>)context.getEntityMetadata(c);
			if (parentEmd == null) {
				continue;
			}
			Object parent = persistor.fetch(c, parentKey);
			copyFields(parent, entity, parentEmd);
			return;
		}
		
		throw new IllegalStateException("Super entity for " + entity.getClass() + " is not found");
	}
	
	private <T> void copyFields(T src, T target, EntityMetadata<T> targetEmd) {
		for (FieldMetadata<T, Object, Object> fmd : targetEmd.getFields()){
			Object parentValue = fmd.getAccessor().getValue(src);
			fmd.getAccessor().setValue(target, parentValue);
		}
	}
	
}
