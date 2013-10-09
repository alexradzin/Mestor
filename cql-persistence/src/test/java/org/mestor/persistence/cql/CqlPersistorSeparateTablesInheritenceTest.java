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

import org.mestor.entities.Employee;
import org.mestor.entities.Person;
import org.mestor.entities.Student;
import org.mestor.metadata.EntityMetadata;
import org.mestor.metadata.FieldMetadata;

import com.google.common.collect.ObjectArrays;

public class CqlPersistorSeparateTablesInheritenceTest extends CqlPersistorInheritanceTestCase {
	private final static String PEOPLE_TABLE = "People";
	private final static String STUDENTS_TABLE = "Students";
	private final static String EMPLOYEES_TABLE = "Employees";

	@Override
	protected void createTables() {
		EntityMetadata<Person> personMeta = createEntityMetadata(Person.class, schemaName, PEOPLE_TABLE, prefix("people_", getPersonFields())/*, createDiscriminator(Person.class, discriminatorName)*/);
		
		
		EntityMetadata<Student> studentMeta = createEntityMetadata(Student.class, schemaName, STUDENTS_TABLE, 
				ObjectArrays.concat(prefix("student_", getStudentFields()), prefix("people_", getPersonFields()), FieldMetadata.class));
		
		EntityMetadata<Employee> employeeMeta = createEntityMetadata(Employee.class, schemaName, EMPLOYEES_TABLE, 
				ObjectArrays.concat(prefix("employee_", getEmployeeFields()), prefix("people_", getPersonFields()), FieldMetadata.class));
		
		doReturn(Arrays.asList(personMeta, studentMeta, employeeMeta)).when(helper.ctx).getEntityMetadata();
		
		
		helper.testEditTable(personMeta, null, true);
		helper.testEditTable(studentMeta, null, true, "people_id");
		helper.testEditTable(employeeMeta, null, true, "people_id");
	}

	
	@Override
	protected String[] getExpectedTables() {
		return new String[] {PEOPLE_TABLE, STUDENTS_TABLE, EMPLOYEES_TABLE};
	}
	

}
