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

package org.mestor.persistence.query;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Selection;


public class CriteriaQueryImpl<T> extends AbstractQueryImpl<T, CriteriaQueryImpl<T>> implements CriteriaQuery<T> {
    private List<Order> orderBy;

	public CriteriaQueryImpl(final Class<T> resultClass, final CriteriaBuilder queryBuilder) {
		super(resultClass, queryBuilder);
	}




	@Override
	public CriteriaQuery<T> select(final Selection<? extends T> selection) {
        this.selection = selection;
		return this;
	}

	@Override
	public CriteriaQuery<T> multiselect(final Selection<?>... selections) {
		return multiselect(asList(selections));
	}

	@Override
	public CriteriaQuery<T> multiselect(final List<Selection<?>> selectionList) {
		throw new UnsupportedOperationException();
	}



	@Override
	public CriteriaQuery<T> orderBy(final Order... o) {
		return orderBy(asList(o));
	}

	@Override
	public CriteriaQuery<T> orderBy(final List<Order> o) {
        this.orderBy = o;
        return this;
	}


	@Override
	public List<Order> getOrderList() {
		return orderBy;
	}

	@Override
	public Set<ParameterExpression<?>> getParameters() {
		return Collections.emptySet();
	}


}
