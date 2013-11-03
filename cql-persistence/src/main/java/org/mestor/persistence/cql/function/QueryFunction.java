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

package org.mestor.persistence.cql.function;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.TreeMap;

import com.google.common.base.Function;

public class QueryFunction<R, A> implements Function<A, R> {
	public final static QueryFunction<Long, Number> MIN = new QueryFunction<Long, Number>(true, Long.class, Number.class) {
		private long min = Long.MAX_VALUE;
		@Override
		public Long apply(final Number input) {
			final long longInput = input.longValue();
			if (longInput < min) {
				min = longInput;
			}
			return min;
		}
	};

	public final static QueryFunction<Long, Number> MAX = new QueryFunction<Long, Number>(true, Long.class, Number.class) {
		private long max = Long.MIN_VALUE;
		@Override
		public Long apply(final Number input) {
			final long longInput = input.longValue();
			if (longInput > max) {
				max = longInput;
			}
			return max;
		}
	};

	public final static QueryFunction<Long, Number> SUM = new QueryFunction<Long, Number>(true, Long.class, Number.class) {
		private long sum = 0;
		@Override
		public Long apply(final Number input) {
			final long longInput = input.longValue();
			sum += longInput;
			return sum;
		}
	};

	public final static QueryFunction<Double, Number> AVG = new QueryFunction<Double, Number>(true, Double.class, Number.class) {
		private double avg = 0;
		private long count = 0;

		@Override
		public Double apply(final Number input) {
			avg = ((avg * count) + input.longValue()) / ++count;
			System.out.println("avg:" + avg);
			return avg;
		}
	};



	public final static QueryFunction<String, String> UPPER = new QueryFunction<String, String>(false, String.class, String.class) {
		@Override
		public String apply(final String input) {
			return input == null ? null : input.toUpperCase();
		}
	};

	public final static QueryFunction<String, String> LOWER = new QueryFunction<String, String>(false, String.class, String.class) {
		@Override
		public String apply(final String input) {
			return input == null ? null : input.toLowerCase();
		}
	};

	public final static QueryFunction<Integer, String> LENGTH = new QueryFunction<Integer, String>(false, Integer.class, String.class) {
		@Override
		public Integer apply(final String input) {
			return input == null ? 0 : input.length();
		}
	};

	public final static QueryFunction<String, String> TRIM = new QueryFunction<String, String>(false, String.class, String.class) {
		@Override
		public String apply(final String input) {
			return input == null ? null : input.trim();
		}
	};


	private final static Map<String, QueryFunction<?,?>> functions = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

	static {
		Field nameField;
		try {
			nameField = QueryFunction.class.getDeclaredField("name");
		} catch (final NoSuchFieldException e1) {
			throw new IllegalStateException("Cannot retrieve name field");
		}
		for (final Field f : QueryFunction.class.getFields()) {
			if (QueryFunction.class.equals(f.getType())) {
				try {
					final String name = f.getName();
					final QueryFunction<?,?> func = (QueryFunction<?,?>)f.get(null);
					functions.put(name, func);
					nameField.set(func, name);
				} catch (final IllegalAccessException e) {
					throw new IllegalStateException(e);
				}
			}
		}
	}

	private final boolean aggregation;
	private Class<R> returnType;
	private Class<A> argumentType;
	private String name;



	private QueryFunction(final boolean aggregation) {
		this.aggregation = aggregation;
	}

	private QueryFunction(final boolean aggregation, final Class<R> returnType, final Class<A> argumentType) {
		this.aggregation = aggregation;
		this.returnType = returnType;
		this.argumentType = argumentType;
	}



	public static <R, A> QueryFunction<R, A> valueOf(final String name) {
		if (name == null) {
			throw new NullPointerException(name);
		}
		@SuppressWarnings("unchecked")
		final QueryFunction<R, A>  function = (QueryFunction<R, A>)functions.get(name);
		if (function == null) {
			throw new IllegalArgumentException(name);
		}
		return function;
	}



	@Override
	public R apply(final A input) {
		return null;
	}


	public boolean isAggregation() {
		return aggregation;
	}

	public Class<R> getReturnType() {
		return returnType;
	}

	public String name() {
		return name;
	}

	@Override
	public String toString() {
		return name();
	}
}
