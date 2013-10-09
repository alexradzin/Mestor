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
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doReturn;

import java.io.IOException;
import java.lang.reflect.AccessibleObject;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mestor.cassandra.CassandraAwareTestRunner;
import org.mestor.context.Persistor;
import org.mestor.entities.Employee;
import org.mestor.entities.Person;
import org.mestor.entities.Student;
import org.mestor.entities.Person.Gender;
import org.mestor.metadata.EntityMetadata;
import org.mestor.metadata.FieldMetadata;
import org.mestor.reflection.Access;
import org.mestor.reflection.ClassNameAccess;
import org.mestor.reflection.PropertyAccessor;

import com.datastax.driver.core.TableMetadata;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.ObjectArrays;

@RunWith(CassandraAwareTestRunner.class)
public abstract class CqlPersistorInheritanceTestCase {
	protected final Persistor persistor;
	protected final CqlPersistorTestHelper helper;

	protected final String schemaName = "test1";

	private final static String PASCAL_STUDENT_ID = "chemist1";


	protected CqlPersistorInheritanceTestCase() {
		try {
			helper = new CqlPersistorTestHelper();
		} catch (IOException e) {
			throw new AssertionError("Cannot start test", e);
		}
		persistor = helper.getPersistor();
		doReturn(helper.getPersistor()).when(helper.ctx).getPersistor();
	}

	@Test
	public void testCreateSchema() {
		try {
			helper.testCreateSchema(schemaName, null, false);
			createTables();
			assertDbSchma();
		} finally {
			persistor.dropSchema(schemaName);
		}
	}

	@Test
	public void testCrudTable() {
		try {
			helper.testCreateSchema(schemaName, null, false);
			createTables();
			testStoreAndFetchHierarchy();
		} finally {
			persistor.dropSchema(schemaName);
		}
	}

	protected abstract void createTables();

	protected void assertDbSchma() {
		Collection<TableMetadata> tables = ((CqlPersistor) persistor).getCluster().getMetadata()
				.getKeyspace(schemaName).getTables();

		Collection<String> actualTableNames = Collections2.transform(tables, new Function<TableMetadata, String>() {
			@Override
			public String apply(TableMetadata meta) {
				return meta.getName();
			}
		});

		assertEquals(new HashSet<String>(Arrays.asList(getExpectedTables())), new HashSet<String>(actualTableNames));
	}

	protected abstract String[] getExpectedTables();

	public void testStoreAndFetchHierarchy() {
		Person torricelli = new Person(1, "Evangelista", "Torricelli", Gender.MALE);
		final String PASCAL_COLLEGE = "Home"; // Pascal was educated by his
												// father.
		Student pascal = new Student(2, "Blaise", "Pascal", Gender.MALE, PASCAL_STUDENT_ID, PASCAL_COLLEGE);

		persistor.store(torricelli);
		persistor.store(pascal);

		Person torricelli2 = persistor.fetch(Person.class, 1);
		try {
			@SuppressWarnings("unused")
			Student temp = (Student) torricelli2;
			fail("Unexpected ability to cast instance to subclass");
		} catch (ClassCastException e) {
			// it is fine. Torricelli is not a student.
		}
		assertPerson(torricelli, torricelli2);

		Student pascal2 = persistor.fetch(Student.class, "chemist1");
		assertStudent(pascal, pascal2);

		// Try to get student instance as a person using person id
		Student pascal3 = (Student) persistor.fetch(Person.class, 2);
		assertStudent(pascal, pascal3);
	}

	private void assertStudent(Student expected, Student actual) {
		if (expected == null) {
			assertNull(actual);
			return;
		}
		assertPerson(expected, actual);
		assertEquals(expected.getStudentId(), actual.getStudentId());
		assertEquals(expected.getCollegeName(), actual.getCollegeName());
	}

	private void assertPerson(Person expected, Person actual) {
		if (expected == null) {
			assertNull(actual);
			return;
		}
		assertNotNull(actual);
		assertEquals(expected.getId(), actual.getId());
		assertEquals(expected.getName(), actual.getName());
		assertEquals(expected.getLastName(), actual.getLastName());
	}


	protected FieldMetadata<Person, Object, Object>[] makeFirstPk(String columnPrefix, FieldMetadata<Person, Object, Object>[] fields) {
		fields[0].setKey(true);
		return fields;
	}
	
	
	@SafeVarargs
	protected final <E> FieldMetadata<E, Object, Object>[] prefix(String columnPrefix, FieldMetadata<E, Object, Object>... fields) {
		if (columnPrefix == null) {
			return fields;
		}
		for (FieldMetadata<E, Object, Object> field : fields) {
			String column = field.getColumn();
			if (column != null) {
				field.setColumn(columnPrefix + column);
			}
		}
		return fields;
	}
	
	protected FieldMetadata<Person, Object, Object>[] getPersonFields() {
		@SuppressWarnings("unchecked")
		FieldMetadata<Person, Object, Object>[] personFields = new FieldMetadata[] { 
				helper.createFieldMetadata(Person.class, int.class, "id", "id", false),
				helper.createFieldMetadata(Person.class, String.class, "name", "first_name", false),
				helper.createFieldMetadata(Person.class, String.class, "lastName", "last_name", false), };

		return personFields;
	}

	protected FieldMetadata<Student, Object, Object>[] getStudentFields() {
		@SuppressWarnings("unchecked")
		FieldMetadata<Student, Object, Object>[] studentFields = new FieldMetadata[] { 
				helper.createFieldMetadata(Student.class, String.class, "studentId", "id", false),
				helper.createFieldMetadata(Student.class, String.class, "collegeName", "college", false), };

		return studentFields;
	}

	protected FieldMetadata<Employee, Object, Object>[] getEmployeeFields() {
		@SuppressWarnings("unchecked")
		FieldMetadata<Employee, Object, Object>[] studentFields = new FieldMetadata[] { 
				helper.createFieldMetadata(Employee.class, String.class, "employeeId", "id", false),
				helper.createFieldMetadata(Employee.class, String.class, "companyName", "company", false), };

		return studentFields;
	}

	protected <E> FieldMetadata<E, String, String> createDiscriminator(Class<E> clazz, String discriminatorName) {
		FieldMetadata<E, String, String> discriminator = helper.createFieldMetadata(clazz, String.class, null,
				discriminatorName, false);
		discriminator.setDiscriminator(true);
		Access<E, String, AccessibleObject> discriminatorAccess = new ClassNameAccess<>();
		discriminator.setAccessor(new PropertyAccessor<E, String>(clazz, String.class, discriminatorName, null, null,
				null, discriminatorAccess, discriminatorAccess));
		discriminator.setColumnType(String.class);
		discriminator.setDefaultValue(clazz.getName());

		return discriminator;
	}

	protected <E> String createDiscriminatorName(String tableName) {
		String discriminatorName = tableName + "." + "discriminator";
		return discriminatorName;
	}


	@SafeVarargs
	protected final <E> EntityMetadata<E> createEntityMetadata(Class<E> c, String schemaName, String tableName,
			FieldMetadata<E, ?, ?> pk, FieldMetadata<?, ?, ?>[] fields, FieldMetadata<E, ?, ?>... additinalFields) {
		FieldMetadata<?, ?, ?>[] allFields = ObjectArrays.concat(fields, additinalFields, FieldMetadata.class);
		EntityMetadata<E> emd = helper.createMetadata(c, schemaName, tableName, pk, allFields);
		doReturn(emd).when(helper.ctx).getEntityMetadata(c);
		return emd;
	}

	
	@SafeVarargs
	protected final <E> EntityMetadata<E> createEntityMetadata(Class<E> c, String schemaName, String tableName,
			FieldMetadata<?, ?, ?>[] fields, FieldMetadata<E, ?, ?>... additinalFields) {
		@SuppressWarnings("unchecked")
		FieldMetadata<E, ?, ?> pk = (FieldMetadata<E, ?, ?>)fields[0];
		pk.setKey(true);
		
		return createEntityMetadata(c, schemaName, tableName, pk, fields, additinalFields);
	}
	
}
