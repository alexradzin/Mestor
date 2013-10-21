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

	public JpaAnnotatedClassScanner(final Collection<String> packages) {
		this(null, packages);
	}



	public JpaAnnotatedClassScanner(final Collection<URL> resources, final Collection<String> packages) {
		this(Thread.currentThread().getContextClassLoader(), resources, packages);
	}

	public JpaAnnotatedClassScanner(final ClassLoader classLoader, final Collection<URL> resources, final Collection<String> packages) {
		this(new ClassLoader[] {classLoader}, resources, packages);
	}


	public JpaAnnotatedClassScanner(final ClassLoader[] classLoaders, final Collection<URL> resources, final Collection<String> packages) {
		this(Entity.class, classLoaders, resources, packages);
	}

	public JpaAnnotatedClassScanner(final Class<? extends Annotation> anno, final ClassLoader[] classLoaders, final Collection<URL> resources, final Collection<String> packages) {
		final ConfigurationBuilder cb = new ConfigurationBuilder();

		for (final ClassLoader cl : classLoaders) {
			cb.addClassLoader(cl);
		}

		if (resources != null) {
			cb.addUrls(resources);
		}

		final FilterBuilder fb = new FilterBuilder();

		for (final String p : packages) {
			final String regex = (p + ".").replace(".", "\\.") + ".*";
			fb.add(new Include(regex));
		}
		cb.filterInputsBy(fb);

		reflections = new Reflections(cb);
	}


	@Override
	public Collection<Class<?>> scan() {
		return reflections.getTypesAnnotatedWith(Entity.class);
	}

}
