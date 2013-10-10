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

package org.mestor.entities.annotated;

import javax.persistence.AttributeConverter;

import org.mestor.entities.annotated.Person.Gender;

public class GenderAcronym implements AttributeConverter<Gender, Character> {

	@Override
	public Character convertToDatabaseColumn(Gender gender) {
		return gender.name().charAt(0);
	}

	@Override
	public Gender convertToEntityAttribute(Character dbData) {
		switch(dbData) {
			case 'M': return Gender.MALE;
			case 'F': return Gender.FEMALE;
			default:  throw new IllegalArgumentException("Illegal gender acronym " + dbData);
		}
	}

	
}
