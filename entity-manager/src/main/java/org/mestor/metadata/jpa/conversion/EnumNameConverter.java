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

public class EnumNameConverter<T extends Enum<T>> implements AttributeConverter<T, String> {
	private final Class<T> enumType;
	
	public EnumNameConverter(Class<T> enumType) {
		this.enumType = enumType;
	}

	
	@Override
	public String convertToDatabaseColumn(T attribute) {
		return attribute == null ? null : attribute.name();
	}

	@Override
	public T convertToEntityAttribute(String name) {
		return name == null ? null : Enum.valueOf(enumType, name);
	}
}
