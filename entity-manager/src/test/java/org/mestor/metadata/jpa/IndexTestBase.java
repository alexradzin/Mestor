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
import org.mestor.metadata.exceptions.DuplicateIndexName;

public abstract class IndexTestBase {

	public IndexTestBase() {
		super();
	}

	@Test
	public void testOneColumnIndex() {
		Map<String, String[]> expected = MetadataFactoryTestUtils.buildStringToStringArrayMap(
				"index1", new String[] { "column1" });
		MetadataFactoryTestUtils.testIndexes(getOneColumnIndexClass(), expected);
	}
	
	@Test
	public void testTwoColumnIndex() {
		Map<String, String[]> expected = MetadataFactoryTestUtils.buildStringToStringArrayMap(
				"index1", new String[] { "column1", "column2" });
		MetadataFactoryTestUtils.testIndexes(getTwoColumnIndexClass(), expected);
	}
	
	@Test
	public void testThreeGoodIndexes() {
		Map<String, String[]> expected = MetadataFactoryTestUtils.buildStringToStringArrayMap(
				"index1", new String[] { "column1" },
				"index2", new String[] { "column2" },
				"index3", new String[] { "column3", "column4" });
		MetadataFactoryTestUtils.testIndexes(getThreeGoodIndexesClass(), expected);
	}

	@Test
	public void testDuplicateIndexName() {
		if(getDuplicateIndexNameClass() != null){
			try{
				MetadataFactoryTestUtils.testIndexes(getDuplicateIndexNameClass(), null);
			}catch(DuplicateIndexName e){
				assertEquals("index1", e.getIndexName());
				return;
			}
			fail("No exception caught");
		}
	}

	@Test
	public void testSameColumnTwiceInIndex() {
		Map<String, String[]> expected = MetadataFactoryTestUtils.buildStringToStringArrayMap(
				"index1", new String[] { "column1" });
		MetadataFactoryTestUtils.testIndexes(getSameColumnTwiceInIndexClass(), expected);
	}
	
	@Test 
	public void testSpacesInNames(){
		//????
		//actually dont'n know expected behavior here
	}
	
	@Test
	public void testOneColumnFieldLevelIndex() {
		Map<String, String[]> expected = MetadataFactoryTestUtils.buildStringToStringArrayMap(
				"field_index1", new String[] { "column1" });
		MetadataFactoryTestUtils.testIndexes(getOneColumnFieldLevelIndexClass(), expected);
	}
	
	@Test
	public void testTwoColumnFieldLevelIndex() {
		Map<String, String[]> expected = MetadataFactoryTestUtils.buildStringToStringArrayMap(
				"field_index1", new String[] { "column1", "column2" });
		MetadataFactoryTestUtils.testIndexes(getTwoColumnFieldLevelIndexClass(), expected);
	}
	
	@Test
	public void testThreeGoodFieldLevelIndexes() {
		Map<String, String[]> expected = MetadataFactoryTestUtils.buildStringToStringArrayMap(
				"field_index1", new String[] { "column1" },
				"field_index2", new String[] { "column2" },
				"field_index3", new String[] { "column3", "column4" });
		MetadataFactoryTestUtils.testIndexes(getThreeGoodFieldLevelIndexesClass(), expected);
	}

	@Test
	public void testFieldLevelIndexColumnCollision() {
		Map<String, String[]> expected = MetadataFactoryTestUtils.buildStringToStringArrayMap(
				"field_index1", new String[] { "column1" });
		MetadataFactoryTestUtils.testIndexes(getFieldLevelIndexColumnCollisionClass(), expected);
	}

	@Test
	public void testAutoNamedFieldLevelIndex() {
		Map<String, String[]> expected = MetadataFactoryTestUtils.buildStringToStringArrayMap(
				"field_column1", new String[] { "field_column1" });
		MetadataFactoryTestUtils.testIndexes(getAutoNamedFieldLevelIndexClass(), expected);
	}

	@Test
	public void testOneColumnMethodLevelIndex() {
		Map<String, String[]> expected = MetadataFactoryTestUtils.buildStringToStringArrayMap(
				"method_index1", new String[] { "column1" });
		MetadataFactoryTestUtils.testIndexes(getOneColumnMethodLevelIndexClass(), expected);
	}
	
	@Test
	public void testTwoColumnMethodLevelIndex() {
		Map<String, String[]> expected = MetadataFactoryTestUtils.buildStringToStringArrayMap(
				"method_index1", new String[] { "column1", "column2" });
		MetadataFactoryTestUtils.testIndexes(getTwoColumnMethodLevelIndexClass(), expected);
	}
	
	@Test
	public void testThreeGoodMethodLevelIndexes() {
		Map<String, String[]> expected = MetadataFactoryTestUtils.buildStringToStringArrayMap(
				"method_index1", new String[] { "column1" },
				"method_index2", new String[] { "column2" },
				"method_index3", new String[] { "column3", "column4" });
		MetadataFactoryTestUtils.testIndexes(getThreeGoodMethodLevelIndexesClass(), expected);
	}

	@Test
	public void testMethodLevelIndexColumnCollision() {
		Map<String, String[]> expected = MetadataFactoryTestUtils.buildStringToStringArrayMap(
				"method_index1", new String[] { "column1" });
		MetadataFactoryTestUtils.testIndexes(getMethodLevelIndexColumnCollisionClass(), expected);
	}

	@Test
	public void testAutoNamedMethodLevelIndex() {
		Map<String, String[]> expected = MetadataFactoryTestUtils.buildStringToStringArrayMap(
				"method_column1", new String[] { "method_column1" });
		MetadataFactoryTestUtils.testIndexes(getAutoNamedMethodLevelIndexClass(), expected);
	}
	
	
	protected Class<?> getOneColumnIndexClass() {
		return null;
	}

	protected Class<?> getTwoColumnIndexClass() {
		return null;
	}

	protected Class<?> getThreeGoodIndexesClass() {
		return null;
	}

	protected Class<?> getDuplicateIndexNameClass() {
		return null;
	}

	protected Class<?> getSameColumnTwiceInIndexClass() {
		return null;
	}

	protected Class<?> getFieldLevelIndexColumnCollisionClass() {
		return null;
	}
	
	protected Class<?> getAutoNamedFieldLevelIndexClass(){
		return null;
	}

	protected Class<?> getAutoNamedMethodLevelIndexClass() {
		return null;
	}

	protected Class<?> getOneColumnFieldLevelIndexClass() {
		return null;
	}

	protected Class<?> getTwoColumnFieldLevelIndexClass() {
		return null;
	}
	
	protected Class<?> getMethodLevelIndexColumnCollisionClass() {
		return null;
	}
	
	protected Class<?> getThreeGoodMethodLevelIndexesClass(){
		return null;
	}
	
	protected Class<?> getThreeGoodFieldLevelIndexesClass(){
		return null;
	}
	
	protected Class<?> getTwoColumnMethodLevelIndexClass(){
		return null;
	}
	
	protected Class<?> getOneColumnMethodLevelIndexClass(){
		return null;
	}

}