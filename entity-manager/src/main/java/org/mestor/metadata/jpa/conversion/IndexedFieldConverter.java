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

import java.util.Collections;
import java.util.Map;

import javax.persistence.AttributeConverter;

import org.mestor.context.EntityContext;
import org.mestor.context.Persistor;
import org.mestor.metadata.EntityMetadata;
import org.mestor.metadata.FieldMetadata;

public class IndexedFieldConverter<E, F> implements AttributeConverter<E, F> {
	protected final Persistor persistor;
	/**
	 * {@see #getEntityMetadata()} for reasons why this field is not final
	 */
	protected final String fieldName;
	protected final Class<E> clazz;
	protected final EntityContext context;
	
	protected EntityMetadata<E> emd;
	protected FieldMetadata<E, F, ?> fmd;

	/**
	 * This constructor is needed to simplify generic access to functionality of this
	 * converter. The convention is that converter can be either stateless and implement 
	 * default constructor or be dependent on the entity type and context. 
	 * @param clazz
	 * @param context
	 */
	public IndexedFieldConverter(Class<E> clazz, String fieldName, EntityContext context) {
		this.persistor = context.getPersistor();
		this.clazz = clazz;
		this.context = context;
		this.fieldName = fieldName;
		this.emd = context.getEntityMetadata(clazz);
		if (this.emd != null) {
			this.fmd = getFieldMetadata();
		}
	}
	
	@Override
	public F convertToDatabaseColumn(E entity) {
		return entity == null ? null : getFieldMetadata().getAccessor().getValue(entity);
	}

	@Override
	public E convertToEntityAttribute(F fieldValue) {
		if (fieldValue == null) {
			return null;
		}
		E dummyEntity = persistor.getObjectWrapperFactory(clazz).makeLazy(clazz, fieldValue);
		@SuppressWarnings("unchecked")
		E existingEntity = (E)Persistor.fetchContext.get().get(dummyEntity);
		if (existingEntity == null) {
			existingEntity = fetchExistingEntity(fieldValue);
		}
		
		return existingEntity;
	}
	
	protected E fetchExistingEntity(F value) {
		Map<String, F> criteria = Collections.singletonMap(fieldName, value);
		return persistor.fetch(getEntityMetadata().getEntityType(), criteria);
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

	protected FieldMetadata<E, F, ?> getFieldMetadata() {
		if (fmd != null) {
			return fmd;
		}
		return getEntityMetadata().getFieldByName(fieldName);
	}
}
