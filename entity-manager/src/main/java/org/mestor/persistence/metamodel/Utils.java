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

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.EnumMap;
import java.util.Map;

import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.metamodel.Type.PersistenceType;

class Utils {
	final static Map<PersistenceType, Class<? extends Annotation>> typeAnnotations = new EnumMap<PersistenceType, Class<? extends Annotation>>(PersistenceType.class) {{
		put(PersistenceType.ENTITY, Entity.class);
		put(PersistenceType.EMBEDDABLE, Embeddable.class);
		put(PersistenceType.MAPPED_SUPERCLASS, Inheritance.class);
	}};

	
	//static Function<FieldMetadata<T, F, C>, Attribute<T, F>> f;
	
	
	static <T, F, E extends Enum<E>> E getAnnotatedCategory(
			Map<E, Class<? extends Annotation>> enum2annotation, 
			AnnotatedElement ae, 
			E[] values, 
			E defaultValue) {
		
		for (E t : values) {
			if(ae.getAnnotation(enum2annotation.get(t)) != null) {
				return t;
			}
		}
		return defaultValue;
	}
	
	
//	static <T, F> Attribute<T, F> createAttribute(FieldMetadata<T, F, ?> fmd) {
//		return null;
//	}
//	
//	static class AttributeFactory<T, F> implements Function<FieldMetadata<T, F, ?>, Attribute<T, F>> {
//		private final ManagedType<T> managedType;
//		
//		AttributeFactory(ManagedType<T> managedType) {
//			this.managedType = managedType;
//		}
//		
//		@SuppressWarnings({ "rawtypes", "unchecked" })
//		@Override
//		public Attribute<T, F> apply(FieldMetadata<T, F, ?> fmd) {
//			Class<F> clazz = fmd.getType();
//			
//			if (List.class.isAssignableFrom(clazz)) {
//				return new ListAttributeImpl(managedType, fmd);
//			}
//			
//			if (Set.class.isAssignableFrom(clazz)) {
//				return new SetAttributeImpl(managedType, fmd);
//			}
//			
//			if (Map.class.isAssignableFrom(clazz)) {
//				return new MapAttributeImpl(managedType, fmd);
//			}
//			
//			return new SingularAttributeImpl<T, F>(managedType, fmd);
//		}
//		
//	}
}
