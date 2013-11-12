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

import java.util.regex.Pattern;

import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import javassist.util.proxy.ProxyObject;

import org.mestor.context.EntityContext;
import org.mestor.metadata.EntityMetadata;
import org.mestor.metadata.FieldMetadata;
import org.mestor.reflection.ClassAccessor;
import org.mestor.wrap.ObjectWrapperFactory;

public class JavassistObjectWrapperFactory<T> implements ObjectWrapperFactory<T> {
	private final EntityContext context;
	private final static Pattern proxyClassNamePattern = Pattern.compile("^\\w+_\\$\\$_javassist_\\d+$");


	public JavassistObjectWrapperFactory(final EntityContext context) {
		this.context = context;
	}

	@Override
	public T wrap(final T obj) {
		@SuppressWarnings("unchecked")
		final
		Class<T> clazz = (Class<T>)obj.getClass();
		final T proxy = ClassAccessor.newInstance(createClass(clazz));
		final EntityMetadata<T> metadata = context.getEntityMetadata(clazz);
		final MethodHandler mi = new PropertyAccessHandler<T>(obj, metadata, context, false);
		((ProxyObject)proxy).setHandler(mi);
		return proxy;
	}

	@Override
	public <K> T makeLazy(final Class<T> clazz, final K pk) {
		final T obj = ClassAccessor.newInstance(clazz);
		final EntityMetadata<T> metadata = context.getEntityMetadata(clazz);

		@SuppressWarnings("unchecked")
		final
		FieldMetadata<T, K, K> pkMeta = (FieldMetadata<T, K, K>)metadata.getPrimaryKey();
		pkMeta.getAccessor().setValue(obj, pk);
		return wrap(obj);
	}

	@Override
	public T unwrap(final T obj) {
		return getMethodHandler(obj).getPayload();
	}

	@Override
	public boolean isWrapped(final T obj) {
		return obj instanceof ProxyObject;
	}



	private Class<? extends T> createClass(final Class<T> clazz) {

		final ProxyFactory f = new ProxyFactory();

		if (clazz.isInterface()) {
			f.setInterfaces(new Class[] {clazz});
		} else {
			f.setSuperclass(clazz);
		}

		f.setFilter(new HierarchyAwareMethodFilter<>(context, clazz));

		@SuppressWarnings("unchecked")
		final
		Class<? extends T> c = f.createClass();
		return c;

	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<T> getRealType(final Class<? extends T> wrappedType) {
		if (!isWrappedType(wrappedType)) {
			throw new IllegalArgumentException(wrappedType + " is not wrapped");
		}
		return (Class<T>)wrappedType.getSuperclass();
	}

	@Override
	public boolean isWrappedType(Class<? extends T> clazz) {
		return proxyClassNamePattern.matcher(clazz.getSimpleName()).find();
	}


	@Override
	public void markAsRemoved(final T obj) {
		getMethodHandler(obj).markAsRemoved();
	}

	@Override
	public boolean isRemoved(final T obj) {
		return getMethodHandler(obj).isRemoved();
	}


	@SuppressWarnings("unchecked")
	private PropertyAccessHandler<T> getMethodHandler(final T obj) {
		if (!isWrapped(obj)) {
			throw new IllegalArgumentException("Object " + obj + (obj == null ? "" : " of type " + obj.getClass()) + " is not wrapped");
		}
		return (PropertyAccessHandler<T>)((ProxyObject)obj).getHandler();
	}
}
