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

import static org.mestor.persistence.cql.management.CommandHelper.fullname;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.cassandra.db.marshal.AbstractType;
import org.mestor.persistence.cql.management.CommandBuilder.EditCommand;

import com.google.common.base.Joiner;

public class AlterTable extends EditTable<AlterTable> {
	private Map<String, AbstractType<?>> alter = new LinkedHashMap<>();
//	private Map<String, AbstractType<?>> add = new LinkedHashMap<>();
	private Collection<String> drop = new LinkedHashSet<>();
	
	
	AlterTable() {
		super(EditCommand.ALTER);
	}

	public AlterTable alter(String column, Class<?> type, FieldAttribute[] attrs) {
		alter(column, CommandHelper.toCassandraType(type), attrs);
		return this;
	}

	public AlterTable alter(String column, AbstractType<?> type, FieldAttribute[] attrs) {
		add(alter, column, type, attrs);
		return this;
	}
	
	public AlterTable drop(String column) {
		drop.add(column);
		return this;
	}
	
	
	protected AlterTable include(Map<String, AbstractType<?>> columns, String column, AbstractType<?> type) {
		columns.put(column, type);
		return this;
	}

	@Override
	public String getQueryString() {
		return Joiner.on(" ").skipNulls().join(
				getFieldsQuery("ALTER", alter), 
				getFieldsQuery("ADD", fields), 
				getFieldsQuery("DROP", drop)
		);
	}
	
	private String getFieldsQuery(String action, Map<String, AbstractType<?>> fields) {
		if (fields == null || fields.isEmpty()) {
			return null;
		}

		StringBuilder buf = new StringBuilder();
		for (Entry<String, AbstractType<?>> field : fields.entrySet()) {
			Joiner.on(" ").appendTo(buf, 
					"ALTER", "TABLE", fullname(action, action), 
					action, field.getKey(), "TYPE", field.getValue().asCQL3Type().toString());
			buf.append(";");
		}
		
		return buf.toString();
	}
	
	private String getFieldsQuery(String action, Collection<String> fields) {
		if (fields == null || fields.isEmpty()) {
			return null;
		}

		StringBuilder buf = new StringBuilder();
		for (String field : fields) {
			Joiner.on(" ").appendTo(buf, 
					"ALTER", "TABLE", fullname(action, action), 
					action, field);
			buf.append(";");
		}
		
		return buf.toString();
	}
	

	@Override
	public boolean shouldRun() {
		return name != null && (!fields.isEmpty() || !alter.isEmpty() || !drop.isEmpty());
	}
	
}
