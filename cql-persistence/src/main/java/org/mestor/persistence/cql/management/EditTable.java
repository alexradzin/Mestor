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
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.cassandra.db.marshal.AbstractType;
import org.mestor.persistence.cql.management.CommandBuilder.EditCommand;

import com.datastax.driver.core.Statement;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterators;

public abstract class EditTable<T extends EditTable<T>> extends Statement {
	public static enum FieldAttribute {
		NONE, PRIMARY_KEY, INDEX;
	}
	
	
	protected final EditCommand command;
	protected String keyspace;
	protected String name;
	protected Map<String, Object> properties = new LinkedHashMap<>();
	protected Map<String, AbstractType<?>> fields = new LinkedHashMap<>();
	protected Collection<String> primaryKey = new ArrayList<String>();
	protected Collection<String> index = new ArrayList<String>();
	

	EditTable(EditCommand command) {
		this.command = command;
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
	public String getQueryString() {
		return CommandHelper.createQueryString(new String[] {
				command.name(),  
				"TABLE", 
				CommandHelper.fullname(keyspace, name),
				"(", getFieldsQuery(fields), ",",  getPrimaryKeyQuery(primaryKey), ")"
			},
			properties);
	}

	@Override
	public ByteBuffer getRoutingKey() {
		return null;
	}


	public T add(String column, Class<?> clazz, FieldAttribute[] attrs) {
		add(column, CommandHelper.toCassandraType(clazz), attrs);
		return getThis();
	}
	
	public T add(String column, AbstractType<?> type, FieldAttribute[] attrs) {
		return add(fields, column, type, attrs);
	}
	
	protected T add(Map<String, AbstractType<?>> map, String column, AbstractType<?> type, FieldAttribute[] attrs) {
		map.put(column, type);
		for (FieldAttribute attr : attrs) {
			switch(attr) {
				case PRIMARY_KEY:
					primaryKey.add(column);
					break;
				case INDEX:
					index.add(column);
					break;
				case NONE:
					break; // do nothing. None is none.
				default:
					throw new UnsupportedOperationException(attr.name());
			}
		}
		return getThis();
	}
	
	
	private String getFieldsQuery(Map<String, AbstractType<?>> fields) {
		return Joiner.on(", ").join(Iterators.transform(fields.entrySet().iterator(), new Function<Entry<String, AbstractType<?>>, String>() {
			  @Override
			public String apply(Entry<String, AbstractType<?>> field) {
				  return field.getKey() + " " + field.getValue().asCQL3Type().toString();
			  }
		}));
	}

	private String getPrimaryKeyQuery(Collection<String> primaryKey) {
		return "PRIMARY KEY (" + Joiner.on(", ").join(primaryKey) + ")";
	}
	
	
	
	@SuppressWarnings("unchecked")
	protected final T getThis() {
		return (T)this;
	}
	
	public boolean shouldRun() {
		return name != null && !fields.isEmpty();
	}
}
