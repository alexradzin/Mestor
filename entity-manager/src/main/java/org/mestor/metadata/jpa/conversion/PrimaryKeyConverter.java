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
import org.mestor.metadata.FieldMetadata;

public class PrimaryKeyConverter<E, K> extends IndexedFieldConverter<E, K> implements AttributeConverter<E, K> {

	/**
	 * This constructor is needed to simplify generic access to functionality of this
	 * converter. The convention is that converter can be either stateless and implement 
	 * default constructor or be dependent on the entity type and context. 
	 * @param clazz
	 * @param context
	 */
	public PrimaryKeyConverter(Class<E> clazz, EntityContext context) {
		super(clazz, null, context);
	}
	

	@Override
	protected E fetchExistingEntity(K primaryKey) {
		return persistor.fetch(getEntityMetadata().getEntityType(), primaryKey);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	protected FieldMetadata<E, K, ?> getFieldMetadata() {
		return 	(FieldMetadata<E, K, ?>)getEntityMetadata().getPrimaryKey();
	}
}
