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
package org.mestor.metadata.em;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;

import org.junit.Ignore;
import org.junit.Test;
import org.mestor.em.MestorProperties;
import org.mestor.entities.SimpleFieldsProperty;
import org.mestor.entities.annotated.SimpleProperty;

public class EntityManagerTest {

	private EntityManager getEntityManager(final String persistenceXmlLocation, final String puName) {
		System.setProperty(MestorProperties.PERSISTENCE_XML.key(),
				persistenceXmlLocation);
		return Persistence.createEntityManagerFactory(puName)
				.createEntityManager();
	}

	@Test
	public void testSimplePropertyManipulation() {
		final EntityManager em = getEntityManager("simple_property.xml", "simple_property");

		final SimpleProperty sp = new SimpleProperty();
		sp.setName("name");
		sp.setType(String.class);
		sp.setValue("value");
		em.persist(sp);
		findAndCheckSimpleProperty(em, sp);

		sp.setValue("merge");
		em.persist(sp);
		findAndCheckSimpleProperty(em, sp);

		em.remove(sp);
		final SimpleProperty spDb = em.find(SimpleProperty.class, sp.getName());
		assertNull(spDb);
	}

	private void findAndCheckSimpleProperty(final EntityManager em,
			final SimpleProperty sp) {
		final SimpleProperty spDb = em.find(SimpleProperty.class, sp.getName());
		assertEquals(sp.getName(), spDb.getName());
		assertEquals(sp.getType(), spDb.getType());
		assertEquals(sp.getValue(), spDb.getValue());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testWrongFile() {
		getEntityManager("wrong_file.xml", "wrong");
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testWrongPu() {
		getEntityManager("wrong.xml", "wrong_pu");
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testWrongClass() {
		getEntityManager("wrong.xml", "wrong_class");
	}

	@Test(expected = IllegalStateException.class)
	public void testWrongHost() {
		getEntityManager("wrong.xml", "wrong_host");
	}
	
	@Test
	public void testSimpleFieldsProperty() {
		final EntityManager em = getEntityManager("simple_fields_property.xml", "simple_fields_property");
		for (int i = 0; i < 1000; i++) {
			final SimpleFieldsProperty sfp = new SimpleFieldsProperty();
			sfp.setId(Long.valueOf(i));
			sfp.setName(String.valueOf(i));
			sfp.setDate(new Date());
			em.persist(sfp);
		}
	}

	@Ignore
	@Test
	public void testStartEntityManager() {
		final EntityManager em = getEntityManager("persistence.xml", "integration_test");
		// TODO: finish him
	}

}