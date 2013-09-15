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
import java.util.LinkedHashSet;
import java.util.Set;

import javassist.util.proxy.MethodHandler;

import org.mestor.context.DirtyEntityManager;
import org.mestor.context.Persistor;
import org.mestor.metadata.EntityMetadata;
import org.mestor.reflection.MethodAccessor;

public class PropertyAccessHandler<T> implements MethodHandler {
	private final T instance;
	private final EntityMetadata<T> metadata;
	private Set<String> changedFields = new LinkedHashSet<>();
	private Set<String> retreivedFields = new LinkedHashSet<>();
	private Persistor persistor;
	private boolean lazy = false;
	private final DirtyEntityManager dirtyEntityManager;

	
	public PropertyAccessHandler(T instance, EntityMetadata<T> metadata, Persistor persistor, DirtyEntityManager dirtyEntityManager, boolean lazy) {
		this.instance = instance;
		this.metadata = metadata;
		this.persistor = persistor;
		this.dirtyEntityManager = dirtyEntityManager;
		this.lazy = lazy;
	}
	
	

	@Override
	public Object invoke(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable {
		
		if (MethodAccessor.isGetter(thisMethod)) {
			final Object result;
			String fieldName = metadata.getFieldNameByGetter(thisMethod);
			
			if ((lazy || metadata.getField(fieldName).isLazy()) && !retreivedFields.contains(fieldName)) {
				// retrieve that property from DB
				result = persistor.fetchProperty(self, fieldName);
				// put value into wrapped object
				metadata.getField(fieldName).getAccessor().setValue(instance, result);
				retreivedFields.add(fieldName);
			} else {
				result = thisMethod.invoke(instance, args);
				if (dirtyEntityManager != null) {
					dirtyEntityManager.addDirtyEntity(instance);
				}
			} 
			return result;
		} 
		
		if (MethodAccessor.isSetter(thisMethod)) {
			thisMethod.invoke(instance, args);
			changedFields.add(metadata.getFieldNameBySetter(thisMethod));
			return null; // setter does not return value
		}
		
		
		throw new IllegalArgumentException("Method " + thisMethod + " is neither getter nor setter");
	}
	

	public T getPayload() {
		return instance;
	}
	
}
