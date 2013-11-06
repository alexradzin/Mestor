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

import java.util.Map;

import org.junit.Test;
import org.mestor.entities.index_test.AutoNamedClassLevelMultipleFieldIndex;
import org.mestor.entities.index_test.AutoNamedClassLevelSingleFieldIndex;
import org.mestor.entities.index_test.AutoNamedFieldLevelIndex;
import org.mestor.entities.index_test.AutoNamedMethodLevelIndex;
import org.mestor.entities.index_test.DuplicateIndexName;
import org.mestor.entities.index_test.IndexColumnCollisionFieldLevel;
import org.mestor.entities.index_test.IndexColumnCollisionMethodLevel;
import org.mestor.entities.index_test.IndexNameWithSpace;
import org.mestor.entities.index_test.OneColumnFieldLevelIndex;
import org.mestor.entities.index_test.OneColumnIndex;
import org.mestor.entities.index_test.OneColumnMethodLevelIndex;
import org.mestor.entities.index_test.SameColumnTwiceInIndex;
import org.mestor.entities.index_test.ThreeGoodFieldLevelIndexes;
import org.mestor.entities.index_test.ThreeGoodIndexes;
import org.mestor.entities.index_test.ThreeGoodMethodLevelIndex;
import org.mestor.entities.index_test.TwoColumnFieldLevelIndex;
import org.mestor.entities.index_test.TwoColumnIndex;
import org.mestor.entities.index_test.TwoColumnMethodLevelIndex;

public class EclipseLinkIndexesTest extends IndexTestBase {

	@Override
	protected Class<?> getOneColumnIndexClass() {
		return OneColumnIndex.class;
	}

	@Override
	protected Class<?> getTwoColumnIndexClass() {
		return TwoColumnIndex.class;
	}

	@Override
	protected Class<?> getThreeGoodIndexesClass() {
		return ThreeGoodIndexes.class;
	}

	@Override
	protected Class<?> getDuplicateIndexNameClass() {
		return DuplicateIndexName.class;
	}

	@Override
	protected Class<?> getSameColumnTwiceInIndexClass() {
		return SameColumnTwiceInIndex.class;
	}

	protected Class<?> getOneColumnFieldLevelIndexClass() {
		return OneColumnFieldLevelIndex.class;
	}

	protected Class<?> getTwoColumnFieldLevelIndexClass() {
		return TwoColumnFieldLevelIndex.class;
	}

	protected Class<?> getThreeGoodFieldLevelIndexesClass() {
		return ThreeGoodFieldLevelIndexes.class;
	}

	protected Class<?> getFieldLevelIndexColumnCollisionClass() {
		return IndexColumnCollisionFieldLevel.class;
	}

	protected Class<?> getAutoNamedFieldLevelIndexClass() {
		return AutoNamedFieldLevelIndex.class;
	}

	protected Class<?> getOneColumnMethodLevelIndexClass() {
		return OneColumnMethodLevelIndex.class;
	}

	protected Class<AutoNamedMethodLevelIndex> getAutoNamedMethodLevelIndexClass() {
		return AutoNamedMethodLevelIndex.class;
	}

	protected Class<?> getTwoColumnMethodLevelIndexClass() {
		return TwoColumnMethodLevelIndex.class;
	}

	protected Class<?> getMethodLevelIndexColumnCollisionClass() {
		return IndexColumnCollisionMethodLevel.class;
	}

	protected Class<?> getThreeGoodMethodLevelIndexesClass() {
		return ThreeGoodMethodLevelIndex.class;
	}

	@Override
	protected Class<?> getAutoNamedSingleColumnIndex() {
		return AutoNamedClassLevelSingleFieldIndex.class;
	}

	@Override
	protected Class<?> getAutoNamedMultipleColumnIndex() {
		return AutoNamedClassLevelMultipleFieldIndex.class;
	}

	@Override
	protected Class<?> getIndexNameWithSpace() {
		return IndexNameWithSpace.class;
	}




	@Test
	public void testMethodLevelIndexColumnCollision() {
		final Map<String, String[]> expected = MetadataFactoryTestUtils.buildStringToStringArrayMap(
				"method_index1", new String[] { "column1" });
		MetadataFactoryTestUtils.testIndexes(getMethodLevelIndexColumnCollisionClass(), expected);
	}

	@Test
	public void testThreeGoodMethodLevelIndexes() {
		final Map<String, String[]> expected = MetadataFactoryTestUtils.buildStringToStringArrayMap(
				"method_index1", new String[] { "column1" },
				"method_index2", new String[] { "column2" },
				"method_index3", new String[] { "column3", "column4" });
		MetadataFactoryTestUtils.testIndexes(getThreeGoodMethodLevelIndexesClass(), expected);
	}

	@Test
	public void testAutoNamedMethodLevelIndex() {
		final Map<String, String[]> expected = MetadataFactoryTestUtils.buildStringToStringArrayMap(
				"method_column1", new String[] { "method_column1" });
		MetadataFactoryTestUtils.testIndexes(getAutoNamedMethodLevelIndexClass(), expected);
	}

	@Test
	public void testThreeGoodFieldLevelIndexes() {
		final Map<String, String[]> expected = MetadataFactoryTestUtils.buildStringToStringArrayMap(
				"field_index1", new String[] { "column1" },
				"field_index2", new String[] { "column2" },
				"field_index3", new String[] { "column3", "column4" });
		MetadataFactoryTestUtils.testIndexes(getThreeGoodFieldLevelIndexesClass(), expected);
	}

	@Test
	public void testTwoColumnFieldLevelIndex() {
		final Map<String, String[]> expected = MetadataFactoryTestUtils.buildStringToStringArrayMap(
				"field_index1", new String[] { "column1", "column2" });
		MetadataFactoryTestUtils.testIndexes(getTwoColumnFieldLevelIndexClass(), expected);
	}

	@Test
	public void testFieldLevelIndexColumnCollision() {
		final Map<String, String[]> expected = MetadataFactoryTestUtils.buildStringToStringArrayMap(
				"field_index1", new String[] { "column1" });
		MetadataFactoryTestUtils.testIndexes(getFieldLevelIndexColumnCollisionClass(), expected);
	}

	@Test
	public void testOneColumnMethodLevelIndex() {
		final Map<String, String[]> expected = MetadataFactoryTestUtils.buildStringToStringArrayMap(
				"method_index1", new String[] { "column1" });
		MetadataFactoryTestUtils.testIndexes(getOneColumnMethodLevelIndexClass(), expected);
	}

	@Test
	public void testOneColumnFieldLevelIndex() {
		final Map<String, String[]> expected = MetadataFactoryTestUtils.buildStringToStringArrayMap(
				"field_index1", new String[] { "column1" });
		MetadataFactoryTestUtils.testIndexes(getOneColumnFieldLevelIndexClass(), expected);
	}

	@Test
	public void testAutoNamedFieldLevelIndex() {
		final Map<String, String[]> expected = MetadataFactoryTestUtils.buildStringToStringArrayMap(
				"field_column1", new String[] { "field_column1" });
		MetadataFactoryTestUtils.testIndexes(getAutoNamedFieldLevelIndexClass(), expected);
	}

	@Test
	public void testTwoColumnMethodLevelIndex() {
		final Map<String, String[]> expected = MetadataFactoryTestUtils.buildStringToStringArrayMap(
				"method_index1", new String[] { "column1", "column2" });
		MetadataFactoryTestUtils.testIndexes(getTwoColumnMethodLevelIndexClass(), expected);
	}
}
