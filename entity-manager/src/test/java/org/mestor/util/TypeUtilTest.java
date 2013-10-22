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

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class TypeUtilTest {
	@Test
	public void isCompatibleToInterfaceToItsImplementation() {
		testIsCompatibleTo(List.class, ArrayList.class, true);
	}

	@Test
	public void isCompatibleToImplementationToItsInterface() {
		testIsCompatibleTo(ArrayList.class, List.class, false);
	}

	@Test
	public void isCompatibleToSameClasses() {
		testIsCompatibleTo(ArrayList.class, ArrayList.class, true);
	}

	@Test
	public void isCompatibleToSamePrimitives() {
		testIsCompatibleTo(long.class, long.class, true);
	}

	@Test
	public void isCompatibleToSamePrimitiveWrappers() {
		testIsCompatibleTo(Short.class, Short.class, true);
	}

	@Test
	public void isCompatibleToPrimitiveAndCorrespondingWrapper() {
		testIsCompatibleTo(double.class, Double.class, true);
	}

	@Test
	public void isCompatibleToWrapperAndCorrespondingPrimitive() {
		testIsCompatibleTo(Double.class, double.class, true);
	}

	@Test
	public void isCompatibleToPrimiteiveAndCompatiblePrimitive() {
		testIsCompatibleTo(long.class, int.class, true);
	}

	@Test
	public void isCompatibleToPrimiteiveAndCompatibleWrapper() {
		testIsCompatibleTo(long.class, Short.class, true);
	}

	@Test
	public void isCompatibleToPrimiteiveAndIncompatibleWrapper() {
		testIsCompatibleTo(short.class, Integer.class, false);
	}

	@Test
	public void isCompatibleToPrimiteiveAndIncompatiblePrimitive() {
		testIsCompatibleTo(byte.class, short.class, false);
	}

	private void testIsCompatibleTo(final Class<?> left, final Class<?> right, final boolean expected) {
		assertEquals(expected, TypeUtil.isCompatibleTo(left, right));
	}
}
