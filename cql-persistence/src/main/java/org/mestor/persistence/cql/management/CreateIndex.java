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

import java.nio.ByteBuffer;

import com.datastax.driver.core.Statement;
import com.google.common.base.Joiner;



public class CreateIndex extends Statement {
	private String name;
	private String keyspace;
	private String table;

	CreateIndex() {
		// empty package protected constructor
	}
	

	public CreateIndex in(String keyspace) { 
		this.keyspace = keyspace;
		return this;
	}
	
	public CreateIndex on(String table) { 
		this.table = table;
		return this;
	}

	public CreateIndex on(String keyspace, String table) { 
		this.keyspace = keyspace;
		this.table = table;
		return this;
	}
	
	public CreateIndex named(String name) { 
		this.name = name;
		return this;
	}
	
	
	@Override
	public String getQueryString() {
		return Joiner.on(" ").join("CREATE", "INDEX", name, "ON", fullname(keyspace, table));
	}

	@Override
	public ByteBuffer getRoutingKey() {
		return null;
	}

}
