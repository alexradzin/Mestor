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

import javax.persistence.EntityManager;
import javax.persistence.Persistence;

import org.junit.Ignore;
import org.junit.Test;
import org.mestor.em.MestorProperties;
import org.mestor.entities.annotated.SimpleProperty;

public class EntityManagerTest {

	
	@Test
	public void testSimplePropertyManipulation(){
		final String persistenceXmlLocation = "simple_property.xml";
		System.setProperty(MestorProperties.PERSISTENCE_XML.key(), persistenceXmlLocation);
		final EntityManager em = Persistence.createEntityManagerFactory("simple_property").createEntityManager();
		
		final SimpleProperty sp = new SimpleProperty();
		sp.setName("name");
		sp.setType(String.class);
		sp.setValue("value");
		em.persist(sp);
		findAndCheck(em, sp);
		
		sp.setValue("merge");
		em.persist(sp);
		findAndCheck(em, sp);
	}

	private void findAndCheck(final EntityManager em, final SimpleProperty sp) {
		final SimpleProperty spDb = em.find(SimpleProperty.class, sp.getName());
		assertEquals(sp.getName(), spDb.getName());
		assertEquals(sp.getType(), spDb.getType());
		assertEquals(sp.getValue(), spDb.getValue());
	}
	
	@Ignore
	@Test
	public void testStartEntityManager(){
		final String persistenceXmlLocation = "persistence.xml";
		System.setProperty(MestorProperties.PERSISTENCE_XML.key(), persistenceXmlLocation);
		Persistence.createEntityManagerFactory("integration_test").createEntityManager();
	}
}
