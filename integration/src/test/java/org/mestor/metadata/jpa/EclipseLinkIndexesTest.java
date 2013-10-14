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
package org.mestor.metadata.jpa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Map;

import org.junit.Test;
import org.mestor.entities.annotated.AutoNamedIndexPerson;
import org.mestor.entities.annotated.DuplicateNamesIndexedPerson;
import org.mestor.entities.annotated.FieldIndexedPerson;
import org.mestor.entities.annotated.IndexColumnCollisionPerson;
import org.mestor.entities.annotated.IndexedPerson;
import org.mestor.entities.annotated.MethodIndexPerson;
import org.mestor.entities.annotated.SameColumnTwiceInIndexPerson;
import org.mestor.metadata.exceptions.DuplicateIndexName;

public class EclipseLinkIndexesTest {

	@Test
	public void testIndexes(){
		Map<String, String[]> expected = MetadataFactoryTestUtils.buildStringToStringArrayMap(
				"name", new String[] { "name" },
				"age", new String[] { "age" },
				"full_name", new String[] { "name", "lastName" });
		MetadataFactoryTestUtils.testIndexes(IndexedPerson.class, expected);
	}
	
	@Test
	public void testDuplicateNamesIndexes(){
		try{
			MetadataFactoryTestUtils.testIndexes(DuplicateNamesIndexedPerson.class, null);
		}catch(DuplicateIndexName e){
			assertEquals("name", e.getIndexName());
			return;
		}
		fail("No exception caught");
	}
	
	@Test
	public void testFieldLevelIndexes(){
		Map<String, String[]> expected = MetadataFactoryTestUtils.buildStringToStringArrayMap(
				"simple", new String[] { "simpleIndex" },
				"complex", new String[] { "complexIndex1", "complexIndex2"});
		MetadataFactoryTestUtils.testIndexes(FieldIndexedPerson.class, expected);
	}
	
	@Test
	public void testSameColumnTwiceInIndex(){
		Map<String, String[]> expected = MetadataFactoryTestUtils.buildStringToStringArrayMap(
				"name", new String[] { "name" });
		MetadataFactoryTestUtils.testIndexes(SameColumnTwiceInIndexPerson.class, expected);
	}
	
	@Test
	public void testIndexColumnCollision(){
		Map<String, String[]> expected = MetadataFactoryTestUtils.buildStringToStringArrayMap(
				"idx", new String[] { "indexColumn" });
		MetadataFactoryTestUtils.testIndexes(IndexColumnCollisionPerson.class, expected);
	}
	
	@Test
	public void testAutoNamedIndex(){
		Map<String, String[]> expected = MetadataFactoryTestUtils.buildStringToStringArrayMap(
				"auto_named_index", new String[] { "autoNamedIndex" });
		MetadataFactoryTestUtils.testIndexes(AutoNamedIndexPerson.class, expected);
	}
	
	@Test
	public void testMethodIndex(){
		Map<String, String[]> expected = MetadataFactoryTestUtils.buildStringToStringArrayMap(
				"index", new String[] { "column" },
				"index2", new String[] { "index2Col1", "index2Col2" });
		MetadataFactoryTestUtils.testIndexes(MethodIndexPerson.class, expected);
	}
}
