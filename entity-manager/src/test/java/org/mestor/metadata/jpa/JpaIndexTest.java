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

import org.mestor.entities.index_test.DuplicateIndexName;
import org.mestor.entities.index_test.OneColumnIndex;
import org.mestor.entities.index_test.SameColumnTwiceInIndex;
import org.mestor.entities.index_test.ThreeGoodIndexes;
import org.mestor.entities.index_test.TwoColumnIndex;

public class JpaIndexTest extends IndexTestBase {
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
}
