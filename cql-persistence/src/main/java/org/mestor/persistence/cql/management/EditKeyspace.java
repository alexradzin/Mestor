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

import org.mestor.persistence.cql.management.CommandBuilder.EditCommand;

import com.datastax.driver.core.Statement;

import static org.mestor.persistence.cql.management.CommandHelper.quote;

public class EditKeyspace extends Statement {
	private final EditCommand command;
	private String name;
	private Map<String, Object> properties = new LinkedHashMap<>();
	
	
	EditKeyspace(EditCommand command) {
		this.command = command;
	}
	
	
	public EditKeyspace named(String name) { 
		this.name = name;
		return this;
	}
	
	
	public EditKeyspace with(Map<String, Object> properties) {
		this.properties.putAll(properties);
		return this;
	}

	public EditKeyspace with(String key,  Object value) {
		this.properties.put(key, value);
		return this;
	}
	
	
	@Override
	public String getQueryString() {
		return CommandHelper.createQueryString(new String[] {command.name(),  "KEYSPACE", quote(name)}, properties);
	}

	@Override
	public ByteBuffer getRoutingKey() {
		return null;
	}

}
