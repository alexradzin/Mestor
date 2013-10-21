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
import java.util.List;

import javax.persistence.criteria.Selection;

import org.mestor.context.EntityContext;

public abstract class SelectionImpl<X> implements Selection<X>, Serializable {
	private final Class<? extends X> type;
	private final EntityContext entityContext;
	private String alias;

	SelectionImpl(final EntityContext entityContext, final Class<? extends X> type) {
		this.entityContext = entityContext;
		this.type = type;
	}

	@Override
	public Class<? extends X> getJavaType() {
		return type;
	}

	@Override
	public String getAlias() {
		return alias;
	}

	@Override
	public Selection<X> alias(final String name) {
		this.alias = name;
		return this;
	}

	@Override
	public boolean isCompoundSelection() {
		return false;
	}

	@Override
	public List<Selection<?>> getCompoundSelectionItems() {
		throw new IllegalStateException("Criteria not a compound selection");
	}

	protected EntityContext getEntityContext() {
		return entityContext;
	}
}
