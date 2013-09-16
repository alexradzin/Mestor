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

package org.mestor.persistence.cql.management;

import java.nio.ByteBuffer;
import java.util.LinkedHashMap;
import java.util.Map;

import com.datastax.driver.core.Statement;

public abstract class EditTable<T extends EditTable<T>> extends Statement {
	protected String keyspace;
	protected String name;
	protected Map<String, Object> properties = new LinkedHashMap<>();
	

	protected EditTable() {
		// empty protected constructor of abstract class
	}
	

	public T in(String keypspace) { 
		this.keyspace = keypspace;
		return getThis();
	}
	
	public T named(String name) { 
		this.name = name;
		return getThis();
	}
	
	
	public T with(Map<String, Object> properties) {
		if (properties != null) {
			this.properties.putAll(properties);
		}
		return getThis();
	}

	public T with(String key,  Object value) {
		this.properties.put(key, value);
		return getThis();
	}
	
	

	@Override
	public ByteBuffer getRoutingKey() {
		return null;
	}
	
	@SuppressWarnings("unchecked")
	protected final T getThis() {
		return (T)this;
	}
	
}
