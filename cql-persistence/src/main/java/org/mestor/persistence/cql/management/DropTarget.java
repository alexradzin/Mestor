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

import com.datastax.driver.core.Statement;
import com.google.common.base.Joiner;

public class DropTarget extends Statement {
	static enum Target {
		INDEX, TABLE;
	}	
	
	private final Target target;
	private String keyspace;
	private String name;
	
	
	DropTarget(Target target) {
		this.target = target;
	}

	public DropTarget in(String keypspace) { 
		this.keyspace = keypspace;
		return this;
	}
	
	public DropTarget named(String name) { 
		this.name = name;
		return this;
	}
	
	
	@Override
	public String getQueryString() {
		return Joiner.on(" ").join("DROP",  target.name(), CommandHelper.fullname(keyspace, name));
	}

	@Override
	public ByteBuffer getRoutingKey() {
		return null;
	}

}
