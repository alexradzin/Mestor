package org.mestor.reflection;

import java.lang.reflect.AccessibleObject;

public abstract class Valuable<T extends AccessibleObject, V> {
	protected final T accessible;
	
	protected Valuable(T accessible) {
		this.accessible = accessible;
	}

	@SuppressWarnings("unchecked")
	public V value(Object ... args) {
		try {
			return (V)valueImpl(args);
		} catch (ReflectiveOperationException e) {
			throw new IllegalArgumentException(e);
		}
	}
	
	protected abstract Object valueImpl(Object ... args) throws ReflectiveOperationException;
	
}
