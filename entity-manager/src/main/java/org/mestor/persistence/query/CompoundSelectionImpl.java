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

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CompoundSelection;
import javax.persistence.criteria.Selection;

import org.mestor.context.EntityContext;


@SuppressWarnings("serial")
public class CompoundSelectionImpl<X> extends SelectionImpl<X> implements CompoundSelection<X> {
	protected List<Selection<?>> subSelections;

	CompoundSelectionImpl(final EntityContext entityContext, final Class<? extends X> type) {
		super(entityContext, type);
	}

    @SuppressWarnings("unchecked")
	public CompoundSelectionImpl(final EntityContext entityContext, final Class<? extends X> type, @SuppressWarnings("rawtypes") final Selection[] subSelections) {
    	this(entityContext, type);
        this.subSelections = new ArrayList<Selection<?>>();
        for (final Selection<X> sel : subSelections) {
        	this.subSelections.add(sel);
        }
    }


	@Override
	public boolean isCompoundSelection() {
		return true;
	}

	@Override
	public List<Selection<?>> getCompoundSelectionItems() {
		return subSelections;
	}


}
