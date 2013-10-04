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

import static org.mockito.Mockito.doReturn;

import java.util.Arrays;

import org.mestor.metadata.EntityMetadata;
import org.mestor.metadata.FieldMetadata;
import org.mestor.testEntities.Employee;
import org.mestor.testEntities.Person;
import org.mestor.testEntities.Student;

import com.google.common.collect.ObjectArrays;

public class CqlPersistorSingleTableInheritenceTest extends CqlPersistorInheritanceTestCase {
	private final static String PEOPLE_TABLE = "People";

	@Override
	protected void createTables() {
		@SuppressWarnings("unchecked")
		FieldMetadata<Person, ?, ?>[] personFields = ObjectArrays.concat(ObjectArrays.concat(
				prefix("people_", getPersonFields()), 
				prefix("student_", getStudentFields()), FieldMetadata.class), 
				prefix("employee_", getEmployeeFields()), FieldMetadata.class);
		
		final String discriminatorName = createDiscriminatorName(PEOPLE_TABLE);
		
		EntityMetadata<Person> personMeta = createEntityMetadata(Person.class, schemaName, PEOPLE_TABLE, personFields, createDiscriminator(Person.class, discriminatorName));
		
		
		@SuppressWarnings("unchecked")
		FieldMetadata<Person, ?, ?>[] studentFields = ObjectArrays.concat(
				prefix("student_", getStudentFields()), 
				prefix("people_", getPersonFields()), FieldMetadata.class);		
		EntityMetadata<Student> studentMeta = createEntityMetadata(Student.class, schemaName, PEOPLE_TABLE, studentFields, createDiscriminator(Student.class, null));

		
		
		@SuppressWarnings("unchecked")
		FieldMetadata<Person, ?, ?>[] employeeFields = ObjectArrays.concat(
				prefix("employee_", getEmployeeFields()), 
				prefix("people_", getPersonFields()), FieldMetadata.class);		
		EntityMetadata<Employee> employeeMeta = createEntityMetadata(Employee.class, schemaName, PEOPLE_TABLE, employeeFields, createDiscriminator(Employee.class, null));
		

		doReturn(Arrays.asList(personMeta, studentMeta, employeeMeta)).when(helper.ctx).getEntityMetadata();
		helper.testEditTable(personMeta, null, true, discriminatorName, "student_id", "employee_id");
	}

	@Override
	protected String[] getExpectedTables() {
		return new String[] {PEOPLE_TABLE};
	}
}
