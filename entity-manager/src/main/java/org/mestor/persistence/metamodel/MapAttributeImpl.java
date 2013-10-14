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

package org.mestor.persistence.metamodel;

import java.util.Iterator;
import java.util.Map;

import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.Type;

import org.mestor.metadata.FieldMetadata;

public class MapAttributeImpl<T, K, V> extends PluralAttributeImpl<T, Map<K, V>, V> implements MapAttribute<T, K, V> {
	private Class<K> keyClass;
	private Type<K> keyType;

	protected MapAttributeImpl(ManagedType<T> managedType, FieldMetadata<T, Map<K, V>, ?> fmd) {
		super(managedType, fmd);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	protected void initElementType() {
		Iterator<Class<?>> types = fmd.getGenericTypes().iterator();
		
		keyClass = (Class<K>)types.next();
		keyType = new TypeImpl<K>(keyClass);
		
		elementClass = (Class<V>)types.next();
		elementType = new TypeImpl<V>(elementClass);
	}
	

	@Override
	public Class<K> getKeyJavaType() {
		return keyClass;
	}

	@Override
	public Type<K> getKeyType() {
		return keyType;
	}

	@Override
	public CollectionType getCollectionType() {
		return CollectionType.MAP;
	}

}
