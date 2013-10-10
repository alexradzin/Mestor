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

/**
 * Dummy implmentation of {@link AttributeConverter} that actually does no conversion 
 * returning the same object that was received as a parameter.
 * 
 * Generally this class should be parametrized by one parameter only because both its methods
 * return the same type that they get. However we have to use 2 parameters that by convention 
 * must be the same to make access to this converter more convenient and similar to other 
 * converters. 
 * @author alexr
 *
 * @param <T>
 */
public class DummyAttributeConverter<X, Y> implements AttributeConverter<X, Y> {

	@SuppressWarnings("unchecked")
	@Override
	public Y convertToDatabaseColumn(X attribute) {
		return (Y)attribute;
	}

	@SuppressWarnings("unchecked")
	@Override
	public X convertToEntityAttribute(Y dbData) {
		return (X)dbData;
	}


}
