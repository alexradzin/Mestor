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
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.Type;

import org.mestor.metadata.FieldMetadata;

public class SingularAttributeImpl<E, F> extends AttributeImpl<E, F> implements SingularAttribute<E, F> {
	public SingularAttributeImpl(ManagedType<E> managedType, FieldMetadata<E, F, ?> fmd) {
		super(managedType, fmd);
	}

	@Override
	public BindableType getBindableType() {
		return BindableType.SINGULAR_ATTRIBUTE;
	}

	@Override
	public Class<F> getBindableJavaType() {
		return fmd.getType();
	}

	@Override
	public boolean isId() {
		return fmd.isKey();
	}

	@Override
	public boolean isVersion() {
		return false;
	}

	@Override
	public boolean isOptional() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Type<F> getType() {
		return new TypeImpl<F>(getBindableJavaType());
	}

}
