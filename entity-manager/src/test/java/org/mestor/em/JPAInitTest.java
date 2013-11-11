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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Properties;
import java.util.regex.Pattern;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;

import org.hamcrest.CustomMatcher;
import org.hamcrest.Matcher;
import org.junit.Test;
import org.mestor.context.EntityContext;
import org.mestor.entities.annotatedidgenerator.NumericGeneratedId;
import org.mestor.entities.annotatedidgenerator.SimpleNumericId;
import org.mestor.entities.annotatedidgenerator.StringGeneratedId;
import org.mestor.persistence.idgenerator.SimpleNumericSequence;
import org.mestor.persistence.idgenerator.SimpleUuidSequence;

public class JPAInitTest {
	private final String MESTOR_PREFIX = "org.mestor";
	private final String MESTOR_PERSISTOR_CLASS = MESTOR_PREFIX + "." + "persistor.class";
	private final String MESTOR_ID_GENERATOR = MESTOR_PREFIX + "." + "id.generator";
	//private final String MESTOR_MANAGED_PACKAGE = MESTOR_PREFIX + "." + "managed.package";
	private final String MANAGED_CLASS_NAMES = MESTOR_PREFIX + "." + "managed.entities";

	// example: b3312373-254a-4fa7-959c-c5f6cd68b8cd
	private final static Pattern uuidPattern = Pattern.compile("^[a-z0-9]{8}(?:-[a-z0-9]{4}){3}-[a-z0-9]{12}$");




	@Test
	public void testEmptyInit() {
		EntityManager em = testInitPersistor(DummyPersistor.class.getName());
		assertNotNull(em);
		assertEquals(EntityManagerImpl.class, em.getClass());
	}

	@Test
	public void testEmptyInitUndefinedPersistor() {
		final String className = "ThisClassDoesNotExist";
		try {
			testInitPersistor(className);
			fail();
		} catch (IllegalStateException e) {
			final Throwable cause = e.getCause();
			assertEquals(ClassNotFoundException.class, cause.getClass());
			assertEquals(className, cause.getMessage());
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void testEmptyInitWrongPersistor() {
		// this test case class is not a persistor
		testInitPersistor(getClass().getName());
	}



	@Test(expected = IllegalArgumentException.class)
	public void testInitNoPersistor() {
		Persistence.createEntityManagerFactory("mestortest").createEntityManager();
		fail();
	}

	@Test
	public void testDefaultLongIdGenerator() {
		testIdGenerator(MESTOR_ID_GENERATOR, SimpleNumericSequence.class, NumericGeneratedId.class, new Object[] {1L, 2L});
		testIdGenerator(MESTOR_ID_GENERATOR, SimpleNumericSequence.class, SimpleNumericId.class, new Object[] {1L, 2L});
	}

	@Test
	public void testNamedLongIdGenerator() {
		testIdGenerator(MESTOR_ID_GENERATOR + "." + "number-generator", SimpleNumericSequence.class, NumericGeneratedId.class, new Object[] {1L, 2L});
	}


	@Test
	public void testLongIdGenerator() {
		testIdGenerator(MESTOR_ID_GENERATOR + "." + Long.class.getName(), SimpleNumericSequence.class, SimpleNumericId.class, new Object[] {1L, 2L});
	}

	@Test
	public void testEntityIdGenerator() {
		testIdGenerator(MESTOR_ID_GENERATOR + "." + SimpleNumericId.class.getName(), SimpleNumericSequence.class, SimpleNumericId.class, new Object[] {1L, 2L});
	}

	@Test
	public void testEntitySpecificFieldIdGenerator() {
		testIdGenerator(MESTOR_ID_GENERATOR + "." + SimpleNumericId.class.getName() + "#" + "id", SimpleNumericSequence.class, SimpleNumericId.class, new Object[] {1L, 2L});
	}


	@Test
	public void testNamedStringIdGenerator() {
		testIdGenerator(MESTOR_ID_GENERATOR + "." + "string-generator", SimpleUuidSequence.class, StringGeneratedId.class, new CustomMatcher<String[]>("Unexpected IDs") {
			@Override
			public boolean matches(Object item) {
				Object[] ids = (Object[])item;
				if (ids == null || ids.length != 2) {
					return false;
				}

				for (Object id : ids) {
					if (!uuidPattern.matcher(id.toString()).find()) {
						return false;
					}
				}

				return true;
			}});
	}



	private EntityManager testInitPersistor(String persistorClassName) {
		return testInit(MESTOR_PERSISTOR_CLASS, persistorClassName);
	}





	private EntityManager testInit(String propName, String propValue) {
		return testInit(new String[] {propName}, new String[] {propValue});
	}

	private EntityManager testInit(String[] propNames, String[] propValues) {
		assertEquals(propNames.length, propValues.length);
		for (int i = 0; i < propNames.length; i++) {
			System.setProperty(propNames[i], propValues[i]);
		}
		try {
			return Persistence.createEntityManagerFactory("mestortest").createEntityManager();
		} finally {
			final Properties sysprops = System.getProperties();
			for (int i = 0; i < propNames.length; i++) {
				sysprops.remove(propNames[i]);
			}
		}
	}

	private <ID> void testIdGenerator(final String idGenPropName, final Class<?> idGenClass, final Class<?> entityClass, final ID[] expectedIds) {
		testIdGenerator(idGenPropName, idGenClass, entityClass, new CustomMatcher<ID[]> ("Unexpected ids") {
			@SuppressWarnings("unchecked")
			@Override
			public boolean matches(Object item) {
				return Arrays.equals(expectedIds, (ID[])item);
			}});
	}

	private <ID> void testIdGenerator(String idGenPropName, Class<?> idGenClass, Class<?> entityClass, Matcher<ID[]> matcher) {
		EntityManager em = testInit(
				new String[] {
						MESTOR_PERSISTOR_CLASS,
						idGenPropName,
						MANAGED_CLASS_NAMES,
				},
				new String[] {
						DummyPersistor.class.getName(),
						idGenClass.getName(),
						entityClass.getName()
				});

		EntityContext ctx = (EntityContext)em;
		ID id1 = ctx.getNextId(entityClass, "id");
		ID id2 = ctx.getNextId(entityClass, "id");

		System.out.println(id1);

		assertTrue(matcher.matches(new Object[] {id1, id2}));
	}

}
