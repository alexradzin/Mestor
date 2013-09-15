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

import org.mestor.persistence.cql.management.DropTarget.Target;


public abstract class CommandBuilder {
	static enum EditCommand {
		CREATE, ALTER;
	}
	
	private CommandBuilder() {
		// private empty constructor to prevent any attempt to create instance of this class
		throw new IllegalStateException();
	}

	
	public static CreateTable createTable() {
		return new CreateTable();
	}

	public static CreateIndex createIndex() {
		return new CreateIndex();
	}

	public static EditKeyspace createKeyspace() {
		return new EditKeyspace(EditCommand.CREATE);
	}

	public static AlterTable alterTable() {
		return new AlterTable();
	}

	public static EditKeyspace alterKeyspace() {
		return new EditKeyspace(EditCommand.ALTER);
	}

	public static DropTarget dropTable() {
		return new DropTarget(Target.TABLE);
	}

	public static DropKeyspace dropKeyspace() {
		return new DropKeyspace();
	}
	
	public static DropTarget dropIndex() {
		return new DropTarget(Target.INDEX);
	}
	
	
	
//	CREATE KEYSPACE
//	USE
//	ALTER KEYSPACE
//	DROP KEYSPACE
//	CREATE TABLE
//	ALTER TABLE
//	DROP TABLE
//	TRUNCATE
//	CREATE INDEX
//	DROP INDEX
	
	
	/* CREATE KEYSPACE/TABLE name [with props]
	 * CREATE INDEX name ON table_name(column_name)
	 * DROP TARGET target_name
	 * USE keyspace_name
	 * 
	 * ALTER KEYSPACE [with props]
	 * ALTER TABLE with props, add/drop/alter column
	 * 
	 * */
}
