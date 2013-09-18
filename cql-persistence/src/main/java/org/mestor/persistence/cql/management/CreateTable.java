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

import static org.mestor.persistence.cql.management.CommandHelper.quote;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.cassandra.cql3.CQL3Type;
import org.apache.cassandra.db.marshal.CollectionType;
import org.mestor.util.Pair;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterators;


public class CreateTable extends EditTable<CreateTable> {
	public static enum FieldAttribute {
		NONE, PRIMARY_KEY, INDEX;
	}
	
	
	//protected Map<String, CQL3Type> fields = new LinkedHashMap<>();
	protected Collection<Map.Entry<String, CQL3Type>> fields = new ArrayList<>();
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


	public CreateTable add(String column, Class<?> clazz, Class<?>[] generics, FieldAttribute[] attrs) {
		add(column, CommandHelper.toCqlType(clazz, generics), attrs);
		return getThis();
	}
	
	public CreateTable add(String column, CQL3Type type, FieldAttribute[] attrs) {
		return add(fields, column, type, attrs);
	}
	
	protected CreateTable add(final Collection<Map.Entry<String, CQL3Type>> fieldTypes, final String columnName, final CQL3Type cqlType, final FieldAttribute[] attrs) {
		fieldTypes.add(new Pair<String, CQL3Type>(columnName, cqlType));
		
		for (FieldAttribute attr : attrs) {
			switch(attr) {
				case PRIMARY_KEY:
					primaryKey.add(columnName);
					break;
				case INDEX:
					index.add(columnName);
					break;
				case NONE:
					break; // do nothing. None is none.
				default:
					throw new UnsupportedOperationException(attr.name());
			}
		}
		return getThis();
	}
	
	
	private String getFieldsQuery(Collection<Map.Entry<String, CQL3Type>> fields) {
		return Joiner.on(", ").join(Iterators.transform(fields.iterator(), new Function<Entry<String, CQL3Type>, String>() {
			@Override
			public String apply(Entry<String, CQL3Type> field) {
				  return quote(field.getKey()) + " " + CreateTable.this.toString(field.getValue());
			}
		}));
	}

	// patch to work this issue https://issues.apache.org/jira/browse/CASSANDRA-6051 around
	private String toString(CQL3Type cql3Type) {
		String str = cql3Type.toString();
		if (cql3Type.isCollection()) {
			// This is a VERY long statement with a lot of castings that theoretically throw ClassCastException
			// However if this happens something is going very bad because if we are here current cql3type IS 
			// a collection and therefore all castings should perform successfully.
			if (CollectionType.Kind.MAP.equals(((CollectionType<?>)((CQL3Type.Collection)cql3Type).getType()).kind)) {
				return str.replaceFirst("^set", "map");
			}
		}
		return str;
	}
	
	
	private String getPrimaryKeyQuery(Collection<String> primaryKey) {
		return "PRIMARY KEY (" + Joiner.on(", ").join(quote(primaryKey)) + ")";
	}
	
	
	
	public boolean shouldRun() {
		return name != null && !fields.isEmpty();
	}
	
}
