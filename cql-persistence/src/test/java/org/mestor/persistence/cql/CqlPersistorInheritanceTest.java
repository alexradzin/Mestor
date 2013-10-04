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
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doReturn;

import java.io.IOException;
import java.lang.reflect.AccessibleObject;
import java.util.Arrays;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mestor.cassandra.CassandraAwareTestRunner;
import org.mestor.context.Persistor;
import org.mestor.metadata.EntityMetadata;
import org.mestor.metadata.FieldMetadata;
import org.mestor.reflection.Access;
import org.mestor.reflection.ClassNameAccess;
import org.mestor.reflection.PropertyAccessor;
import org.mestor.testEntities.Employee;
import org.mestor.testEntities.Person;
import org.mestor.testEntities.Person.Gender;
import org.mestor.testEntities.Student;

import com.google.common.collect.ObjectArrays;

/**
 * This test case is replaced by {@link CqlPersistorInheritanceTestCase} and its subclasses
 * and should be removed.
 * 
 * @author alexr
 *
 */
@RunWith(CassandraAwareTestRunner.class)
@Ignore
@Deprecated
public class CqlPersistorInheritanceTest {
	private final Persistor persistor;
	private final CqlPersistorTestHelper helper;


	private final static String PASCAL_STUDENT_ID = "chemist1";
	
	
	public CqlPersistorInheritanceTest() throws IOException {
		helper = new CqlPersistorTestHelper();
		persistor = helper.getPersistor();
		doReturn(helper.getPersistor()).when(helper.ctx).getPersistor();
	}

	
	@Test
	public void testCrudTablePerClass() {
		final String schemaName = "test1";
		try {
			createTablePerClass(schemaName);
			testStoreAndFetchHierarchy();
		} finally {
			persistor.dropSchema(schemaName);
		}
	}
	
	@Test
	public void testCrudSingleTable() {
		final String schemaName = "test1";
		try {
			createSingleTable(schemaName);
			testStoreAndFetchHierarchy();
			// Try to get student instance as a person using person id 
			Student pascal3 = (Student)persistor.fetch(Person.class, 2);
			assertNotNull(pascal3);
		} finally {
			persistor.dropSchema(schemaName);
		}
	}
	
	
	@Test
	public void testCrudJoinedTable() {
		final String schemaName = "test1";
		try {
			createJoinedTable(schemaName);
			testStoreAndFetchHierarchy();
			// Try to get student instance as a person using person id 
			Student pascal3 = (Student)persistor.fetch(Person.class, 2);
			assertNotNull(pascal3);
			assertEquals("Pascal", pascal3.getLastName());
			assertEquals(PASCAL_STUDENT_ID, pascal3.getStudentId());
		} finally {
			persistor.dropSchema(schemaName);
		}
	}
	

	private void createTablePerClass(final String schemaName) {
		helper.testCreateSchema(schemaName, null, false);
		FieldMetadata<Person, Integer, Integer> pk = helper.createFieldMetadata(Person.class, int.class, "id", "identifier", true);
		
		@SuppressWarnings("unchecked")
		FieldMetadata<Person, Object, Object>[] personFields = new FieldMetadata[] {
				pk,
				helper.createFieldMetadata(Person.class, String.class, "name", "first_name", false),
				helper.createFieldMetadata(Person.class, String.class, "lastName", "last_name", false),
		};

		
		FieldMetadata<Student, String, String> studentId = helper.createFieldMetadata(Student.class, String.class, "studentId", "student_id", true);
		
		@SuppressWarnings("unchecked")
		FieldMetadata<Person, Object, Object>[] studentFields = new FieldMetadata[] {
				studentId,
				helper.createFieldMetadata(Student.class, String.class, "collegeName", "college", false),
		};

		
		EntityMetadata<Person> personMeta = helper.createMetadata(Person.class, schemaName, "People", pk, personFields);
		EntityMetadata<Student> studentMeta = helper.createMetadata(Student.class, schemaName, "Students", studentId, ObjectArrays.concat(personFields, studentFields, FieldMetadata.class));
		
		
		for (EntityMetadata<?> emd : new EntityMetadata[] {personMeta, studentMeta}) {
			helper.testEditTable(emd, null, true);
			doReturn(emd).when(helper.ctx).getEntityMetadata(emd.getEntityType());
		}
	}


	
	private void createJoinedTable(final String schemaName) {
		final String DISCRIMINATOR_FIELD = "discriminator";
		final String TABLE_NAME = "People";
		
		
		helper.testCreateSchema(schemaName, null, false);
		FieldMetadata<Person, Integer, Integer> pk = helper.createFieldMetadata(Person.class, int.class, "id", "identifier", true);
		
		FieldMetadata<Person, String, String> discriminator = helper.createFieldMetadata(Person.class, String.class, DISCRIMINATOR_FIELD, DISCRIMINATOR_FIELD, false);
		discriminator.setDiscriminator(true);
		Access<Person, String, AccessibleObject> discriminatorAccess = new ClassNameAccess<>(); 
		discriminator.setAccessor(new PropertyAccessor<Person, String>(Person.class, String.class, DISCRIMINATOR_FIELD, null, null, null, discriminatorAccess, discriminatorAccess));
		discriminator.setColumnType(String.class);
		
		@SuppressWarnings("unchecked")
		FieldMetadata<Person, Object, Object>[] personFields = new FieldMetadata[] {
				pk,
				helper.createFieldMetadata(Person.class, String.class, "name", "first_name", false),
				helper.createFieldMetadata(Person.class, String.class, "lastName", "last_name", false),
		};

		
		//FK for sub-classes
		FieldMetadata<Person, Integer, Integer> personRef = helper.createFieldMetadata(Person.class, Integer.class, "id", "person_id", false);
		personRef.setColumnType(Integer.class);
		personRef.setJoiner(true);
		
		
		FieldMetadata<Student, String, String> studentId = helper.createFieldMetadata(Student.class, String.class, "studentId", "student_id", true);
		@SuppressWarnings("unchecked")
		FieldMetadata<Person, Object, Object>[] studentFields = new FieldMetadata[] {
				personRef,
				studentId,
				helper.createFieldMetadata(Student.class, String.class, "collegeName", "college", false),
				// fields inherited from Person
				helper.createFieldMetadata(Student.class, String.class, "name", null, false),
				helper.createFieldMetadata(Student.class, String.class, "lastName", null, false),
				helper.createFieldMetadata(Student.class, int.class, "id", null, false),
		};


		FieldMetadata<Employee, Integer, Integer> employeeId = helper.createFieldMetadata(Employee.class, int.class, "employeeId", "employee_id", true);
		@SuppressWarnings("unchecked")
		FieldMetadata<Person, Object, Object>[] employeeFields = new FieldMetadata[] {
				personRef,
				employeeId,
				helper.createFieldMetadata(Employee.class, String.class, "companyName", "company", false),
				// fields inherited from Person
				helper.createFieldMetadata(Employee.class, String.class, "name", null, false),
				helper.createFieldMetadata(Employee.class, String.class, "lastName", null, false),
				helper.createFieldMetadata(Employee.class, int.class, "id", null, false),
		};
		
		
		EntityMetadata<Person> personMeta = createEntityMetadata(Person.class, schemaName, TABLE_NAME, pk, personFields, DISCRIMINATOR_FIELD, DISCRIMINATOR_FIELD);
//		EntityMetadata<Student> studentMeta = helper.createMetadata(Student.class, schemaName, "Students", studentId, studentFields);
//		EntityMetadata<Employee> employeeMeta = helper.createMetadata(Employee.class, schemaName, "Employees", employeeId, employeeFields);
		
		EntityMetadata<Student> studentMeta = createEntityMetadata(Student.class, schemaName, "Students", studentId, studentFields, DISCRIMINATOR_FIELD, null);
		EntityMetadata<Employee> employeeMeta = createEntityMetadata(Employee.class, schemaName, "Employees", employeeId, employeeFields, DISCRIMINATOR_FIELD, null);
		
		
		helper.testEditTable(personMeta, null, true, DISCRIMINATOR_FIELD);
		
		
		for (EntityMetadata<?> emd : new EntityMetadata[] {studentMeta, employeeMeta}) {
			helper.testEditTable(emd, null, true);
			doReturn(emd).when(helper.ctx).getEntityMetadata(emd.getEntityType());
		}
		
		doReturn(Arrays.asList(personMeta, studentMeta, employeeMeta)).when(helper.ctx).getEntityMetadata();
		
	}

	
	private void createSingleTable(final String schemaName) {
		final String DISCRIMINATOR_FIELD = "discriminator";
		final String TABLE_NAME = "People";
		
		
		
		helper.testCreateSchema(schemaName, null, false);
		FieldMetadata<Person, Integer, Integer> personId = helper.createFieldMetadata(Person.class, int.class, "id", "identifier", true);
		
		FieldMetadata<Student, String, String> studentId = helper.createFieldMetadata(Student.class, String.class, "studentId", "student_id", false);
		FieldMetadata<Employee, Integer, Integer> employeeId = helper.createFieldMetadata(Employee.class, int.class, "employeeId", "employee_id", false);
		
		FieldMetadata<Person, String, String> discriminator = helper.createFieldMetadata(Person.class, String.class, DISCRIMINATOR_FIELD, DISCRIMINATOR_FIELD, false);
		discriminator.setDiscriminator(true);
		Access<Person, String, AccessibleObject> discriminatorAccess = new ClassNameAccess<>(); 
		discriminator.setAccessor(new PropertyAccessor<Person, String>(Person.class, String.class, DISCRIMINATOR_FIELD, null, null, null, discriminatorAccess, discriminatorAccess));
		discriminator.setColumnType(String.class);
		
		@SuppressWarnings("unchecked")
		FieldMetadata<Person, ?, ?>[] fields = new FieldMetadata[] {
				personId,
				helper.createFieldMetadata(Person.class, String.class, "name", "first_name", false),
				helper.createFieldMetadata(Person.class, String.class, "lastName", "last_name", false),
				studentId,
				helper.createFieldMetadata(Student.class, String.class, "collegeName", "college", false),
				employeeId,
				helper.createFieldMetadata(Employee.class, String.class, "companyName", "company_name", false),
		};

		
		
		EntityMetadata<Person> personMeta = createEntityMetadata(Person.class, schemaName, TABLE_NAME, personId, fields, DISCRIMINATOR_FIELD, DISCRIMINATOR_FIELD);
		helper.testEditTable(personMeta, null, true, DISCRIMINATOR_FIELD);
		
		EntityMetadata<Student> studentMeta = createEntityMetadata(Student.class, schemaName, TABLE_NAME, studentId, fields, DISCRIMINATOR_FIELD, DISCRIMINATOR_FIELD);
		EntityMetadata<Employee> employeeMeta = createEntityMetadata(Employee.class, schemaName, TABLE_NAME, employeeId, fields, DISCRIMINATOR_FIELD, DISCRIMINATOR_FIELD);
		
		
		doReturn(Arrays.asList(personMeta, studentMeta, employeeMeta)).when(helper.ctx).getEntityMetadata();
		
	}
	
	
	

	private <E> EntityMetadata<E> createEntityMetadata(Class<E> c, String schemaName, String tableName, FieldMetadata<E, ?, ?> pk, FieldMetadata<Person, ?, ?>[] fields, String discriminatorField, String discriminatorColumn) {
		FieldMetadata<Person, String, String> discriminator = helper.createFieldMetadata(Person.class, String.class, discriminatorField, discriminatorColumn, false);
		discriminator.setDiscriminator(true);
		Access<Person, String, AccessibleObject> discriminatorAccess = new ClassNameAccess<>(); 
		discriminator.setAccessor(new PropertyAccessor<Person, String>(Person.class, String.class, discriminatorField, null, null, null, discriminatorAccess, discriminatorAccess));
		discriminator.setColumnType(String.class);
		discriminator.setDefaultValue(c.getName());
		
		FieldMetadata<Person, ?, ?>[] allFields = ObjectArrays.concat(fields, discriminator);
		EntityMetadata<E> emd = helper.createMetadata(c, schemaName, tableName, pk, allFields);
		doReturn(emd).when(helper.ctx).getEntityMetadata(c);
		return emd;
		
	}
	
	
	public void testStoreAndFetchHierarchy() {
		Person torricelli = new Person(1, "Evangelista", "Torricelli", Gender.MALE);
		final String PASCAL_COLLEGE = "Home"; // Pascal was educated by his father.
		Student pascal = new Student(2, "Blaise", "Pascal", Gender.MALE, PASCAL_STUDENT_ID, PASCAL_COLLEGE);
		
		persistor.store(torricelli);
		persistor.store(pascal);
		

		Person torricelli2 = persistor.fetch(Person.class, 1);
		Student pascal2 = persistor.fetch(Student.class, "chemist1");
		
		
		assertNotNull(torricelli2);
		assertNotNull(pascal2);
		
		try {
			@SuppressWarnings("unused")
			Student temp = (Student)torricelli2;
			fail("Unexpected ability to cast instance to subclass");
		} catch (ClassCastException e) {
			// it is fine. Torricelli is not a student.
		}
		
		assertEquals(PASCAL_STUDENT_ID, pascal2.getStudentId());
		assertEquals(PASCAL_COLLEGE, pascal2.getCollegeName());

		assertEquals("Blaise", pascal2.getName());
		assertEquals("Pascal", pascal2.getLastName());
		assertEquals(2, pascal2.getId());
	}
	
	
}
