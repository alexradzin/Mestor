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

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.EntityType;

import org.mestor.context.EntityContext;

abstract class CriteriaChangeBase<T, S extends CriteriaChangeBase<T, S>> extends CommonAbstractCriteriaBase<T, S> implements CriteriaDelete<T> {
	CriteriaChangeBase(final EntityContext entityContext, final Class<T> targetEntity, final CriteriaBuilder queryBuilder) {
		super(entityContext, targetEntity, queryBuilder, new TreeSet<Root<?>>(new Comparator<Root<?>>() {
			// Comparator that guarantees single element set every time new element is added it replaces any old element
			@Override
			public int compare(final Root<?> o1, final Root<?> o2) {
				return 0;
			}

		}));
	}


	@Override
	public Root<T> from(final Class<T> entityClass) {
		return addRoot(new RootImpl<T>(getEntityContext(), entityClass));
	}

	@Override
	public Root<T> from(final EntityType<T> entity) {
		return from(entity.getJavaType());
	}




	@SuppressWarnings("unchecked")
	@Override
	public Root<T> getRoot() {
		final Set<Root<?>> roots = getRoots();
		return roots.isEmpty() ? null : (Root<T>)roots.iterator().next();
	}
}
