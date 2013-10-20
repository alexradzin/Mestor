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

import com.google.common.base.Objects;

public class OrderByInfo {
	private final String field;
	private final Order order;

	public static enum Order {
		ASC, DSC;
	}


	public OrderByInfo(final String field, final Order order) {
		this.field = field;
		this.order = order;
	}


	public String getField() {
		return field;
	}


	public Order getOrder() {
		return order;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null || !(obj instanceof OrderByInfo)) {
			return false;
		}
		return equals((OrderByInfo)obj);
	}

	public boolean equals(final OrderByInfo other) {
		return Objects.equal(field, other.field) && Objects.equal(order, other.order);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(field, order);
	}
}
