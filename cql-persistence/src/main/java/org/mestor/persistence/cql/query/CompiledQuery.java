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
package org.mestor.persistence.cql.query;

import org.mestor.query.QueryInfo;

public class CompiledQuery {
	private final String cqlQuery;
	private final QueryInfo queryInfo;
	private final Class<?> resultType;

	private boolean aggregation;

	public CompiledQuery(final String cqlQuery, final QueryInfo queryInfo, final Class<?> resultType) {
		this(cqlQuery, queryInfo, resultType, false);
	}

	public CompiledQuery(final String cqlQuery, final QueryInfo queryInfo, final Class<?> resultType, final boolean aggregation) {
		super();
		this.cqlQuery = cqlQuery;
		this.queryInfo = queryInfo;
		this.resultType = resultType;
		this.aggregation = aggregation;
	}

	public String getCqlQuery() {
		return cqlQuery;
	}

	public QueryInfo getQueryInfo() {
		return queryInfo;
	}

	public Class<?> getResultType() {
		return resultType;
	}

	public void setAggregation(final boolean aggregation) {
		this.aggregation = aggregation;
	}

	public boolean isAggregation() {
		return this.aggregation;
	}
}
