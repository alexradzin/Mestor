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

import org.apache.cassandra.cql3.CQL3Type;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterators;

import static org.mestor.persistence.cql.management.CommandHelper.quote;


public class CreateTable extends EditTable<CreateTable> {
	public static enum FieldAttribute {
		NONE, PRIMARY_KEY, INDEX;
	}
	
	
	protected Map<String, CQL3Type> fields = new LinkedHashMap<>();
	protected Collection<String> primaryKey = new ArrayList<String>();
	protected Collection<String> index = new ArrayList<String>();
	

	@Override
	public String getQueryString() {
		return CommandHelper.createQueryString(new String[] {
				"CREATE",  
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


	public CreateTable add(String column, Class<?> clazz, FieldAttribute[] attrs) {
		add(column, CommandHelper.toCqlType(clazz), attrs);
		return getThis();
	}
	
	public CreateTable add(String column, CQL3Type type, FieldAttribute[] attrs) {
		return add(fields, column, type, attrs);
	}
	
	protected CreateTable add(Map<String, CQL3Type> map, String column, CQL3Type type, FieldAttribute[] attrs) {
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
	
	
	private String getFieldsQuery(Map<String, CQL3Type> fields) {
		return Joiner.on(", ").join(Iterators.transform(fields.entrySet().iterator(), new Function<Entry<String, CQL3Type>, String>() {
			@Override
			public String apply(Entry<String, CQL3Type> field) {
				  return quote(field.getKey()) + " " + field.getValue().toString();
			}
		}));
	}

	private String getPrimaryKeyQuery(Collection<String> primaryKey) {
		return "PRIMARY KEY (" + Joiner.on(", ").join(quote(primaryKey)) + ")";
	}
	
	
	
	public boolean shouldRun() {
		return name != null && !fields.isEmpty();
	}
	
}
