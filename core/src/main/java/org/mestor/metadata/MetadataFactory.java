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

import java.util.Map;

public interface MetadataFactory {
	/**
	 * Set current schema that will be used to create all tables, indexes etc
	 * @param schema
	 */
	public void setSchema(String schema);
	
	/**
	 * Creates {@link EntityMetadata} for given class.
	 * @param clazz
	 * @return entity meta data
	 */
	public <T> EntityMetadata<T> create(Class<T> clazz);
	
	/**
	 * Creates {@link FieldMetadata} for specified field of given class.
	 * @param clazz
	 * @param fieldClass
	 * @param name
	 * @return the field meta data
	 */
	public <T, F, C> FieldMetadata<T, F, C> create(Class<T> clazz, Class<F> fieldClass, String name);
	
	/**
	 * Creates {@link IndexMetadata} identified by {@code name} including specified fields for given entity meta data.
	 * @param entityMetadata
	 * @param name
	 * @param indexedFields
	 * @return index meta data
	 */
	public <T> IndexMetadata<T> create(EntityMetadata<T> entityMetadata, String name, String... indexedFields);
	
	/**
	 * Performs all required updates of created {@link EntityMetadata}. This method should be called when all instances
	 * {@link EntityMetadata} for all classes in context are already created, so required fixes and additions based on 
	 * relationships between different entities can be done. 
	 * @param metadata
	 */
	public void update(Map<Class<?>, EntityMetadata<?>> metadata);
}
