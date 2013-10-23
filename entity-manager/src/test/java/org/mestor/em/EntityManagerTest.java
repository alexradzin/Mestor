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
package org.mestor.em;

import static org.junit.Assert.assertEquals;
import static org.mestor.em.EntityManagerImpl.compareTypes;

import org.junit.Test;

public class EntityManagerTest {
	
	private static class TypesTest{
		private final Class<?> actualType;
		private final Class<?> declaredType;
		private final boolean result;

		private TypesTest(final Class<?> declaredType, final Class<?> actualType, final boolean result){
			this.declaredType = declaredType;
			this.actualType = actualType;
			this.result = result;
		}
	}
	
	@Test
	public void testCompareTypes(){
		final TypesTest[] types = new TypesTest[]{
				new TypesTest(int.class, Integer.class, true),
				new TypesTest(long.class, Long.class, true),
				new TypesTest(Integer.class, Integer.class, true),
				new TypesTest(Long.class, Long.class, true),
				new TypesTest(String.class, String.class, true),
				new TypesTest(Object.class, String.class, true),
				
				//?????new TypesTest(long.class, Integer.class, true)
				//?????new TypesTest(Long.class, Integer.class, true)
		};
		for(final TypesTest type : types){
			assertEquals(type.result, compareTypes(type.declaredType, type.actualType));
		}
	}
}
