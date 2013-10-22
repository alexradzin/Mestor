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

import java.util.HashMap;
import java.util.Map;

import com.google.common.primitives.Primitives;

/**
 * This class contains type conversion and discovery utilities
 * @author alexr
 */
public class TypeUtil {
	private static Class<?>[] compatibility = new Class[] {
		double.class, float.class, long.class, int.class, short.class, byte.class
	};

	private static Map<Class<?>, Integer> compatibilityMap = new HashMap<>();
	static {
		for (int i = 0; i < compatibility.length; i++) {
			compatibilityMap.put(compatibility[i], i);
		}
	}


	/**
	 * This method is an "extension" of {@link Class#isAssignableFrom(Class)} but supports primitives and primitive
	 * wrappers as well.
	 * <br/>
	 * The method returns {@code true} if {@code left.isAssignableFrom(right)} is true or if unwrapped version of both
	 * classes are equal or if both classes represent numeric type and {@code right} can be assigned to {@code left}
	 * using natural conversion, e.g. {@code int} can be assigned to {@code long} but {@code long} cannot be assigned to
	 * {@code int} without casting and possible presision lost.
	 *
	 * @param left
	 * @param right
	 * @return true if right is assignable to left
	 */
	public static boolean isCompatibleTo(final Class<?> left, final Class<?> right) {
		if (left.isAssignableFrom(right)) {
			return true;
		}

		final Class<?> l = Primitives.unwrap(left);
		final Class<?> r = Primitives.unwrap(right);

		if (l == null || r == null) {
			return false;  // at list one of them is not primitive
		}

		// both are primitives

		// if both primitive types are the same they are definitly compatible
		if (l.equals(r)) {
			return true;
		}

		// check in chain of compatibility
		final Integer leftCompIndex = compatibilityMap.get(l);
		final Integer rightCompIndex = compatibilityMap.get(r);

		if (leftCompIndex == null || rightCompIndex == null) {
			// at least one of them is not numeric
			return false;
		}

		return leftCompIndex < rightCompIndex;
	}
}
