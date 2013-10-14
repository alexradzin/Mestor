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

import org.mestor.entities.index_test.AutoNamedFieldLevelIndex;
import org.mestor.entities.index_test.AutoNamedMethodLevelIndex;
import org.mestor.entities.index_test.DuplicateIndexName;
import org.mestor.entities.index_test.IndexColumnCollisionFieldLevel;
import org.mestor.entities.index_test.IndexColumnCollisionMethodLevel;
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

	@Override
	protected Class<?> getOneColumnFieldLevelIndexClass() {
		return OneColumnFieldLevelIndex.class;
	}

	@Override
	protected Class<?> getTwoColumnFieldLevelIndexClass() {
		return TwoColumnFieldLevelIndex.class;
	}

	@Override
	protected Class<?> getThreeGoodFieldLevelIndexesClass() {
		return ThreeGoodFieldLevelIndexes.class;
	}
	
	@Override
	protected Class<?> getFieldLevelIndexColumnCollisionClass() {
		return IndexColumnCollisionFieldLevel.class;
	}
	
	@Override
	protected Class<?> getAutoNamedFieldLevelIndexClass() {
		return AutoNamedFieldLevelIndex.class;
	}
	
	@Override
	protected Class<?> getOneColumnMethodLevelIndexClass() {
		return OneColumnMethodLevelIndex.class;
	}

	@Override
	protected Class<AutoNamedMethodLevelIndex> getAutoNamedMethodLevelIndexClass() {
		return AutoNamedMethodLevelIndex.class;
	}

	@Override
	protected Class<?> getTwoColumnMethodLevelIndexClass() {
		return TwoColumnMethodLevelIndex.class;
	}

	@Override
	protected Class<?> getMethodLevelIndexColumnCollisionClass() {
		return IndexColumnCollisionMethodLevel.class;
	}
	
	@Override
	protected Class<?> getThreeGoodMethodLevelIndexesClass() {
		return ThreeGoodMethodLevelIndex.class;
	}
}
