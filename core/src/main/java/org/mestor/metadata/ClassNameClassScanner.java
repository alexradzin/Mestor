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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class ClassNameClassScanner implements ClassScanner {
	private final Iterable<String> classNames;
	private final ClassLoader cl;

	public ClassNameClassScanner(ClassLoader cl, Iterable<String> classNames) {
		this.cl = cl;
		this.classNames = classNames;
	}
	
	
	public ClassNameClassScanner(Iterable<String> classNames) {
		this(Thread.currentThread().getContextClassLoader(), classNames);
	}

	public ClassNameClassScanner(ClassLoader cl, String[] classNames) {
		this(cl, Arrays.asList(classNames));
	}
	
	
	public ClassNameClassScanner(String[] classNames) {
		this(Arrays.asList(classNames));
	}
	
	
	

	@Override
	public Collection<Class<?>> scan() {
		Collection<Class<?>> classes = new ArrayList<>();
		for (String className : classNames) {
			try {
				classes.add(cl.loadClass(className));
			} catch (ClassNotFoundException e) {
				throw new IllegalArgumentException(e);
			}
		}
		return classes;
	}

}
