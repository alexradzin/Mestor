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
package org.mestor.benchmarks;

import org.mestor.benchmarks.entities.ObjectWithRandomBytesArray;

public enum Parameters {
	ENTITY_CLASS("benchmark.entity_class", ObjectWithRandomBytesArray.class),
	PERSISTENCE_UNIT("benchmark.pu", "benchmarks"),
	OUTPUT_DIR("benchmark.output_dir", "/tmp/"),
	CLEAR_SELECTS_CACHE("benchmark.clear_selects_cache", false),

	ITERATIONS_COUNT("benchmark.iterations", 10), 
	INSERTS_PER_ITERATION("benchmark.inserts_per_iteration", 1000), 
	SELECTS_PER_ITERATION("benchmark.selects_per_iteration", 1000), 

	;

	private final Object value;

	private Parameters(final String propName, final int defaultValue) {
		final String explicitValue = System.getProperty(propName);
		this.value = explicitValue == null ? defaultValue : Integer.valueOf(explicitValue);
	}
	
	private Parameters(final String propName, final String defaultValue) {
		final String explicitValue = System.getProperty(propName);
		this.value = explicitValue == null ? defaultValue : explicitValue;
	}
	
	private Parameters(final String propName, final boolean defaultValue) {
		final String explicitValue = System.getProperty(propName);
		this.value = explicitValue == null ? defaultValue : Boolean.valueOf(explicitValue);
	}

	private Parameters(final String propName, final Class<?> defaultValue) {
		final String explicitValue = System.getProperty(propName);
		if (explicitValue != null) {
			Class<?> rawClass;
			try {
				rawClass = Class.forName(explicitValue);
			} catch (final ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
			this.value = rawClass;
		} else {
			this.value = defaultValue;
		}
	}

	public Object get() {
		return value;
	}

	public int getInt() {
		return (Integer) value;
	}
	
	public String getString() {
		return (String) value;
	}

	public Class<?> getClazz() {
		return (Class<?>) value;
	}

	public boolean getBoolean() {
		return (Boolean) value;
	}
}