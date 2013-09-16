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

package org.mestor.metadata.jpa;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.google.common.base.Joiner;

public enum StandardNamingStrategy implements NamingStrategy {
	UPPER_CAMEL_CASE {
		@Override
		protected String transformName(String name) {
			return name.substring(0, 1).toUpperCase() + name.substring(1);
		}
	},
	
	LOWER_CAMEL_CASE {
		@Override
		protected String transformName(String name) {
			return name.substring(0, 1).toLowerCase() + name.substring(1);
		}
	},
	
	LOWER_CASE {
		@Override
		protected String transformName(String name) {
			return name.toLowerCase();
		}
	},
	
	UPPER_CASE {
		@Override
		protected String transformName(String name) {
			return name.toUpperCase();
		}
	},
 
	LOWER_CASE_UNDERSCORE {
		@Override
		protected String transformName(String name) {
			return underscore(name).toLowerCase(); 
		}
	},
	
	UPPER_CASE_UNDERSCORE {
		@Override
		protected String transformName(String name) {
			return underscore(name).toUpperCase(); 
		}
	},

	;

	private final static Pattern accessMethodPrefix = Pattern.compile("^(get|set|is)");
	private final static Pattern splitByCapitalLetter = Pattern.compile("(?=\\b[A-Z])");
			
	
	
	@Override
	public String getEntityName(Class<?> clazz) {
		return transformName(extractName(clazz, Entity.class, clazz.getSimpleName()));
	}

	@Override
	public String getTableName(Class<?> clazz) {
		return transformName(extractName(clazz, Table.class, clazz.getSimpleName()));
	}

	@Override
	public String getColumnName(AccessibleObject ao) {
		return transformName(extractName(ao, Column.class, getPropertyName(ao)));
	}


	protected String transformName(String name) {
		return name;
	}
	
	private <A extends Annotation> String extractName(AnnotatedElement ae, Class<A> annotationClass, String defaultName) {
		A annotation = ae.getAnnotation(annotationClass);
		if (annotation != null) {
			String name;
			try {
				name = (String)annotationClass.getMethod("name").invoke(annotation);
			} catch (ReflectiveOperationException e) {
				throw new IllegalArgumentException(e);
			}
			
			if (!"".equals(name)) {
				return name;
			}
		}
		return defaultName;
	}

	
	private String getPropertyName(AccessibleObject ao) {
		// I beg my pardon for instanceof... This is done to make less methods in public interface NamingStrategy
		if (ao instanceof Field) {
			return getPropertyName((Field)ao);
		} 
		if (ao instanceof Method) {
			return getPropertyName((Method)ao);
		}
		throw new IllegalArgumentException("" + ao.getClass() + " is neither " + Field.class + " nor " + Method.class);
	}
	
	
	private String getPropertyName(Field field) {
		String name = field.getName();
		return name.substring(0, 1).toUpperCase() + name.substring(1);
	}

	private String getPropertyName(Method method) {
		String methodName = method.getName();
		Matcher m = accessMethodPrefix.matcher(methodName);
		if (!m.find()) {
			throw new IllegalArgumentException(methodName + " is neither getter or setter");
		}
		return m.replaceFirst("");
	}

	protected String underscore(String str) {
		return Joiner.on('_').join(splitByCapitalLetter.split(str));
	}
}
