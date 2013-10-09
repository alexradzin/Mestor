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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.annotation.ElementType;
import java.nio.ByteBuffer;

import javax.persistence.AttributeConverter;

import org.junit.Test;
import org.mestor.entities.Country;
import org.mestor.entities.Passport;
import org.mestor.entities.Person;
import org.mestor.metadata.EntityMetadata;
import org.mestor.metadata.FieldMetadata;
import org.mestor.metadata.MetadataTestHelper;

public class AttributeConverterTest {
	private MetadataTestHelper helper = new MetadataTestHelper();
	
	@Test
	public void testDummyAttributeConverter() {
		test(new DummyAttributeConverter<String, String>(), null, null);
		test(new DummyAttributeConverter<String, String>(), "hello", "hello");
	}
	
	@Test
	public void testByteArrayToBufferConverter() {
		test(new ByteArrayToBufferConverter(), null, null);
		
		final byte[] bytes = new byte[0];
		test(new ByteArrayToBufferConverter(), bytes, ByteBuffer.wrap(bytes));
	}


	@Test
	public void testSerializableConverter() throws IOException {
		test(new SerializableConverter<String>(), null, null);
		
		String str = "hello";
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(str);
		
		test(new SerializableConverter<String>(), str, ByteBuffer.wrap(baos.toByteArray()));
		
		
		Passport p = new Passport(Country.IL, "1234567890");
		Passport p2 = new SerializableConverter<Passport>().convertToEntityAttribute(new SerializableConverter<Passport>().convertToDatabaseColumn(p));
		assertEquals(p.getCountry(), p2.getCountry());
		assertEquals(p.getPassportId(), p2.getPassportId());
	}
	
	@Test
	public void testEnumOrdinalConverter() {
		for (ElementType et : ElementType.values()) {
			test(new EnumOrdinalConverter<ElementType>(ElementType.class), et, et.ordinal());
		}
	}
	
	@Test
	public void testEnumNameConverter() {
		for (ElementType et : ElementType.values()) {
			test(new EnumNameConverter<ElementType>(ElementType.class), et, et.name());
		}
	}

	@Test
	public void testBeanConverter() {
		Person jfk = new Person();
		jfk.setName("John");
		jfk.setLastName("Kennedy");
		jfk.setAge(46);

		
		FieldMetadata<Person, Integer, Integer> pk = helper.createFieldMetadata(Person.class, int.class, "id", "identifier", true);
		
		
		@SuppressWarnings("unchecked")
		FieldMetadata<Person, Object, Object>[] fields = new FieldMetadata[] {
				pk,
				helper.createFieldMetadata(Person.class, String.class, "name", "first_name", false),
				helper.createFieldMetadata(Person.class, String.class, "lastName", "last_name", false),
				helper.createFieldMetadata(Person.class, int.class, "age", "age", false),
		};
		
		EntityMetadata<Person> emd = helper.createMetadata(Person.class, "ConverterTest", "People", pk, fields);
		doReturn(emd).when(helper.ctx).getEntityMetadata(Person.class);
		
		test(new BeanConverter<Person>(Person.class, helper.ctx), jfk, null);
	}
	
	
	private <X> void test(AttributeConverter<X, ByteBuffer> converter, X attribute, ByteBuffer buf) {
		assertEquals(attribute, converter.convertToEntityAttribute(converter.convertToDatabaseColumn(attribute)));
		assertArrayEquals(array(buf), array(converter.convertToDatabaseColumn(converter.convertToEntityAttribute(buf))));
	}
	
	
	private <X, Y> void test(AttributeConverter<X, Y> converter, X attribute, Y dbData) {
		assertEquals(attribute, converter.convertToEntityAttribute(converter.convertToDatabaseColumn(attribute)));
		assertEquals(dbData, converter.convertToDatabaseColumn(converter.convertToEntityAttribute(dbData)));
	}
	
	private byte[] array(ByteBuffer buf) {
		return buf == null ? null : buf.array();
	}
}
