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

	public static AlterTableBuilder alterTable() {
		return new AlterTableBuilder();
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
	
	
	public static class AlterTableBuilder extends EditTable<AlterTable> {
		public AlterTable dropColumn(String column) {
			return setCommonData(new AlterTableDropColumn().drop(column));
		}

		public <T> AlterTable addColumn(String column, Class<T> type) {
			return setCommonData(new AlterTableAddColumn().add(column, type));
		}
		public <T> AlterTable alterColumn(String column, Class<T> type) {
			return setCommonData(new AlterTableAlterColumn().alter(column, type));
		}
		
		private AlterTable setCommonData(AlterTable t) {
			return t.named(name).in(keyspace).with(properties);
		}

		@Override
		public String getQueryString() {
			throw new UnsupportedOperationException();
		}
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
