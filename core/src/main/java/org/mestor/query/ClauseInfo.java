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

package org.mestor.query;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

public class ClauseInfo {
	private final String field;
	private final Operand operand;
	private final Object expression;

	public static enum Operand {
		/**
		 * ==
		 */
		EQ("="),
		/**
		 * !=
		 */
		NE("<>"),
		/**
		 * >
		 */
		GT(">"),
		/**
		 * >=
		 */
		GE(">="),
		/**
		 * <
		 */
		LT("<"),
		/**
		 * <=
		 */
		LE("<="),
		/**
		 * like
		 */
		LIKE("LIKE"),
		/**
		 * not like
		 */
		NOT_LIKE("NOT LIKE"),
		/**
		 * in
		 */
		IN(Object[].class, "IN"),
		/**
		 * not
		 */
		NOT("NOT"),

		AND(Object[].class, "AND"),
		OR(Object[].class, "OR"),
		;


		private final Class<?> paramType;
		private final String symbol;
		private final static Map<String, Operand> symbol2operand = new TreeMap<String, Operand>(String.CASE_INSENSITIVE_ORDER) {{
			for (final Operand o : Operand.values()) {
				put(o.symbol(), o);
			}
		}};

		private Operand(final Class<?> paramType, final String symbol) {
			this.paramType = paramType;
			this.symbol = symbol;

		}

		private Operand(final String symbol) {
			this(Object.class, symbol);
		}

		public boolean isArrayParameter() {
			return paramType.isArray();
		}

		public String symbol() {
			return symbol;
		}

		public static Operand bySymbol(final String symbol) {
			return symbol2operand.get(symbol);
		}
	}


	public ClauseInfo(final String field, final Operand operand, final Object expression) {
		this.field = field;
		this.operand = operand;

		if (operand.isArrayParameter()) {
			if (expression.getClass().isArray()) {
				this.expression = expression;
			} else if (expression instanceof Collection) {
				this.expression = ((Collection<?>)expression).toArray();
			} else {
				this.expression = expression;
			}
		} else {
			if (expression.getClass().isArray()) {
				this.expression = Array.get(expression, 0);
			} else if (expression instanceof Collection) {
				this.expression = ((Collection<?>)expression).iterator().next();
			} else {
				this.expression = expression;
			}
		}
	}


	public String getField() {
		return field;
	}


	public Operand getOperand() {
		return operand;
	}


	public Object getExpression() {
		return expression;
	}
}
