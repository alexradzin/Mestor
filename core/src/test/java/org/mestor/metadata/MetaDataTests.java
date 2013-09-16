package org.mestor.metadata;
import java.util.LinkedHashMap;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mestor.testEntities.Person;

public class MetaDataTests {
	private boolean testDefTableName = false;
	EntityMetadata<Person> emd;
	private final String TABLE_NAME = "PeopleTable";
	private final String AGE_COLUMN = "age";
	private final String NAME_COLUMN = "full_name";
	private final String SCHEME_NAME = "test-scheme";

	@Before
	public void init() throws NoSuchMethodException, SecurityException{

		
		
		emd = new EntityMetadata<>(Person.class);
		emd.setEntityName(Person.class.getSimpleName());
		if(!testDefTableName){
			emd.setTableName(TABLE_NAME);
		}
		emd.setSchemaName(SCHEME_NAME);

		//set field metadata per property 
		FieldMetadata<Person, String> nameField = new FieldMetadata<>(Person.class, String.class, "name");		
		nameField.setColumn(NAME_COLUMN);
		nameField.setKey(true);
		nameField.setNullable(false);
		nameField.setLazy(false);
		
		FieldMetadata<Person, Integer> ageField = new FieldMetadata<>(Person.class, Integer.class, "age");
		ageField.setColumn(AGE_COLUMN);
		ageField.setNullable(true);
		
		
		
		Map<String, FieldMetadata<Person, Object>> fields = new LinkedHashMap<>();
		for (FieldMetadata<Person, Object> field : new FieldMetadata[] {nameField, ageField}) {
			fields.put(field.getName(), field);
		}
		System.out.println("before set fields");
		emd.setFields(fields);

	}

	
	@Test
	public void testTableMd(){
		Assert.assertEquals(Person.class.getSimpleName(), emd.getEntityName());
		Assert.assertEquals(TABLE_NAME, emd.getTableName());
		Assert.assertEquals(SCHEME_NAME, emd.getSchemaName());
		
	}

	@Test
	public void testColumnsMd(){
		Assert.assertEquals(emd.getField("age").getColumn() ,AGE_COLUMN);
		Assert.assertEquals(emd.getField("age").isKey() ,false);
		Assert.assertEquals(emd.getField("age").isLazy(),false);
		Assert.assertEquals(emd.getField("age").isNullable(),true);
		
		
		
		Assert.assertEquals(emd.getField("name").getColumn() ,NAME_COLUMN);
		Assert.assertEquals(emd.getField("name").isKey(),true);
		Assert.assertEquals(emd.getField("name").isLazy(),false);
		Assert.assertEquals(emd.getField("name").isNullable(),false);
		
	}
	
	
	@Test
	public void testDefaultTableName(){
		if(testDefTableName){
			System.out.println("BBBB "+emd.getTableName());
			Assert.assertEquals(Person.class.getSimpleName(), emd.getTableName());
		}

	}

}
