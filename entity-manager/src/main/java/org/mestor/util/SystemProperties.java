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

package org.mestor.util;

import java.util.Properties;
import java.util.Map.Entry;

public class SystemProperties {
	/**
	 * This method retrieves system properties making sure that we take only properties that are indeed of String type.
	 * Theoretically system properties should not contain values other than strings.
	 * However since java.util.Properties extends Hashtable<Object, Object>
	 * system properties can contain such values. If so this causes NullPointerException
	 * into Maps.fromProperties(). To prevent this situation we filter such value out here.
	 * @return
	 */
	public static Properties systemProperties() {
		final Properties sysprops = new Properties();
		for (final Entry<Object, Object> entry : System.getProperties().entrySet()) {
			final Object key = entry.getKey();
			final Object value = entry.getValue();
			if (key instanceof String && value instanceof String) {
				sysprops.put(key, value);
			}
		}
		return sysprops;
	}
}
