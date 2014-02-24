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

import java.util.Collection;
import java.util.Map;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.Bindable;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;

import org.mestor.context.EntityContext;

@SuppressWarnings("serial")
public class PathImpl<X> extends ExpressionImpl<X> implements Path<X> {
	private final static String PATHNODE_IS_PRIMITIVE_NODE = "pathnode_is_primitive_node";
	private final static String PATHNODE_TYPE_DOES_NOT_APPLY_TO_PRIMITIVE_NODE = "pathnode_type_does_not_apply_to_primitive_node";

    protected Path<?> pathParent;
    protected Object modelArtifact;


    public PathImpl(final EntityContext entityContext, final Path<?> parent, final Class<? extends X> javaClass, final Bindable<X> modelArtifact) {
        super(entityContext, javaClass);
        this.pathParent = parent;
        this.modelArtifact = modelArtifact;
        if (modelArtifact instanceof Attribute) {
        	this.alias(((Attribute<?, ?>)modelArtifact).getName());
        }
    }


	@SuppressWarnings("unchecked")
	@Override
	public Bindable<X> getModel() {
        return (Bindable<X>) this.modelArtifact;
	}

	@Override
	public Path<?> getParentPath() {
        return this.pathParent;
	}

	@Override
	public <Y> Path<Y> get(final SingularAttribute<? super X, Y> attribute) {
		throw failure(PATHNODE_IS_PRIMITIVE_NODE);
	}

	@Override
	public <E, C extends Collection<E>> Expression<C> get(final PluralAttribute<X, C, E> collection) {
		throw failure(PATHNODE_IS_PRIMITIVE_NODE);
	}

	@Override
	public <K, V, M extends Map<K, V>> Expression<M> get(final MapAttribute<X, K, V> map) {
		throw failure(PATHNODE_IS_PRIMITIVE_NODE);
	}

	@Override
	public Expression<Class<? extends X>> type() {
		throw failure(PATHNODE_TYPE_DOES_NOT_APPLY_TO_PRIMITIVE_NODE);
	}

	@Override
	public <Y> Path<Y> get(final String attributeName) {
		throw failure(PATHNODE_IS_PRIMITIVE_NODE);
	}

	private RuntimeException failure(final String msg) {
		return new IllegalStateException(msg);
	}
}
