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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.Closeable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mestor.cassandra.CassandraAwareTestRunner;
import org.mestor.context.EntityContext;
import org.mestor.em.MestorProperties;
import org.mestor.entities.Child;
import org.mestor.entities.Country;
import org.mestor.entities.Human;
import org.mestor.entities.Parent;
import org.mestor.entities.annotated.AbstractEntity;
import org.mestor.entities.annotated.Address;
import org.mestor.entities.annotated.EmailAddress;
import org.mestor.entities.annotated.Event;
import org.mestor.entities.annotated.EventProperty;
import org.mestor.entities.annotated.Person;
import org.mestor.entities.annotated.Person.Gender;
import org.mestor.entities.annotated.SimpleProperty;
import org.mestor.entities.annotated.StreetAddress;
import org.mestor.entities.annotated.User;
import org.mestor.entities.annotated.UserRole;
import org.mestor.entities.annotatedidgenerator.CompositeGeneratedId;
import org.mestor.entities.annotatedidgenerator.SimpleNumericId;
import org.mestor.entities.annotatedidgenerator.StringGeneratedId;
import org.mestor.entities.queries.NamedQueriesEntity;

import com.datastax.driver.core.exceptions.InvalidQueryException;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;

@RunWith(CassandraAwareTestRunner.class)
public class EntityManagerTest {
	// example: b3312373-254a-4fa7-959c-c5f6cd68b8cd
	private final static Pattern uuidPattern = Pattern.compile("^[a-z0-9]{8}(?:-[a-z0-9]{4}){3}-[a-z0-9]{12}$");

	@SuppressWarnings("unchecked")
	private <EM extends EntityManager & Closeable> EM getEntityManager(final String persistenceXmlLocation, final String puName) {
		System.setProperty(MestorProperties.PERSISTENCE_XML.key(), persistenceXmlLocation);
		return (EM)Persistence.createEntityManagerFactory(puName).createEntityManager();
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
			selectAfterId.setParameter(1, entities[OFFSET].getIdentifier());
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

	@Test
	public void testNamedQueriesSelectWithCount() {
		final EntityManager em = getEntityManager("named_queries.xml", "named_queries");
		try{
			final int count = 4;
			createNamedQueriesEntities(count, em);
			final TypedQuery<Long> selectCount = em.createNamedQuery("selectCount", Long.class);
			final List<Long> resultList = selectCount.getResultList();
			assertEquals(1, resultList.size());
			for(final Long actual : resultList) {
				assertEquals(actual, Long.valueOf(count));
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
			selectAfterId.setParameter("identifier", entities[OFFSET].getIdentifier());
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

	@Test
	public void testStartEntityManager() {
		final EntityManager em = getEntityManager("persistence.xml", "integration_test");
		try {
		assertNotNull(em);
		final Metamodel metamodel = em.getMetamodel();
		assertNotNull(metamodel);


		assertEquals(
				new HashSet<Class<?>>(Arrays.asList(Person.class, User.class, Address.class, EmailAddress.class, StreetAddress.class, SimpleProperty.class)),
				new HashSet<Class<?>>(Collections2.transform(metamodel.getEntities(), new Function<EntityType<?>, Class<?>>() {
					@Override
					public Class<?> apply(final EntityType<?> et) {
						return et.getJavaType();
					}
				})));
		} finally {
			em.close();
		}
	}


	@Test
	public void testCrudWithHierarchicalCollectionEntityManager() {
		final EntityManager em = getEntityManager("persistence.xml", "integration_test");
		try {
			assertNotNull(em);
			final Metamodel metamodel = em.getMetamodel();
			assertNotNull(metamodel);
	
			final Person jl = new Person("John", "Lennon", Gender.MALE);
			jl.setIdentifier(1);
			final StreetAddress jlHome = new StreetAddress(1, "251", "Menlove Avenue", "Liverpool", Country.GB);
			final EmailAddress jlEmail = new EmailAddress();
			jl.setAddresses(Arrays.<Address>asList(jlHome, jlEmail));
	
			em.persist(jl);
			em.persist(jlHome);
			em.persist(jlEmail);
	
			// The following does not work because addresses are inherited using TABLLE_PER_CLASS strategy
			// that does not work well now even in Persister.fetch() because it assumes that table of base class exists.
			// It also does not work when fetching lazy dependent collection because ObjectWrapperFactory tries to create instance of
			// abstract class Address.
			// Bottom line: the inheritance support should be reviewed and fixed, however we do not need it now, so this task is postponed.
	//		final Person foundJl = em.find(Person.class, 1);
	//		assertNotNull(foundJl);
	
			em.remove(jl);
			em.remove(jlHome);
			em.remove(jlEmail);
		} finally {
			em.close();
		}
	}

	@Test
	public void testCrudWithCollectionEntityManager() throws MalformedURLException {
		final EntityManager em = getEntityManager("persistence.xml", "integration_test");
		try {
			assertNotNull(em);
			final Metamodel metamodel = em.getMetamodel();
			assertNotNull(metamodel);
	
			final Person jl = new Person("John", "Lennon", Gender.MALE);
			jl.setIdentifier(1);
	
	
			final User user = new User(new URL("http://www.thebeatles.com"), "john", "^John$", EnumSet.of(UserRole.ADMINISTRATOR));
			jl.setAccounts(Collections.singletonList(user));
			user.setPerson(jl);
	
			em.persist(jl);
			em.persist(user);
	
			final Person foundJl = em.find(Person.class, 1);
			assertNotNull(foundJl);
	
			assertPerson(jl, foundJl);
		} finally {
			em.close();
		}
	}


	@Test
	public void testAutomaticLongIdGeneration() {
		final EntityManager em = getEntityManager("id_generator.xml", "id_generator_test");
		try{
			assertNull(em.find(SimpleNumericId.class, 1L));
			final SimpleNumericId snid = new SimpleNumericId();
			em.persist(snid);
			SimpleNumericId found = em.find(SimpleNumericId.class, 1L);
			assertNotNull(found);
			em.remove(found);
			assertNull(em.find(SimpleNumericId.class, 1L));
		} finally {
			em.close();
		}
	}

	@Test
	public void testAutomaticStringIdGeneration() {
		final EntityManager em = getEntityManager("id_generator.xml", "id_generator_test");
		try{
			final String query = "select * from StringGeneratedId";
			em.createQuery(query, StringGeneratedId.class);
			assertTrue(em.createQuery(query, StringGeneratedId.class).getResultList().isEmpty());
			final StringGeneratedId snid = new StringGeneratedId();
			em.persist(snid);

			List<StringGeneratedId> foundEntities = em.createQuery(query, StringGeneratedId.class).getResultList();
			assertNotNull(foundEntities);
			assertFalse(foundEntities.isEmpty());
			assertTrue(foundEntities.size() == 1);

			StringGeneratedId found = foundEntities.get(0);
			assertNotNull(found);
			assertNotNull(found.getId());
			assertTrue(uuidPattern.matcher(found.getId()).find());

			em.remove(found);
			assertTrue(em.createQuery(query, StringGeneratedId.class).getResultList().isEmpty());
		} finally {
			em.close();
		}
	}

	@Test
	public void testAutomaticCompositeIdGeneration() {
		final EntityManager em = getEntityManager("id_generator.xml", "id_generator_test");
		try{
			final String query = "select * from CompositeGeneratedId";
			em.createQuery(query, CompositeGeneratedId.class);
			assertTrue(em.createQuery(query, CompositeGeneratedId.class).getResultList().isEmpty());
			final CompositeGeneratedId snid = new CompositeGeneratedId();
			em.persist(snid);

			List<CompositeGeneratedId> foundEntities = em.createQuery(query, CompositeGeneratedId.class).getResultList();
			assertNotNull(foundEntities);
			assertFalse(foundEntities.isEmpty());
			assertTrue(foundEntities.size() == 1);

			CompositeGeneratedId found = foundEntities.get(0);
			assertNotNull(found);
			assertNotNull(found.getId());
			assertNotNull(found.getName());

			assertEquals(1L, found.getId().longValue());
			assertTrue(uuidPattern.matcher(found.getName()).find());

			em.remove(found);
			assertTrue(em.createQuery(query, CompositeGeneratedId.class).getResultList().isEmpty());
		} finally {
			em.close();
		}
	}


	//TODO: move this method to reusable utility
	private void assertPerson(final Person expected, final Person actual) {
		if (expected == null) {
			assertNull(actual);
			return;
		}
		assertNotNull(actual);
		assertEquals(expected.getIdentifier(), actual.getIdentifier());
		assertEquals(expected.getName(), actual.getName());
		assertEquals(expected.getLastName(), actual.getLastName());
	}


	@Test
	public <EM extends EntityManager & Closeable> void testAutoIdsAndCascade() {
		EntityManager em = getEntityManager("cascade_test.xml", "events");
		try {
			Event event = new Event("greeting");
			EventProperty helloWorld = new EventProperty("hello", "world");
			EventProperty hiThere = new EventProperty("hi", "there");
			event.setProperties(Arrays.asList(helloWorld, hiThere));

			em.persist(event);

			for (Long id : new Long[] {event.getId(), helloWorld.getId(), hiThere.getId()}) {
				assertNotNull(id);
			}

			assertNotNull(em.find(Event.class, event.getId()));
			assertNotNull(em.find(EventProperty.class, helloWorld.getId()));
			assertNotNull(em.find(EventProperty.class, hiThere.getId()));


			em.remove(event);

			// Since event properties are cascaded they must be removed together with their parent
			assertNull(em.find(Event.class, event.getId()));
			assertNull(em.find(EventProperty.class, helloWorld.getId()));
			assertNull(em.find(EventProperty.class, hiThere.getId()));
		} finally {
			em.clear();
			em.close();
		}
	}



}