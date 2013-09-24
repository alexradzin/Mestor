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

package org.mestor.metadata.jpa.conversion;

import javax.persistence.AttributeConverter;

import org.mestor.metadata.ValueConverter;

public class ValueAttributeConverter<V, C> implements ValueConverter<V, C> {
	private final AttributeConverter<V, C> attributeConverter;
	
	public ValueAttributeConverter(AttributeConverter<V, C> attributeConverter) {
		this.attributeConverter = attributeConverter;
	}
	
	@Override
	public C toColumn(V value) {
		return attributeConverter.convertToDatabaseColumn(value);
	}

	@Override
	public V fromColumn(C column) {
		return attributeConverter.convertToEntityAttribute(column);
	}
}
