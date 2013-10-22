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

import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.Type;

import org.mestor.metadata.FieldMetadata;

abstract class PluralAttributeImpl<T, C, E> extends AttributeImpl<T, C> implements PluralAttribute<T, C, E> {
	protected Class<E> elementClass;
	protected Type<E> elementType;


	protected PluralAttributeImpl(final ManagedType<T> managedType, final FieldMetadata<T, C, ?> fmd) {
		super(managedType, fmd);
		initElementType();
	}

	@SuppressWarnings("unchecked")
	protected void initElementType() {
		elementClass = (Class<E>)fmd.getGenericTypes().iterator().next();
		elementType = new TypeImpl<E>(elementClass);
	}

	@Override
	public BindableType getBindableType() {
		return BindableType.PLURAL_ATTRIBUTE;
	}

	@Override
	public Class<E> getBindableJavaType() {
		return elementClass;
	}

	@Override
	public CollectionType getCollectionType() {
		return CollectionType.COLLECTION;
	}

	@Override
	public Type<E> getElementType() {
		return elementType;
	}
}
