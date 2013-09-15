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

package org.mestor.metadata;

import java.util.Comparator;
import java.util.Map;

public class EntityComparator<E> implements Comparator<E> {
	private final Map<Class<?>, EntityMetadata<?>> entityClasses;
	
	public EntityComparator(Map<Class<?>, EntityMetadata<?>> entityClasses) {
		this.entityClasses = entityClasses;
	}
	

	@SuppressWarnings("unchecked")
	@Override
	public int compare(E one, E two) {
		if(one == null) {
			return two == null ? 0 : -1;
		}
		if(two == null) {
			return 1; // one is definitely not null here
		}
		// both not null
		Class<E> c1 = (Class<E>)one.getClass();
		Class<E> c2 = (Class<E>)two.getClass();
		
		EntityMetadata<E> m1 = getMetadata(c1);
		EntityMetadata<E> m2 = getMetadata(c2);
		
		if (m1 == null || m2 == null) {
			// at least one of them is not entity. Compare as regular objects
			return compareImpl(one, two);
		}
		
		FieldMetadata<E, ?> pkMeta1 = m1.getPrimaryKey();
		FieldMetadata<E, ?> pkMeta2 = m2.getPrimaryKey();

		if (pkMeta1 == null || pkMeta2 == null) {
			// at least one of them does not have primary key. Compare as regular objects
			return compareImpl(one, two);
		}
		
		// both are entities that have primary keys
		Object pk1 = pkMeta1.getAccessor().getValue(one);
		Object pk2 = pkMeta1.getAccessor().getValue(one);
		
		return compareImpl(pk1, pk2);
	}

	@SuppressWarnings("unchecked")
	public <T> int compareImpl(T one, T two) {
		if (one instanceof Comparable) {
			return ((Comparable<T>) one).compareTo(two);
		}
		return one.hashCode() - two.hashCode();
	}
	
	
	private EntityMetadata<E> getMetadata(Class<E> entityClass) {
		@SuppressWarnings("unchecked")
		EntityMetadata<E> eMeta = (EntityMetadata<E>)entityClasses.get(entityClass);
		return eMeta;
	}
}
