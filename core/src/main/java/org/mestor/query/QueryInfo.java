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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class QueryInfo {
	private final QueryType type;
	// alias-to-name map. If alias is undefined name-to-name is used, i.e. name plays role of alias
	private final Map<String, String> from;
	// contains affected fields. for 'select *' and 'delete' statements values are null. what==null means "all"
	private final Map<String, Object> what;
	private final ClauseInfo where;
	private final Integer start;
	private final Integer limit;
	private final Collection<OrderByInfo> orders;


	public static enum QueryType {
		INSERT, SELECT, UPDATE, DELETE;
	}


	public QueryInfo(final QueryType type, final Map<String, Object> what, final Map<String, String> from, final ClauseInfo where, final Collection<OrderByInfo> orders, final Integer start, final Integer limit) {
		this.type = type;
		this.what = what == null ? null : Collections.unmodifiableMap(new LinkedHashMap<>(what));
		this.from = from == null ? null : Collections.unmodifiableMap(new LinkedHashMap<>(from));
		this.where = where;
		this.orders = orders == null ? null : Collections.unmodifiableCollection(new ArrayList<>(orders));
		this.start = start;
		this.limit = limit;
	}

	public QueryInfo(final QueryType type, final Map<String, Object> what, final Map<String, String> from, final ClauseInfo where, final Collection<OrderByInfo> orders, final Integer limit) {
		this(type, what, from, where, orders, null, limit);
	}

	public QueryInfo(final QueryType type, final Map<String, Object> what, final Map<String, String> from, final ClauseInfo where, final Collection<OrderByInfo> orders) {
		this(type, what, from, where, orders, null, null);
	}

	public QueryInfo(final QueryType type, final Map<String, Object> what, final Map<String, String> from) {
		this(type, what, from, null, null, null, null);
	}

	public QueryInfo(final QueryType type, final Map<String, Object> what, final String from) {
		this(type, what, from == null ? null : Collections.<String, String>singletonMap(from, from), null, null, null, null);
	}

	public QueryInfo(final QueryType type, final Collection<String> what, final String from, final ClauseInfo where, final Collection<OrderByInfo> orders, final Integer limit) {
		this(type, mapFromKeys(what), Collections.<String, String>singletonMap(from, from), where, orders, limit);
	}


	public QueryType getType() {
		return type;
	}

	public Map<String, Object> getWhat() {
		return what;
	}

	public Map<String, String> getFrom() {
		return from;
	}

	public ClauseInfo getWhere() {
		return where;
	}

	public Collection<OrderByInfo> getOrders() {
		return orders;
	}

	public Integer getStart() {
		return start;
	}

	public Integer getLimit() {
		return limit;
	}


	private static <K>  Map<K, Object> mapFromKeys(final Collection<K> keys) {
		if (keys == null) {
			return null;
		}
		final Map<K, Object> map = new LinkedHashMap<>();
		for (final K f : keys) {
			map.put(f, null);
		}
		return map;
	}

}
