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

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.mestor.context.EntityContext;
import org.mestor.wrap.ObjectWrapperFactory;

import com.google.common.base.Function;

public class EntityTraverser {
	private final static Comparator<Object> identityComparator = new Comparator<Object>() {
		@Override
		public int compare(Object o1, Object o2) {
			if (o1 == null) {
				return o2 == null ? 0 : -1;
			}
			if (o2 == null) {
				return 1;
			}
			return System.identityHashCode(o1) - System.identityHashCode(o2);
		}

	};


	private final EntityContext context;


	public EntityTraverser(EntityContext context) {
		this.context = context;
	}


	public <E> void traverse(E entity, CascadeOption currentCascade, Function<Object, Object> action) {
		traverse(entity, currentCascade, action, new TreeSet<Object>(identityComparator));
	}


	private <E> void traverse(E entity, CascadeOption currentCascade, Function<Object, Object> action, Set<Object> visitedEntities) {
		if (entity == null || visitedEntities.contains(entity)) {
			return;
		}

		@SuppressWarnings("unchecked")
		Class<E> clazz = (Class<E>)entity.getClass();
		if (context.getPersistor() != null) {
			// This can happen mostly in testing environment.
			// Other solution is to create deeper mock up that simulates perssitor and ObjectWrapperFactory
			// However this means that test knows to much about implementation details.
			// So, this simple "if" looks like better solution.
			ObjectWrapperFactory<E> wraperFactory = context.getPersistor().getObjectWrapperFactory(clazz);
			if(wraperFactory.isWrappedType(clazz)) {
				clazz = wraperFactory.getRealType(clazz);
			}
		}
		EntityMetadata<E> emd = context.getEntityMetadata(clazz);

		if (emd == null) {
			return;
		}


		action.apply(entity);
		visitedEntities.add(entity);


		for (FieldMetadata<E, ?, ?> fmd : emd.getFields()) {
			if (!fmd.isCascade(currentCascade)) {
				continue;
			}
			Class<?> fieldType = fmd.getType();
			EntityMetadata<?> fieldEmd = context.getEntityMetadata(fieldType);
			if (fieldEmd != null) {
				traverse(fmd.getAccessor().getValue(entity), currentCascade, action, visitedEntities);
				continue;
			}

			// fieldType does not represent entity. But probably it is a collection of entities

			if (Collection.class.isAssignableFrom(fieldType)) {
				Class<?> collectionElementType = fmd.getGenericTypes().iterator().next();
				EntityMetadata<?> collectionElementEmd = context.getEntityMetadata(collectionElementType);
				if (collectionElementEmd != null) {
					// this is a collection of entities
					@SuppressWarnings("unchecked")
					Collection<Object> values = (Collection<Object>)fmd.getAccessor().getValue(entity);
					if (values != null) {
						for(Object value : values) {
							traverse(value, currentCascade, action, visitedEntities);
						}
					}
					continue;
				}
			}


			if (Map.class.isAssignableFrom(fieldType)) {
				Iterator<Class<?>> generics = fmd.getGenericTypes().iterator();
				Class<?> mapKeyType = generics.next();
				Class<?> mapValueType = generics.next();

				EntityMetadata<?> keyEmd = context.getEntityMetadata(mapKeyType);
				EntityMetadata<?> valueEmd = context.getEntityMetadata(mapValueType);

				if (keyEmd != null || valueEmd != null) {
					@SuppressWarnings("unchecked")
					Map<Object, Object> map = (Map<Object, Object>)fmd.getAccessor().getValue(entity);
					if (map != null) {
						for(Entry<Object, Object> entry : map.entrySet()) {
							if (keyEmd != null) {
								traverse(entry.getKey(), currentCascade, action, visitedEntities);
							}
							if (valueEmd != null) {
								traverse(entry.getValue(), currentCascade, action, visitedEntities);
							}
						}
					}
				}
			}

		}
	}

}
