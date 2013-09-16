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

import java.net.URL;
import java.util.Collection;

import javax.persistence.Entity;

import org.mestor.metadata.ClassScanner;
import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.reflections.util.FilterBuilder.Include;

public class JpaAnnotatedClassScanner implements ClassScanner {
	private final Reflections reflections;

	public JpaAnnotatedClassScanner(Collection<String> packages) {
		this(null, packages);
	}
	
	
	public JpaAnnotatedClassScanner(Collection<URL> resources, Collection<String> packages) {
		this(Thread.currentThread().getContextClassLoader(), resources, packages);
	}
	
	public JpaAnnotatedClassScanner(ClassLoader classLoader, Collection<URL> resources, Collection<String> packages) {
		this(new ClassLoader[] {classLoader}, resources, packages);
	}
	
	
	public JpaAnnotatedClassScanner(ClassLoader[] classLoaders, Collection<URL> resources, Collection<String> packages) {
		ConfigurationBuilder cb = new ConfigurationBuilder();
		
		for (ClassLoader cl : classLoaders) {
			cb.addClassLoader(cl);
		}
		
		if (resources != null) {
			cb.addUrls(resources);
		}
		
		FilterBuilder fb = new FilterBuilder();
		for (String p : packages) {
			fb.add(new Include(p + "."));
		}
		cb.filterInputsBy(fb);
		
		reflections = new Reflections(cb);
	}

	@Override
	public Collection<Class<?>> scan() {
		return reflections.getTypesAnnotatedWith(Entity.class);
	}

}
