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
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import javassist.util.proxy.ProxyObject;

import org.mestor.context.EntityContext;
import org.mestor.metadata.EntityMetadata;
import org.mestor.reflection.ClassAccessor;
import org.mestor.wrap.ObjectWrapperFactory;

public class JavassistObjectWrapperFactory<T> implements ObjectWrapperFactory<T> {
	private final EntityContext context;
	
	
	public JavassistObjectWrapperFactory(EntityContext context) {
		this.context = context;
	}

	@Override
	public T wrap(T obj) {
		@SuppressWarnings("unchecked")
		Class<T> clazz = (Class<T>)obj.getClass();
		T proxy = ClassAccessor.newInstance(createClass(clazz));
		EntityMetadata<T> metadata = context.getEntityMetadata(clazz);
		MethodHandler mi = new PropertyAccessHandler<T>(obj, metadata, context.getPersistor(), context.getDirtyEntityManager(), true);
		((ProxyObject)proxy).setHandler(mi);
		return proxy;
	}
	
	@Override
	public <K> T makeLazy(Class<T> clazz, K pk) {
		T obj = ClassAccessor.newInstance(clazz);
		EntityMetadata<T> metadata = context.getEntityMetadata(clazz);
		metadata.getPrimaryKey().getAccessor().setValue(obj, pk);
		return wrap(obj);
	}

	@SuppressWarnings("unchecked")
	@Override
	public T unwrap(T obj) {
		return ((PropertyAccessHandler<T>)((ProxyObject)obj).getHandler()).getPayload();
	}
	
	@Override
	public boolean isWrapped(T obj) {
		return obj instanceof ProxyObject;
	}
	
	
	
	private Class<? extends T> createClass(final Class<T> clazz) {

		ProxyFactory f = new ProxyFactory();

		if (clazz.isInterface()) {
			f.setInterfaces(new Class[] {clazz});
		} else {
			f.setSuperclass(clazz);
		}

		f.setFilter(new MethodFilter() {
			@Override
			public boolean isHandled(Method m) {
				return context.getEntityMetadata(clazz).getFieldNameByGetter(m) != null;
			}
		});
		
		@SuppressWarnings("unchecked")
		Class<? extends T> c = f.createClass();
		return c;
		
	}
	
}
