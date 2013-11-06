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
		final Map<String, String[]> expected = MetadataFactoryTestUtils.buildStringToStringArrayMap(
				"index1", new String[] { "column1" });
		MetadataFactoryTestUtils.testIndexes(getOneColumnIndexClass(), expected);
	}

	@Test
	public void testTwoColumnIndex() {
		final Map<String, String[]> expected = MetadataFactoryTestUtils.buildStringToStringArrayMap(
				"index1", new String[] { "column1", "column2" });
		MetadataFactoryTestUtils.testIndexes(getTwoColumnIndexClass(), expected);
	}

	@Test
	public void testThreeGoodIndexes() {
		final Map<String, String[]> expected = MetadataFactoryTestUtils.buildStringToStringArrayMap(
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
			}catch(final DuplicateIndexName e){
				assertEquals("index1", e.getIndexName());
				return;
			}
			fail("No exception caught");
		}
	}

	@Test
	public void testSameColumnTwiceInIndex() {
		final Map<String, String[]> expected = MetadataFactoryTestUtils.buildStringToStringArrayMap(
				"index1", new String[] { "column1" });
		MetadataFactoryTestUtils.testIndexes(getSameColumnTwiceInIndexClass(), expected);
	}

	@Test
	public void testSpacesInNames(){
		//????
		//actually dont'n know expected behavior here
	}




	@Test
	public void testAutoNamedSingleColumnIndex() {
		final Map<String, String[]> expected = MetadataFactoryTestUtils.buildStringToStringArrayMap(
				"column1", new String[] { "column1" });
		MetadataFactoryTestUtils.testIndexes(getAutoNamedSingleColumnIndex(), expected);
	}


	@Test
	public void testAutoNamedMultipleColumnIndex() {
		final Map<String, String[]> expected = MetadataFactoryTestUtils.buildStringToStringArrayMap(
				"column1_column2_column3", new String[] { "column1", "column2", "column3" });
		MetadataFactoryTestUtils.testIndexes(getAutoNamedMultipleColumnIndex(), expected);
	}


	@Test(expected = IllegalArgumentException.class)
	public void testIndexNameWithSpace() {
		final Map<String, String[]> expected = MetadataFactoryTestUtils.buildStringToStringArrayMap(
				null, new String[] { "column1"});
		MetadataFactoryTestUtils.testIndexes(getIndexNameWithSpace(), expected);
	}




	protected abstract Class<?> getOneColumnIndexClass();
	protected abstract Class<?> getTwoColumnIndexClass();
	protected abstract Class<?> getThreeGoodIndexesClass();
	protected abstract Class<?> getDuplicateIndexNameClass();
	protected abstract Class<?> getSameColumnTwiceInIndexClass();
	protected abstract Class<?> getAutoNamedSingleColumnIndex();
	protected abstract Class<?> getAutoNamedMultipleColumnIndex();
	protected abstract Class<?> getIndexNameWithSpace();
}