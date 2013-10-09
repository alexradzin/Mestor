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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.doReturn;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mestor.cassandra.CassandraAwareTestRunner;
import org.mestor.context.Persistor;
import org.mestor.entities.Country;
import org.mestor.entities.Passport;
import org.mestor.entities.Person;
import org.mestor.entities.Person.Gender;
import org.mestor.metadata.EntityMetadata;
import org.mestor.metadata.FieldMetadata;
import org.mestor.metadata.jpa.conversion.EnumNameConverter;
import org.mestor.metadata.jpa.conversion.SerializableConverter;
import org.mestor.metadata.jpa.conversion.ValueAttributeConverter;
import org.mestor.reflection.CompositePropertyAccessor;
import org.mestor.reflection.PropertyAccessor;

/**
 * JPA provides 2 ways to create a composite primary key.<br/>
 *  
 * <h3>Embeddable primary key</h3>
 * Create special class {@code MyKey} and add field of this type to your entity:
 * 
 * <pre>
 * <code>
 *	&#64;Entity
 *	public class YourEntity {
 *	    &#64;EmbeddedId
 *	    private MyKey myKey;
 *	    Column(name = "ColumnA")
 *	    private String columnA;
 *	}
 *	
 *	&#64;Embeddable
 *	public class MyKey implements Serializable {
 *	    private int id;
 *	    private int version;
 *	} 
 * </code>
 * </pre> 
 * 
 * <h3>Separated primary key</h3>
 * In this case the parts of primary key are stored in their own columns that belong
 * to the entity. Special class that represents primary key and has the same fields is
 * still required and must be referenced using special annotation &#64;{@code IdClass} 
 * 
 * <pre>
 * <code>
 *	&#64;Entity
 *	&#64;IdClass(MyKey.class)
 *	public class YourEntity {
 *	   &#64;Id
 *	   private int id;
 *	   &#64;Id
 *	   private int version;
 *	}
 *	
 *	public class MyKey implements Serializable {
 *	   private int id;
 *	   private int version;
 *	}  
 * </code>
 * </pre>
 * 
 * This test case contains tests that verify both modes.
 * 
 * @author alexr
 *
 */
@RunWith(CassandraAwareTestRunner.class)
public class CqlPersistorCompositePrimaryKeyTest {
	private final Persistor persistor;
	private final CqlPersistorTestHelper helper;

	
	public CqlPersistorCompositePrimaryKeyTest() throws IOException {
		helper = new CqlPersistorTestHelper();
		persistor = helper.getPersistor();
		doReturn(helper.getPersistor()).when(helper.ctx).getPersistor();
	}

	@Test
	public void testEmbeddedId() {
		final String schemaName = "test1";
		try {
			helper.testCreateSchema(schemaName, null, false);
			FieldMetadata<Person, Passport, ByteBuffer> pk = helper.createFieldMetadata(Person.class, Passport.class, "passport", "passport", true);
			pk.setColumnType(ByteBuffer.class);
			pk.setConverter(new ValueAttributeConverter<>(new SerializableConverter<Passport>()));
			
			
			@SuppressWarnings("unchecked")
			FieldMetadata<Person, Object, Object>[] fields = new FieldMetadata[] {
					pk,
					helper.createFieldMetadata(Person.class, String.class, "name", "first_name", false),
					helper.createFieldMetadata(Person.class, String.class, "lastName", "last_name", false),
			};
			
			
			
			EntityMetadata<Person> emd = helper.createMetadata(Person.class, schemaName, "People", pk, fields);
			helper.testEditTable(emd, null, true);
			
			
			doReturn(emd).when(helper.ctx).getEntityMetadata(Person.class);
			
			Person winnie = new Person(0, "Winnie-the-Pooh", "Sanders", Gender.MALE);
			Passport winniePassport = new Passport(Country.GB, "1");
			winnie.setPassport(winniePassport);
			
			Person piglet = new Person(0, "Piglet", "Trespassers W", Gender.MALE);
			Passport pigletPassport = new Passport(Country.GB, "2");
			piglet.setPassport(pigletPassport);
			
			assertNull(persistor.fetch(Person.class, winniePassport));
			assertNull(persistor.fetch(Person.class, pigletPassport));
			
			persistor.store(winnie);
			persistor.store(piglet);
			
			Person winnie2 = persistor.fetch(Person.class, winniePassport);
			Person piglet2 = persistor.fetch(Person.class, pigletPassport);
			
			assertNotNull(winnie2);
			assertNotNull(piglet2);
			
			assertEquals("Winnie-the-Pooh", winnie2.getName());
			assertEquals("Piglet", piglet2.getName());
			
			
			persistor.remove(winnie);
			persistor.remove(piglet);
		} finally {
			persistor.dropSchema(schemaName);
		}
		
	}

	
	@Test
	public void testSeparateCompositeKey() {
		final String schemaName = "test1";
		try {
			helper.testCreateSchema(schemaName, null, false);
			
			// Create PK metadata
			
			// first create metadata for fields that a the part of the PK.
			FieldMetadata<Person, Country, String> country = helper.createFieldMetadata(Person.class, Country.class, "country", "country", false);
			country.setColumnType(String.class);
			country.setConverter(new ValueAttributeConverter<>(new EnumNameConverter<Country>(Country.class)));

			FieldMetadata<Person, String, String> passportId = helper.createFieldMetadata(Person.class, String.class, "passportId", "passport", false);
			

			// now create composite accessor that play a role of "bridge" between real fields defined in entity 
			// and their representation in PK class
			
			@SuppressWarnings("unchecked")
			PropertyAccessor<Person, Object>[] primaryKeyAccessors = new PropertyAccessor[] {country.getAccessor(), passportId.getAccessor()};
			CompositePropertyAccessor<Person, Passport> passportAccessor = new CompositePropertyAccessor<Person, Passport>(Person.class, Passport.class, null, primaryKeyAccessors); 
			
			FieldMetadata<Person, Passport, ByteBuffer> pk = helper.createFieldMetadata(Person.class, Passport.class, null, "global_passport", true);
			pk.setColumnType(ByteBuffer.class);
			pk.setConverter(new ValueAttributeConverter<>(new SerializableConverter<Passport>()));
			pk.setAccessor(passportAccessor);
			
			
			@SuppressWarnings("unchecked")
			FieldMetadata<Person, Object, Object>[] fields = new FieldMetadata[] {
					// fields that compose PK
					country,
					passportId,
					
					// regular fields
					helper.createFieldMetadata(Person.class, String.class, "name", "first_name", false),
					helper.createFieldMetadata(Person.class, String.class, "lastName", "last_name", false),
			};
			
			
			
			EntityMetadata<Person> emd = helper.createMetadata(Person.class, schemaName, "People", pk, fields);
			helper.testEditTable(emd, null, true);
			
			
			doReturn(emd).when(helper.ctx).getEntityMetadata(Person.class);
			
			Person winnie = new Person(0, "Winnie-the-Pooh", "Sanders", Gender.MALE);
			winnie.setCountry(Country.GB);
			winnie.setPassportId("12345");
			
			Person piglet = new Person(0, "Piglet", "Trespassers W", Gender.MALE);
			piglet.setCountry(Country.GB);
			piglet.setPassportId("54321");

			

			
			///////////////////////////////
			
			Passport winniePassport = new Passport(Country.GB, "12345");
			Passport pigletPassport = new Passport(Country.GB, "54321");
			
			assertNull(persistor.fetch(Person.class, winniePassport));
			assertNull(persistor.fetch(Person.class, pigletPassport));
			
			persistor.store(winnie);
			persistor.store(piglet);
			
			Person winnie2 = persistor.fetch(Person.class, winniePassport);
			Person piglet2 = persistor.fetch(Person.class, pigletPassport);
			
			assertNotNull(winnie2);
			assertNotNull(piglet2);
			
			assertEquals("Winnie-the-Pooh", winnie2.getName());
			assertEquals("Piglet", piglet2.getName());
			
			
			persistor.remove(winnie);
			persistor.remove(piglet);
		} finally {
			persistor.dropSchema(schemaName);
		}
		
	}
	
	
}
