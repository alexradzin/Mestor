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

import java.util.Objects;

import javax.persistence.criteria.ParameterExpression;

@SuppressWarnings("serial")
public class ParameterExpressionImpl<T> extends ExpressionImpl<T> implements ParameterExpression<T> {
	private final String name;
	private final Integer position;
	private final Class<T> type;


	public ParameterExpressionImpl(final Class<T> type) {
		this(null, type);
	}

	public ParameterExpressionImpl(final String name, final Class<T> type) {
		this(name, null, type);
	}

	public ParameterExpressionImpl(final int position, final Class<T> type) {
		this(null, position, type);
	}

//	public ParameterExpressionImpl(final String name, final int position, final Class<T> type) {
//		this(name, new Integer(position), type);
//	}

	public ParameterExpressionImpl(final String name, final Integer position, final Class<T> type) {
		super(null, type);
		this.name = name;
		this.position = position;
		this.type = type;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Integer getPosition() {
		return position;
	}

	@Override
	public Class<T> getParameterType() {
		return type;
	}

    public boolean isParameter(){
        return true;
    }

    @Override
	public int hashCode() {
    	return Objects.hash(name, position, type);
    }

    @Override
    public boolean equals(final Object obj) {
    	if (obj == null || !(obj instanceof ParameterExpressionImpl)) {
    		return false;
    	}
    	@SuppressWarnings("unchecked")
		final
		ParameterExpressionImpl<T> other = (ParameterExpressionImpl<T>)obj;
    	return Objects.equals(name, other.name) && Objects.equals(position, other.position) && Objects.equals(type, other.type);
    }
}
