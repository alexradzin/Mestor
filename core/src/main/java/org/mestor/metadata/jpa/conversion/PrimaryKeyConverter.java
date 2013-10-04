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

import javax.persistence.AttributeConverter;

import org.mestor.context.EntityContext;
import org.mestor.context.Persistor;
import org.mestor.metadata.EntityMetadata;

public class PrimaryKeyConverter<E, K> implements AttributeConverter<E, K> {
	protected final Persistor persistor;
	/**
	 * {@see #getEntityMetadata()} for reasons why this field is not final
	 */
	protected EntityMetadata<E> emd;
	protected final Class<E> clazz;
	protected final EntityContext context;

	/**
	 * This constructor is needed to simplify generic access to functionality of this
	 * converter. The convention is that converter can be either stateless and implement 
	 * default constructor or be dependent on the entity type and context. 
	 * @param clazz
	 * @param context
	 */
	public PrimaryKeyConverter(Class<E> clazz, EntityContext context) {
		this.emd = context.getEntityMetadata(clazz);
		this.persistor = context.getPersistor();
		this.clazz = clazz;
		this.context = context;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public K convertToDatabaseColumn(E entity) {
		return entity == null ? null : (K)getEntityMetadata().getPrimaryKey().getAccessor().getValue(entity);
	}

	@Override
	public E convertToEntityAttribute(K primaryKey) {
		if (primaryKey == null) {
			return null;
		}
		E dummyEntity = persistor.getObjectWrapperFactory(clazz).makeLazy(clazz, primaryKey);
		@SuppressWarnings("unchecked")
		E existingEntity = (E)Persistor.fetchContext.get().get(dummyEntity);
		if (existingEntity == null) {
			existingEntity = persistor.fetch(getEntityMetadata().getEntityType(), primaryKey);
		}
		
		return existingEntity;
	}

	/**
	 * This method returns Entity metadata if it has been already initialized or retrieves it 
	 * from context if otherwise. Entity metadata can be {@code null} at this point because 
	 * probably instance of this converter was created before the entity metadata for the class was 
	 * registered in context. 
	 * @return
	 */
	protected EntityMetadata<E> getEntityMetadata() {
		if (emd != null) {
			return emd;
		}
		emd = context.getEntityMetadata(clazz);
		return emd;
	}
}
