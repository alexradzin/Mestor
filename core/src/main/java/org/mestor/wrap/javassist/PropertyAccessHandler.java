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
import java.util.Map.Entry;
import java.util.Set;

import javassist.util.proxy.MethodHandler;

import org.mestor.context.DirtyEntityManager;
import org.mestor.context.EntityContext;
import org.mestor.context.Persistor;
import org.mestor.metadata.EntityMetadata;
import org.mestor.metadata.FieldMetadata;
import org.mestor.reflection.MethodAccessor;
import org.mestor.util.Pair;

import com.google.common.base.Function;

public class PropertyAccessHandler<T> implements MethodHandler {
	private final T instance;
	private final EntityMetadata<T> metadata;
	private final EntityContext context; 
	private final Persistor persistor;
	private final boolean lazy;
	private final DirtyEntityManager dirtyEntityManager;

	private Set<String> changedFields = new LinkedHashSet<>();
	private Set<String> retreivedFields = new LinkedHashSet<>();
	
	private static Function<Entry<EntityMetadata<Object>, Method>, FieldMetadata<Object, Object, Object>> getterFetcher = new Function<Entry<EntityMetadata<Object>, Method>, FieldMetadata<Object, Object, Object>>() {
		@Override
		public FieldMetadata<Object, Object, Object> apply(Entry<EntityMetadata<Object>, Method> entry) {
			return entry.getKey().getFieldByGetter(entry.getValue());
		}
	};

	private static Function<Entry<EntityMetadata<Object>, Method>, FieldMetadata<Object, Object, Object>> setterFetcher = new Function<Entry<EntityMetadata<Object>, Method>, FieldMetadata<Object, Object, Object>>() {
		@Override
		public FieldMetadata<Object, Object, Object> apply(Entry<EntityMetadata<Object>, Method> entry) {
			return entry.getKey().getFieldBySetter(entry.getValue());
		}
	};

	public PropertyAccessHandler(T instance, EntityMetadata<T> metadata, EntityContext context, boolean lazy) {
		this(instance, metadata, context, context.getPersistor(), context.getDirtyEntityManager(), lazy);
	}
	
	private PropertyAccessHandler(T instance, EntityMetadata<T> metadata, EntityContext context, Persistor persistor, DirtyEntityManager dirtyEntityManager, boolean lazy) {
		this.instance = instance;
		this.metadata = metadata;
		this.context = context;
		this.persistor = persistor;
		this.dirtyEntityManager = dirtyEntityManager;
		this.lazy = lazy;
	}
	
	

	@Override
	public Object invoke(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable {
		
		if (MethodAccessor.isGetter(thisMethod)) {
			final Object result;
			//FieldMetadata<T, Object, Object> fmd = metadata.getFieldByGetter(thisMethod);
			
			
			FieldMetadata<T, Object, Object> fmd = findField(metadata, thisMethod, getterFetcher);
			
			
			String fieldName = fmd.getName();
			
			if ((lazy || fmd.isLazy()) && !retreivedFields.contains(fieldName)) {
				// retrieve that property from DB
				Object[] fieldValues = persistor.fetchProperty(self, fieldName);
				result = fieldValues == null ? null : fieldValues[0];
				// put value into wrapped object
				fmd.getAccessor().setValue(instance, result);
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
//			FieldMetadata<T, ?, ?> fmd = metadata.getFieldBySetter(thisMethod);
			FieldMetadata<T, Object, Object> fmd = findField(metadata, thisMethod, setterFetcher);
			changedFields.add(fmd.getName());
			thisMethod.invoke(instance, args);
			return null; // setter does not return value
		}
		
		
		throw new IllegalArgumentException("Method " + thisMethod + " is neither getter nor setter");
	}
	

	public T getPayload() {
		return instance;
	}
	

	@SuppressWarnings("unchecked")
	private FieldMetadata<T, Object, Object> findField(final EntityMetadata<T> entityMetadata, Method method, Function<Entry<EntityMetadata<Object>, Method>, FieldMetadata<Object, Object, Object>> accessor) {
		EntityMetadata<Object> emd = (EntityMetadata<Object>)entityMetadata;
		for (Class<?> c = entityMetadata.getEntityType(); !Object.class.equals(c); c = c.getSuperclass(), emd = (EntityMetadata<Object>)context.getEntityMetadata(c)) {
			FieldMetadata<Object, ?, ?> fmd = accessor.apply(new Pair<EntityMetadata<Object>, Method>(emd, method));
			if (fmd != null) {
				return (FieldMetadata<T, Object, Object>)fmd;
			}
		}
		
		return null;
	}
	
}
