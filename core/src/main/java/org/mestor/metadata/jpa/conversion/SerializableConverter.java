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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;

import javax.persistence.AttributeConverter;

public class SerializableConverter<T extends Serializable> implements AttributeConverter<T, ByteBuffer> {

	@Override
	public ByteBuffer convertToDatabaseColumn(T attribute) {
		if (attribute == null) {
			return null;
		}
		
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(attribute);
			return ByteBuffer.wrap(baos.toByteArray());
		} catch (IOException e) {
			// If IOException is thrown while writing to ByteArrayOutputStream 
			// something is going very very wrong...
			throw new IllegalStateException(e);
		}		
	}

	@Override
	public T convertToEntityAttribute(ByteBuffer dbData) {
		if (dbData == null) {
			return null;
		}
		try {
			ByteArrayInputStream bais = new ByteArrayInputStream(dbData.array());
			ObjectInputStream ois = new ObjectInputStream(bais);
			@SuppressWarnings("unchecked")
			T attribute = (T)ois.readObject();
			return attribute;
		} catch (IOException | ClassNotFoundException e) {
			// If IOException is thrown while reading from ByteArrayInputStream 
			// something is going very very wrong...
			throw new IllegalStateException(e);
		}		
	}
}
