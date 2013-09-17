package org.mestor.metadata;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
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
	private final String DEF_INDEX_NAME_1 = "idx1";
	private final String DEF_INDEX_NAME_2 = "idx2";
	

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
		
		//for primary key tests
		emd.setPrimaryKey(nameField);
		
		
		//test index meta data
		List<IndexMetadata<Person>> indexes = new ArrayList<IndexMetadata<Person>>();
		IndexMetadata<Person> index = new IndexMetadata<>(Person.class, DEF_INDEX_NAME_1, nameField);
		IndexMetadata<Person> index2 = new IndexMetadata<>(Person.class, DEF_INDEX_NAME_2, nameField);
		indexes.add(index);
		indexes.add(index2);
		emd.setIndexes(indexes);

		
		Map<String, FieldMetadata<Person, Object>> fields = new LinkedHashMap<>();
		for (FieldMetadata<Person, Object> field : new FieldMetadata[] {nameField, ageField}) {
			fields.put(field.getName(), field);
		}
		System.out.println("before set fields");
		emd.setFields(fields);

	}

	
	@Test
	//will tests table level related metadata
	public void testTableMd(){
		Assert.assertEquals(Person.class.getSimpleName(), emd.getEntityName());
		Assert.assertEquals(TABLE_NAME, emd.getTableName());
		Assert.assertEquals(SCHEME_NAME, emd.getSchemaName());
		
		//test primary key
		FieldMetadata<Person,? extends Object> pkField = emd.getPrimaryKey();
		Assert.assertEquals(NAME_COLUMN, pkField.getColumn());
		
		//test index 
		emd.getIndexes();
		Assert.assertEquals(2, emd.getIndexes().size());
		IndexMetadata<Person> temp =  (IndexMetadata<Person>)emd.getIndexes().toArray()[0];
		FieldMetadata<Person,? extends Object>[] fmd = temp.getField();
		
		//Assert.assertEquals(NAME_COLUMN, temp.getFields()		
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
