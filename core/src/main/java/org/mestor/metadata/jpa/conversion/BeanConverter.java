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

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.persistence.AttributeConverter;

import org.mestor.context.EntityContext;
import org.mestor.metadata.FieldMetadata;
import org.mestor.reflection.ClassAccessor;

public class BeanConverter<T extends Object> implements AttributeConverter<T, ByteBuffer> {
	private final Class<T> clazz;
	private final EntityContext context;
	// HashMap is used in generics declaration because we need here type that implements both Map and Serializable
	private final SerializableConverter<HashMap<String, Object>> serializableConverter = new SerializableConverter<>();
	
	public BeanConverter(Class<T> clazz, EntityContext context) {
		this.clazz = clazz;
		this.context = context;
	}

	@Override
	public ByteBuffer convertToDatabaseColumn(T attribute) {
		if (attribute == null) {
			return null;
		}
		return (ByteBuffer)serialize(attribute);
	}
	
	@Override
	public T convertToEntityAttribute(ByteBuffer dbData) {
		if(dbData == null) {
			return null;
		}
		return deserialize(clazz, dbData);
	}


	private <O> Object serialize(O obj) {
		if(obj == null || obj.getClass().isPrimitive() || obj instanceof Serializable) {
			return obj;
		}
		HashMap<String, Object> map = new LinkedHashMap<>();
		@SuppressWarnings("unchecked")
		Class<O> clazz = (Class<O>)obj.getClass();
		for(FieldMetadata<O, Object, Object> fmd : context.getEntityMetadata(clazz).getFields()) {
			String name = fmd.getName();
			Object value = serialize(fmd.getAccessor().getValue(obj));
			map.put(name, value);
		}
		return serializableConverter.convertToDatabaseColumn(map);
	}
	
	private <O> O deserialize(Class<O> objType, ByteBuffer buf) {
		Map<String, Object> map = serializableConverter.convertToEntityAttribute(buf);
		O instance = ClassAccessor.newInstance(objType);
		
		for(FieldMetadata<O, Object, Object> fmd : context.getEntityMetadata(objType).getFields()) {
			String name = fmd.getName();
			Object value = map.get(name);
			if (value instanceof ByteBuffer && !ByteBuffer.class.equals((fmd.getType()))) {
				value = deserialize(fmd.getType(), (ByteBuffer)value);
			}
			fmd.getAccessor().setValue(instance, value);
		}
		
		return instance;
	}
}
