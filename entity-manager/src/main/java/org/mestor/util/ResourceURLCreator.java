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

package org.mestor.util;

import java.net.URL;

import com.google.common.base.Function;

public class ResourceURLCreator implements Function<String, URL> {
	private final ClassLoader classLoader;

	public ResourceURLCreator() {
		this(Thread.currentThread().getContextClassLoader());
	}
	
	
	public ResourceURLCreator(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}
	
	
	@Override
	public URL apply(String urlSpec) {
		return classLoader.getResource(urlSpec);
	}
}
