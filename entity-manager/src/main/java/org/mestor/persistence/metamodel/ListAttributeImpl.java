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

import java.util.List;

import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.ManagedType;

import org.mestor.metadata.FieldMetadata;

public class ListAttributeImpl<T, E> extends PluralAttributeImpl<T, List<E>, E> implements ListAttribute<T, E> {

	protected ListAttributeImpl(ManagedType<T> managedType, FieldMetadata<T, List<E>, ?> fmd) {
		super(managedType, fmd);
	}
	
	
	@Override
	public CollectionType getCollectionType() {
		return CollectionType.LIST;
	}

}
