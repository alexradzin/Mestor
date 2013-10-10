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

package org.mestor.persistence.cql;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javassist.util.proxy.ProxyObject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mestor.cassandra.CassandraAwareTestRunner;
import org.mestor.context.Persistor;
import org.mestor.entities.Person;
import org.mestor.metadata.EntityMetadata;
import org.mestor.metadata.FieldMetadata;

@RunWith(CassandraAwareTestRunner.class)
public class CqlPersistorBasicCrudTest {
	private final Persistor persistor;
	private final CqlPersistorTestHelper helper;
	
	public CqlPersistorBasicCrudTest() throws IOException {
		helper = new CqlPersistorTestHelper();
		persistor = helper.getPersistor();
		doReturn(helper.getPersistor()).when(helper.ctx).getPersistor();
	}

	@Test
	public void testCrudOneIntFieldPK() {
		final String schemaName = "test1";
		try {
			helper.testCreateSchema(schemaName, null, false);
			FieldMetadata<Person, Integer, Integer> pk = helper.createFieldMetadata(Person.class, Integer.class, "id", "identifier", true);
			
			EntityMetadata<Person> emd = helper.createMetadata(Person.class, schemaName, "People", pk, pk);
			helper.testEditTable(emd, null, true);
			
			
			doReturn(emd).when(helper.ctx).getEntityMetadata(Person.class);
			
			
			Person person = new Person();
			person.setId(1);
			
			assertNull(persistor.fetch(Person.class, 1));

			assertFalse(persistor.exists(Person.class, 1));
			persistor.store(person);
			assertTrue(persistor.exists(Person.class, 1));
			Person person2 = persistor.fetch(Person.class, 1);
			assertEquals(1, person2.getId());
			assertTrue(person2 instanceof ProxyObject);
			
			assertNotNull(person2);
			assertNull(persistor.fetch(Person.class, 2));
			
			persistor.remove(person2);
			assertNull(persistor.fetch(Person.class, 1));
		} finally {
			persistor.dropSchema(schemaName);
		}
	}

	
	@Test
	public void testCrudPrimitiveFields() {
		final String schemaName = "test1";
		try {
			helper.testCreateSchema(schemaName, null, false);
			FieldMetadata<Person, Integer, Integer> pk = helper.createFieldMetadata(Person.class, int.class, "id", "identifier", true);
			
			
			@SuppressWarnings("unchecked")
			FieldMetadata<Person, Object, Object>[] fields = new FieldMetadata[] {
					pk,
					helper.createFieldMetadata(Person.class, String.class, "name", "first_name", false),
					helper.createFieldMetadata(Person.class, String.class, "lastName", "last_name", false),
					helper.createFieldMetadata(Person.class, int.class, "age", "age", false),
			};
			
			
			
			EntityMetadata<Person> emd = helper.createMetadata(Person.class, schemaName, "People", pk, fields);
			helper.testEditTable(emd, null, true);
			
			
			doReturn(emd).when(helper.ctx).getEntityMetadata(Person.class);
			
			
			Person winnie = new Person();
			winnie.setId(1);
			winnie.setName("Winnie");
			winnie.setLastName("Pooh");
			
			assertNull(persistor.fetch(Person.class, 1));

			persistor.store(winnie);
			Person winnie2 = persistor.fetch(Person.class, 1);
			assertNotNull(winnie2);
			assertEquals(1, winnie2.getId());
			assertEquals("Winnie", winnie2.getName());
			assertEquals("Pooh", winnie2.getLastName());
			assertTrue(winnie2 instanceof ProxyObject);
			
			winnie2.setLastName("The Pooh");
			persistor.store(winnie2);
			Person winnie3 = persistor.fetch(Person.class, 1);
			assertEquals("The Pooh", winnie3.getLastName());
			
			assertNull(persistor.fetch(Person.class, 2));
			
			persistor.remove(winnie2);
			assertNull(persistor.fetch(Person.class, 1));
		} finally {
			persistor.dropSchema(schemaName);
		}
	}

	
	@Test
	public void testCrudWithFieldsOfAllTypes() throws UnknownHostException {
		final String schemaName = "test1";
		try {
			helper.testCreateSchema(schemaName, null, false);
			FieldMetadata<ManySimpleTypes, Integer, Integer> pk = helper.createFieldMetadata(ManySimpleTypes.class, int.class, "intPrimitive", "intPrimitive", true);
			
			@SuppressWarnings("unchecked")
			FieldMetadata<ManySimpleTypes, Object, Object>[] fields = new FieldMetadata[] {
					pk,
					helper.createFieldMetadata(ManySimpleTypes.class, long.class, "longPrimitive"),
					helper.createFieldMetadata(ManySimpleTypes.class, float.class, "floatPrimitive"),
					helper.createFieldMetadata(ManySimpleTypes.class, double.class, "doublePrimitive"), 
					helper.createFieldMetadata(ManySimpleTypes.class, Integer.class, "intWrapper"),
					helper.createFieldMetadata(ManySimpleTypes.class, Long.class, "longWrapper"),
					helper.createFieldMetadata(ManySimpleTypes.class, Float.class, "floatWrapper"),
					helper.createFieldMetadata(ManySimpleTypes.class, Double.class, "doubleWrapper"),
					helper.createFieldMetadata(ManySimpleTypes.class, BigDecimal.class, "bigDecimal"),
					helper.createFieldMetadata(ManySimpleTypes.class, BigInteger.class, "bigInteger"),
					helper.createFieldMetadata(ManySimpleTypes.class, boolean.class, "booleanPrimitive"),
					helper.createFieldMetadata(ManySimpleTypes.class, Boolean.class, "booleanWrapper"),
					helper.createFieldMetadata(ManySimpleTypes.class, String.class, "string"),
					helper.createFieldMetadata(ManySimpleTypes.class, ByteBuffer.class, "bytebuffer"),
					helper.createFieldMetadata(ManySimpleTypes.class, byte[].class, "bytearray"),
					helper.createFieldMetadata(ManySimpleTypes.class, InetAddress.class, "inet"),
					helper.createFieldMetadata(ManySimpleTypes.class, Date.class, "date"),
					helper.createFieldMetadata(ManySimpleTypes.class, UUID.class, "uuid"),
			};
			
			
			
			EntityMetadata<ManySimpleTypes> emd = helper.createMetadata(ManySimpleTypes.class, schemaName, "TypesTest", pk, fields);
			helper.testEditTable(emd, null, true);
			
			
			doReturn(emd).when(helper.ctx).getEntityMetadata(ManySimpleTypes.class);
			
			
			ManySimpleTypes obj = new ManySimpleTypes();
			obj.setIntPrimitive(1);
			obj.setLongPrimitive(2);
			obj.setFloatPrimitive(3.1415926f);
			obj.setDoublePrimitive(2.718281828);
			
			
			obj.setIntWrapper(-1);
			obj.setLongWrapper(-2);
			obj.setFloatWrapper(-3.1415926f);
			obj.setDoubleWrapper(-2.718281828);
			
			
			obj.setBigDecimal(new BigDecimal(12345));
			obj.setBigInteger(new BigInteger("54321"));
			
			obj.setBooleanPrimitive(true);
			obj.setBooleanWrapper(false);
			
			obj.setString("Hello, world!");
			obj.setBytebuffer(ByteBuffer.wrap("Cassandra, the Priam's daughter".getBytes()));
			obj.setBytearray("Priam, the king of Troy".getBytes());
			
			obj.setInet(InetAddress.getLocalHost());
			Date now = new Date();
			obj.setDate(now);
			UUID uuid = UUID.randomUUID();
			obj.setUuid(uuid);
			
			
			assertNull(persistor.fetch(ManySimpleTypes.class, 1));

			persistor.store(obj);
			ManySimpleTypes obj2 = persistor.fetch(ManySimpleTypes.class, 1);
			assertNotNull(obj2);
			assertTrue(obj2 instanceof ProxyObject);

			assertEquals(1, obj2.getIntPrimitive());
			
			assertNull(persistor.fetch(ManySimpleTypes.class, 2));
			
			persistor.remove(obj2);
			assertNull(persistor.fetch(ManySimpleTypes.class, 1));
		} finally {
			persistor.dropSchema(schemaName);
		}
	}
	
	
	@Test
	public void testCreateCrudWithInnerCollections() {
		final String schemaName = "test1";
		try {
			helper.testCreateSchema(schemaName, null, false);
			
			FieldMetadata<InnerCollections, Integer, Integer> pk = helper.createFieldMetadata(InnerCollections.class, int.class, "id", "id", true);
			
			@SuppressWarnings("unchecked")
			FieldMetadata<ManySimpleTypes, Object, Object>[] fields = new FieldMetadata[] {
					pk,
					helper.createFieldMetadata(InnerCollections.class, String[].class, "stringArray"),
					helper.createFieldMetadata(InnerCollections.class, List.class, new Class[] {String.class}, "stringList"),
					helper.createFieldMetadata(InnerCollections.class, Set.class, new Class[] {String.class}, "stringSet"),
					helper.createFieldMetadata(InnerCollections.class, Map.class, new Class[] {String.class, Integer.class}, "stringToIntegerMap"),
			};
			
			
			EntityMetadata<InnerCollections> emd = helper.createMetadata(InnerCollections.class, schemaName, "InnerCollections", pk, fields);
			helper.testEditTable(emd, null, true);
			
			doReturn(emd).when(helper.ctx).getEntityMetadata(InnerCollections.class);
			
			
			
			InnerCollections obj = new InnerCollections();
			
			
			final String[] rgbArr = new String[] {"red", "green", "blue"};
			final List<String> rgbList = Arrays.asList("RED", "GREEN", "BLUE");
			final Set<String> rgbSet = new HashSet<>(Arrays.asList("Red", "Green", "Blue"));
			final Map<String, Integer> rgbMap = new HashMap<String, Integer>(){{
				put("rEd", 0xFF0000);
				put("grEEn", 0x00FF00);
				put("blUe", 0x0000FF);
			}};
			
			
			
			obj.setId(1);
			obj.setStringArray(rgbArr);
			obj.setStringList(rgbList);
			obj.setStringSet(rgbSet);
			obj.setStringToIntegerMap(rgbMap);
			
			
			assertNull(persistor.fetch(InnerCollections.class, 1));

			persistor.store(obj);
			
			InnerCollections obj2 = persistor.fetch(InnerCollections.class, 1);
			assertNotNull(obj2);
			assertNull(persistor.fetch(InnerCollections.class, 2));
			
			assertEquals(1, obj2.getId());
			assertArrayEquals(rgbArr, obj2.getStringArray());
			assertEquals(rgbList, obj2.getStringList());
			assertEquals(rgbSet, obj2.getStringSet());
			assertEquals(rgbMap, obj2.getStringToIntegerMap());
			

			persistor.remove(obj);
			assertNull(persistor.fetch(InnerCollections.class, 1));
		} finally {
			persistor.dropSchema(schemaName);
		}
	}
	

	@Test
	public void testCrudLazy() {
		final String schemaName = "test1";
		try {
			helper.testCreateSchema(schemaName, null, false);
			FieldMetadata<Person, Integer, Integer> pk = helper.createFieldMetadata(Person.class, Integer.class, "id", "identifier", true);
			
			
			@SuppressWarnings("unchecked")
			FieldMetadata<Person, Object, Object>[] fields = new FieldMetadata[] {
					pk,
					helper.createFieldMetadata(Person.class, String.class, "name", "first_name", false),
			};
			
			
			EntityMetadata<Person> emd = helper.createMetadata(Person.class, schemaName, "People", pk, fields);
			helper.testEditTable(emd, null, true);
			
			
			doReturn(emd).when(helper.ctx).getEntityMetadata(Person.class);
			doReturn(persistor).when(helper.ctx).getPersistor();
			
			
			Person winnie = new Person();
			winnie.setId(1);
			winnie.setName("Winnie the Pooh");
			
			persistor.store(winnie);
			Person winnie2 = persistor.fetch(Person.class, 1);
			assertNotNull(winnie2);
			assertEquals(1, winnie2.getId());
			

			// extract wrapped object and check that the value is there. 
			Person winnie2_ = persistor.getObjectWrapperFactory(Person.class).unwrap(winnie2);
			assertEquals("Winnie the Pooh", winnie2_.getName());

			// now check that the value can be accessed when getter is called on wrapper object
			assertEquals("Winnie the Pooh", winnie2.getName());

			
			
			// make name lazy
			fields[1].setLazy(true);
			
			
			Person winnie3 = persistor.fetch(Person.class, 1);
			assertNotNull(winnie3);
			assertEquals(1, winnie3.getId());

			// extract wrapped object and check that the value is null 
			assertNull(persistor.getObjectWrapperFactory(Person.class).unwrap(winnie3).getName());

			// now check that the value can be accessed when getter is called on wrapper object
			assertEquals("Winnie the Pooh", winnie3.getName());
			// check the wrapped object again. Now the value should be there.
			assertEquals("Winnie the Pooh", persistor.getObjectWrapperFactory(Person.class).unwrap(winnie3).getName());
		} finally {
			persistor.dropSchema(schemaName);
		}
	}
	
	
}
