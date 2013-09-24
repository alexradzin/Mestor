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

import java.nio.ByteBuffer;

import javax.persistence.AttributeConverter;

public class ByteArrayToBufferConverter implements AttributeConverter<byte[], ByteBuffer> {

	@Override
	public ByteBuffer convertToDatabaseColumn(byte[] array) {
		return array == null ? null : ByteBuffer.wrap(array);
	}

	@Override
	public byte[] convertToEntityAttribute(ByteBuffer buffer) {
		return buffer == null ? null : buffer.array();
	}


}
