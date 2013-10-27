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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.datastax.driver.core.Row;

class RowSplitter extends RowFunctionAdapter<Map<String, Object>> /*implements Function<Row, Map<String, Object>>*/ {
	private final Map<String, Class<?>[]> fields;

	RowSplitter(final Map<String, Class<?>[]> fields) {
		this.fields = fields;
	}

	@Override
	public Map<String, Object> apply(final Row row) {
		final Map<String, Object> data = new LinkedHashMap<>();

		for (final Entry<String, Class<?>[]> field : fields.entrySet()) {
			final String fieldName = field.getKey();
			final Class<?>[] fieldType = field.getValue();
			data.put(fieldName, getValue(row, fieldName, fieldType));
		}

		return data;
	}
}
