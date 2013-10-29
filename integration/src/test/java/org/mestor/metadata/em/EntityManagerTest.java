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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;

import org.junit.Ignore;
import org.junit.Test;
import org.mestor.context.EntityContext;
import org.mestor.em.MestorProperties;
import org.mestor.entities.Child;
import org.mestor.entities.Human;
import org.mestor.entities.Parent;
import org.mestor.entities.annotated.AbstractEntity;
import org.mestor.entities.annotated.SimpleProperty;
import org.mestor.entities.queries.NamedQueriesEntity;

import com.datastax.driver.core.exceptions.InvalidQueryException;

public class EntityManagerTest {
	
	private EntityManager getEntityManager(final String persistenceXmlLocation, final String puName) {
		System.setProperty(MestorProperties.PERSISTENCE_XML.key(), persistenceXmlLocation);
		return Persistence.createEntityManagerFactory(puName).createEntityManager();
	}

	@Test
	public void testSimplePropertyManipulation() {
		final EntityManager em = getEntityManager("simple_property.xml", "simple_property");
		try{
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
		} finally {
			em.close();
		}
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
	public void testGetEntityMetadataByEntityName(){
		final EntityManager em = getEntityManager("simple_property.xml", "simple_property");
		try{
			assertTrue(em instanceof EntityContext);
			final EntityContext ec = (EntityContext)em;
			assertNotNull(ec.getEntityMetadata(SimpleProperty.class.getSimpleName()));
		} finally {
			em.close();
		}
	}
	
	@Test
	public void testNamedQueriesSelectAll() {
		final EntityManager em = getEntityManager("named_queries.xml", "named_queries");
		try{
			final NamedQueriesEntity[] entities = createNamedQueriesEntities(10, em);
			final TypedQuery<NamedQueriesEntity> selectSorted = em.createNamedQuery("selectSorted", NamedQueriesEntity.class);
			final List<NamedQueriesEntity> resultList = selectSorted.getResultList();
			assertEquals(entities.length, resultList.size());
			//TODO: no order by is implemented yet
			sortAbstractEntityList(resultList);
			int i = 0;
			for(final NamedQueriesEntity actual : resultList){
				final NamedQueriesEntity expected = entities[i];
				compare(actual, expected);
				i++;
			}
		} finally {
			em.close();
		}
	}
	
	@Test
	public void testNamedQueriesSelectWithWhere() {
		final EntityManager em = getEntityManager("named_queries.xml", "named_queries");
		try{
			final NamedQueriesEntity[] entities = createNamedQueriesEntities(4, em);
			final TypedQuery<NamedQueriesEntity> selectAfterId = em.createNamedQuery("selectById", NamedQueriesEntity.class);
			final int OFFSET = 1;
			selectAfterId.setParameter(0, entities[OFFSET].getIdentifier());
			final List<NamedQueriesEntity> resultList = selectAfterId.getResultList();
			assertEquals(1, resultList.size());
			for(final NamedQueriesEntity actual : resultList){
				final NamedQueriesEntity expected = entities[OFFSET];
				compare(actual, expected);
			}
		} finally {
			em.close();
		}
	}

	private void compare(final NamedQueriesEntity actual, final NamedQueriesEntity expected) {
		assertEquals(expected.getIdentifier(), actual.getIdentifier());
		assertEquals(expected.getLastModified(), actual.getLastModified());
		assertEquals(expected.getName(), actual.getName());
	}
	
	@Test
	public void testNamedQueriesSelectWithWhereWrong() {
		final EntityManager em = getEntityManager("named_queries.xml", "named_queries");
		try{
			final NamedQueriesEntity[] entities = createNamedQueriesEntities(4, em);
			final TypedQuery<NamedQueriesEntity> selectAfterId = em.createNamedQuery("selectAfterId", NamedQueriesEntity.class);
			final int OFFSET = 1;
			selectAfterId.setParameter(0, entities[OFFSET].getIdentifier());
			final List<NamedQueriesEntity> resultList = selectAfterId.getResultList();
			/*assertEquals(entities.length - OFFSET, resultList.size());
			//TODO: no order by is implemented yet
			sortAbstractEntityList(resultList);
			int i = OFFSET + 1;
			for(final NamedQueriesEntity actual : resultList){
				final NamedQueriesEntity expected = entities[i];
				assertEquals(expected.getIdentifier(), actual.getIdentifier());
				assertEquals(expected.getLastModified(), actual.getLastModified());
				assertEquals(expected.getName(), actual.getName());
				i++;
			}*/
		} catch(final InvalidQueryException e) {
			assertEquals("Only EQ and IN relation are supported on the partition key (you will need to use the token() function for non equality based relation)", e.getMessage());
		} finally {
			em.close();
		}
	}
	
	private void sortAbstractEntityList(final List<? extends AbstractEntity> list){
		Collections.sort(list, new Comparator<AbstractEntity>() {
			@Override
			public int compare(final AbstractEntity o1, final AbstractEntity o2) {
				return o1.getIdentifier() - o2.getIdentifier();//simple compare, no overflow is taken into account
			}
		});
	}
	
	private NamedQueriesEntity[] createNamedQueriesEntities(final int count, final EntityManager em) {
		final NamedQueriesEntity[] res = new NamedQueriesEntity[count];
		for (int i = 0; i < count; i++) {
			final NamedQueriesEntity e = new NamedQueriesEntity();
			e.setIdentifier(i);
			e.setLastModified(System.nanoTime());
			e.setName("Name_" + i);
			em.persist(e);
			res[i] = e;
		}
		return res;
	}
	
	@Ignore
	@Test
	public void testCascade() {
		final EntityManager em = getEntityManager("parent_child.xml", "parent_child");
		try{
			final Parent parent = createParent(em, "Parent");
			createChild(em, "Child", parent);
			final Parent updatedParent = em.find(Parent.class, parent.getIdentifier());
			assertTrue(updatedParent.getChildren() != null && updatedParent.getChildren().size() == 1);
		} finally {
			em.close();
		}
	}
	
	private Parent createParent(final EntityManager em, final String name) {
		final Parent parent = new Parent();
		setHumanProps(parent, name);
		em.persist(parent);
		return parent;
	}

	final AtomicLong idSequence = new AtomicLong();
	private void setHumanProps(final Human parent, final String name) {
		parent.setIdentifier((int)idSequence.incrementAndGet());
		parent.setName(name);
	}
	
	private Child createChild(final EntityManager em, final String name, final Parent parent) {
		final Child child = new Child();
		setHumanProps(child, name);
		child.setParent(parent);
		em.persist(child);
		return child;
	}

	@Ignore
	@Test
	public void testStartEntityManager() {
		final EntityManager em = getEntityManager("persistence.xml", "integration_test");
		// TODO: finish him
	}

}