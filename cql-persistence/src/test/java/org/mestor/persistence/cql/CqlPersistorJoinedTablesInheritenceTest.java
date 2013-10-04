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

public class CqlPersistorJoinedTablesInheritenceTest extends CqlPersistorInheritanceTestCase {
	private final static String PEOPLE_TABLE = "People";
	private final static String STUDENTS_TABLE = "Students";
	private final static String EMPLOYEES_TABLE = "Employees";

	@Override
	protected void createTables() {
		final String discriminatorName = createDiscriminatorName(PEOPLE_TABLE);
		
		EntityMetadata<Person> personMeta = createEntityMetadata(Person.class, schemaName, PEOPLE_TABLE, getPersonFields(), createDiscriminator(Person.class, discriminatorName));
		helper.testEditTable(personMeta, null, true, discriminatorName);
		

		EntityMetadata<Student> studentMeta = createEntityMetadata(Student.class, schemaName, STUDENTS_TABLE, getStudentFields(), createDiscriminator(Student.class, discriminatorName), createJoiner(Student.class));
		helper.testEditTable(studentMeta, null, true, "People.discriminator", "people_id");
		
		EntityMetadata<Employee> employeeMeta = createEntityMetadata(Employee.class, schemaName, EMPLOYEES_TABLE, getEmployeeFields(), createDiscriminator(Employee.class, discriminatorName), createJoiner(Employee.class));
		helper.testEditTable(employeeMeta, null, true, "People.discriminator", "people_id");
		
		doReturn(Arrays.asList(personMeta, studentMeta, employeeMeta)).when(helper.ctx).getEntityMetadata();
	}

	@Override
	protected String[] getExpectedTables() {
		return new String[] {PEOPLE_TABLE, STUDENTS_TABLE, EMPLOYEES_TABLE};
	}

	protected <E> FieldMetadata<E, ?, ?> createJoiner(Class<E> clazz) {
		Class<?> parent = clazz.getSuperclass();
		EntityMetadata<?> parentMeta = helper.ctx.getEntityMetadata(parent);
		FieldMetadata<?, ?, ?> parentPk = parentMeta.getPrimaryKey();
		String column = parentMeta.getTableName().toLowerCase() + "_" + parentPk.getName();
		FieldMetadata<E, ?, ?> joiner = helper.createFieldMetadata(clazz, parentPk.getType(), parentPk.getName(), column, false);
		joiner.setJoiner(true);
		return joiner;
	}
}
