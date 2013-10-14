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

import java.util.Arrays;

import javax.persistence.metamodel.Type;

import com.google.common.collect.Lists;

class TypeImpl<T> implements Type<T> {
	private final Class<T> clazz;
	private final PersistenceType persistenceType;
	
	TypeImpl(Class<T> clazz) {
		this.clazz = clazz;
		this.persistenceType = Utils.getAnnotatedCategory(
				Utils.typeAnnotations, 
				clazz, 
				Lists.reverse(Arrays.asList(PersistenceType.values())).toArray(new PersistenceType[0]), 
				PersistenceType.BASIC);
	}

	@Override
	public PersistenceType getPersistenceType() {
		return persistenceType;
	}

	@Override
	public Class<T> getJavaType() {
		return clazz;
	}

}
