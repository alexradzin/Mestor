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

@SuppressWarnings("serial")
public class ConstantExpresion<T> extends ExpressionImpl<T> {
	private final T value;

	@SuppressWarnings("unchecked")
	ConstantExpresion(final T value) {
		this((Class<T>)value.getClass(), value);
	}

	ConstantExpresion(final Class<? extends T> type, final T value) {
		super(null, type);
		this.value = value;
	}

	public T getValue() {
		return value;
	}
}
