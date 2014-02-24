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

import java.io.Serializable;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Order;


@SuppressWarnings("serial")
public class OrderImpl implements Order, Serializable{
	protected Expression<?> expression;
    protected boolean isAscending;

    public OrderImpl(final Expression<?> expression){
        this(expression, true);
    }

    public OrderImpl(final Expression<?> expression, final boolean isAscending){
        this.expression = expression;
        this.isAscending = isAscending;
    }

    @Override
	public Expression<?> getExpression() {
        return this.expression;
    }

    @Override
    public boolean isAscending() {
        return this.isAscending;
    }

    @Override
    public Order reverse() {
        return new OrderImpl(this.expression, false);
    }

}
