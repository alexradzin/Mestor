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

package org.mestor.persistence.cql;

import com.datastax.driver.core.querybuilder.Clause;

public enum Comp {
	EQ("=") {
		@Override
		public Clause clause(String name, Object value) {
			return null;
		}
	},
	GT(">") {
		@Override
		public Clause clause(String name, Object value) {
			return null;
		}
	}, 
	GE(">=") {
		@Override
		public Clause clause(String name, Object value) {
			return null;
		}
	}, 
	LT("<") {
		@Override
		public Clause clause(String name, Object value) {
			return null;
		}
	}, 
	LE("<=") {
		@Override
		public Clause clause(String name, Object value) {
			return null;
		}
	},
	IN(null) {
		@Override
		public Clause clause(String name, Object value) {
			return null;
		}
	},
	;
	
	
	String op;
	
	private Comp(String op) {
		this.op = op;
	}
	
	public abstract Clause clause(String name, Object value);
}
