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

package org.mestor.reflection;


public class ConstructorAccessor<V> extends Valuable<java.lang.reflect.Constructor<V>, V> {
	public ConstructorAccessor(Class<V> type, Class<?>[] paramTypes) {
		this(getConstructor(type, paramTypes));
	}
	
	public ConstructorAccessor(java.lang.reflect.Constructor<V> c) {
		super(c);
	}

	@Override
	protected Object valueImpl(Object ... args) throws ReflectiveOperationException {
		return super.accessible.newInstance(args);
	}

	private static <T> java.lang.reflect.Constructor<T> getConstructor(Class<T> type, Class<?>[] paramTypes) {
		try {
			return type.getConstructor(paramTypes);
		} catch (NoSuchMethodException e) {
			throw new IllegalArgumentException(e);
		}
	}
	
}
