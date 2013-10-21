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

import java.util.LinkedHashMap;
import java.util.Map;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.metamodel.SingularAttribute;

import org.mestor.context.EntityContext;

public class CriteriaUpdateImpl<T>  extends CriteriaChangeBase<T, CriteriaUpdateImpl<T>> implements CriteriaUpdate<T> {
	private final Map<Object, Object> fields = new LinkedHashMap<>();


	CriteriaUpdateImpl(final EntityContext entityContext, final Class<T> targetEntity, final CriteriaBuilder queryBuilder) {
		super(entityContext, targetEntity, queryBuilder);
	}

	@Override
	public <Y, X extends Y> CriteriaUpdate<T> set(final SingularAttribute<? super T, Y> attribute, final X value) {
		fields.put(attribute, value);
		return this;
	}

	@Override
	public <Y> CriteriaUpdate<T> set(final SingularAttribute<? super T, Y> attribute, final Expression<? extends Y> value) {
		fields.put(attribute, value);
		return this;
	}

	@Override
	public <Y, X extends Y> CriteriaUpdate<T> set(final Path<Y> attribute, final X value) {
		fields.put(attribute, value);
		return this;
	}

	@Override
	public <Y> CriteriaUpdate<T> set(final Path<Y> attribute, final Expression<? extends Y> value) {
		fields.put(attribute, value);
		return this;
	}

	@Override
	public CriteriaUpdate<T> set(final String attributeName, final Object value) {
		fields.put(attributeName, value);
		return this;
	}

}
