package org.mestor.metadata;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mestor.testEntities.Person;

public class MetaDataTests {
	private boolean testDefTableName = false;
	private EntityMetadata<Person> emd;
	private final String TABLE_NAME = "PeopleTable";
	private final String AGE_COLUMN = "age";
	private final String AGE_FIELD = "age";
	
	private final String NAME_FIELD = "name";
	private final String NAME_COLUMN = "first_name";
	
	private final String SURENAME_COLUMN = "last_name";

	private final String SCHEME_NAME = "test-scheme";
	private final String DEF_INDEX_NAME_1 = "idx1";
	private final String DEF_INDEX_NAME_2 = "idx2";
	
	HashSet<String> predefinedIndexes = new HashSet<String>();
	

	@Before
	public void init() {
				
		emd = new EntityMetadata<>(Person.class);
		emd.setEntityName(Person.class.getSimpleName());
		
		if(!testDefTableName){
			emd.setTableName(TABLE_NAME);
		}
		emd.setSchemaName(SCHEME_NAME);

		//set field metadata per property 
		FieldMetadata<Person, String> nameField = new FieldMetadata<>(Person.class, String.class, NAME_FIELD);		
		nameField.setColumn(NAME_COLUMN);
		nameField.setKey(true);
		nameField.setNullable(false);
		nameField.setLazy(false);
		
		FieldMetadata<Person, Integer> ageField = new FieldMetadata<>(Person.class, Integer.class, "age");
		ageField.setColumn(AGE_COLUMN);
		ageField.setNullable(true);
		
		FieldMetadata<Person, String> surnameField = new FieldMetadata<>(Person.class, String.class, "surename");
		surnameField.setColumn(SURENAME_COLUMN);
		surnameField.setKey(false);
		surnameField.setNullable(false);
		
		//for primary key tests
		System.out.println("b "+nameField.getColumn());
		emd.setPrimaryKey(nameField);
		
		
		
		//test index meta data
		predefinedIndexes.add(SURENAME_COLUMN);
		predefinedIndexes.add(NAME_COLUMN);
		
		
		List<IndexMetadata<Person>> indexes = new ArrayList<IndexMetadata<Person>>();
		IndexMetadata<Person> index = new IndexMetadata<>(Person.class, DEF_INDEX_NAME_1, nameField);
		IndexMetadata<Person> index2 = new IndexMetadata<>(Person.class, DEF_INDEX_NAME_2, surnameField);
		indexes.add(index);
		indexes.add(index2);
		emd.setIndexes(indexes);


		
		Map<String, FieldMetadata<Person, Object>> fields = new LinkedHashMap<>();
		for (FieldMetadata<Person, Object> field : new FieldMetadata[] {nameField, ageField, surnameField}) {
			emd.addField(field);			
		}		
	}

	
	//test 2 primary keys not possible
	//test map 2 fields to same column name not possible
	
	@Test
	//will tests table level related metadata
	public void testTableMd() {
		assertEquals(Person.class.getSimpleName(), emd.getEntityName());
		assertEquals(TABLE_NAME, emd.getTableName());
		assertEquals(SCHEME_NAME, emd.getSchemaName());
		
		//test primary key
		FieldMetadata<Person,? extends Object> pkField = emd.getPrimaryKey();
		assertEquals(NAME_COLUMN, pkField.getColumn());
		
		//test index 
		emd.getIndexes();
		assertEquals(2, emd.getIndexes().size());
		@SuppressWarnings("unchecked")
		IndexMetadata<Person> temp =  (IndexMetadata<Person>)emd.getIndexes().toArray()[0];
		FieldMetadata<Person,? extends Object>[] fmd = temp.getField();
		assertNotNull(fmd);
		assertEquals(1, fmd.length);
		
		@SuppressWarnings("unchecked")
		FieldMetadata<Person, String> nameMd = (FieldMetadata<Person, String>)fmd[0];
		
		assertFieldMetadata(nameMd, NAME_FIELD, NAME_COLUMN, Person.class, String.class);
		
	}
	
	
	@Test
	public void testIndexes(){
		//test index 
		Assert.assertEquals(2, emd.getIndexes().size());
		Iterator itr = emd.getIndexes().iterator();
		while(itr.hasNext()) {
			IndexMetadata<Person> element = (IndexMetadata<Person>) itr.next();
			FieldMetadata<Person,? extends Object>[]  fmd = element.getField();			
			Assert.assertTrue(predefinedIndexes.contains(fmd[0].getColumn()));

		}
	}

	@Test
	public void testColumnsMd(){
		assertFieldMetadata(emd, AGE_FIELD, AGE_COLUMN, false, false, true);
		assertFieldMetadata(emd, NAME_FIELD, NAME_COLUMN, true, false, false);
	}

	

	private <E, F> void assertFieldMetadata(EntityMetadata<E> emd, String fieldName, String columnName, boolean key, boolean lazy, boolean nullable) {
		assertFieldMetadata(emd.getField(columnName), fieldName, columnName, key, lazy, nullable);
	}
	
	
	private <E, F> void assertFieldMetadata(FieldMetadata<E, F> fmd, String fieldName, String columnName, Class<E> entityType, Class<F> fieldType) {
		assertEquals(fieldName, fmd.getName());
		assertEquals(columnName, fmd.getColumn());
		assertEquals(entityType, fmd.getClassType());
		assertEquals(fieldType, fmd.getType());
	}
	
	private <E, F> void assertFieldMetadata(FieldMetadata<E, F> fmd, String fieldName, String columnName, boolean key, boolean lazy, boolean nullable) {
		System.out.println("55 "+fmd+"   "+fieldName);
		assertEquals(fieldName, fmd.getName());
		assertEquals(columnName, fmd.getColumn());
		assertEquals(key, fmd.isKey());
		assertEquals(lazy, fmd.isLazy());
		assertEquals(nullable, fmd.isNullable());
	}
	
	
	
	@Test
	public void testDefaultTableName(){
		if(testDefTableName){			
			Assert.assertEquals(Person.class.getSimpleName(), emd.getTableName());
		}
	}

}
