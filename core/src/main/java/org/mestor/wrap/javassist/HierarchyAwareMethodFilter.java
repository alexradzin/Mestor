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

package org.mestor.wrap.javassist;

import java.lang.reflect.Method;

import javassist.util.proxy.MethodFilter;

import org.mestor.context.EntityContext;
import org.mestor.metadata.EntityMetadata;

public class HierarchyAwareMethodFilter<T> implements MethodFilter {
	private final EntityContext context;
	private final Class<T> clazz;
	
	public HierarchyAwareMethodFilter(EntityContext context, Class<T> clazz) {
		this.context = context;
		this.clazz = clazz;
	}

	@Override
	public boolean isHandled(Method m) {
		for (Class<?> c = clazz; !Object.class.equals(c); c = c.getSuperclass()) {
			EntityMetadata<?> emd = context.getEntityMetadata(c);
			if (emd != null && isHandled(emd, m)) {
				return true;
			}
		}
		return false;
	}

	private <E> boolean isHandled(EntityMetadata<E> emd, Method m) {
		return emd.getFieldByGetter(m) != null || emd.getFieldBySetter(m) != null;
	}

}
