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

package org.mestor.metadata;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mestor.context.DirtyEntityManager;
import org.mestor.context.EntityContext;
import org.mestor.entities.Person;
import org.mestor.entities.Person.Gender;
import org.mestor.util.BeanFieldMatcher;
import org.mestor.wrap.ObjectWrapperFactory;
import org.mestor.wrap.javassist.JavassistObjectWrapperFactory;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

/**
 * This test case contains tests that verify wrapping functionality of {@link ObjectWrapperFactory}.
 * The test case currently verifies {@link JavassistObjectWrapperFactory} but is parameterized and ready
 * to support other implementations of {@link ObjectWrapperFactory} (see {@link #getWrapperFactoryConstructors()}).
 *
 * <br/><br/>
 *
 * This test case is massively using mocking and verifies the {@link ObjectWrapperFactory} only. Since concrete implementations
 * of {@link ObjectWrapperFactory} can depend on other components that should be verified together it is useful to create
 * tests that use real implementations of referenced interfaces that are mocked here.
 * For example {@code ObjectWrapperFactoryWithDirtyEntityManagerTest} uses {@code EntityTransactionImpl} as a real implementation
 * of {@link DirtyEntityManager} (see {@link #getDirtyEntityManager()}).
 *
 *
 * @author alexr
 *
 * @param <W>
 */
@RunWith(Parameterized.class)
public class ObjectWrapperFactoryTest<W extends ObjectWrapperFactory<?>> {
    @Mock
    protected EntityContext ctx;
    private final Constructor<W> constructor;

    public ObjectWrapperFactoryTest(final String name, final Constructor<W> constructor) {
    	this.constructor = constructor;
    }


    @Parameters(name="{0}")
    public static List<Object[]> getWrapperFactoryTestParameters() throws NoSuchMethodException {
    	return Lists.transform(getWrapperFactoryConstructors(), new Function<Constructor<?>, Object[]>() {
    		@Override
			public Object[] apply(final Constructor<?> c) {
    			return new Object[] {c.getDeclaringClass().getSimpleName(), c};
    		}
    	});
    }

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}



    public static List<Constructor<?>> getWrapperFactoryConstructors() throws NoSuchMethodException {
    	return Arrays.<Constructor<?>>asList(JavassistObjectWrapperFactory.class.getConstructor(EntityContext.class));
    }


    @Test
    public void testWrapAndUnWrap() {
    	final Person person  = new Person();
		final ObjectWrapperFactory<Person> personfact = createWrapperFactory(Person.class);

        assertFalse(personfact.isWrapped(person));
        final Person personProxy = personfact.wrap(person);
        assertTrue(personfact.isWrapped(personProxy));
        final Person temp =  personfact.unwrap(personProxy);
        assertFalse(personfact.isWrapped(temp));
    }


    @Test(expected = RuntimeException.class)
    public void testWrapFinalClass() {
    	final Pattern p = Pattern.compile("");
		createWrapperFactory(Pattern.class).wrap(p);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWrapNoDefaultConstructor() {
		createWrapperFactory(File.class).wrap(new File(""));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsRemovedNotWrappedEntity() {
		final ObjectWrapperFactory<Person> personfact = createWrapperFactory(Person.class);
		personfact.isRemoved(new Person());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsRemovedNotEntity() {
		final ObjectWrapperFactory<String> personfact = createWrapperFactory(String.class);
		personfact.isRemoved("");
    }

    @Test
    public void testIsRemovedNotRemovedEntity() {
		final ObjectWrapperFactory<Person> personfact = createWrapperFactory(Person.class);
		final Person person = personfact.wrap(new Person());
		assertFalse(personfact.isRemoved(person));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsRemovedNullEntity() {
		final ObjectWrapperFactory<Person> personfact = createWrapperFactory(Person.class);
		personfact.isRemoved(null);
    }

    @Test
    public void testIsRemovedRemovedEntity() {
		final ObjectWrapperFactory<Person> personfact = createWrapperFactory(Person.class);
		final Person person = personfact.wrap(new Person());
		personfact.markAsRemoved(person);
		assertTrue(personfact.isRemoved(person));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMarkRemovedNotWrappedEntity() {
		final ObjectWrapperFactory<Person> personfact = createWrapperFactory(Person.class);
		personfact.markAsRemoved(new Person());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMarkRemovedNotEntity() {
		final ObjectWrapperFactory<String> personfact = createWrapperFactory(String.class);
		personfact.markAsRemoved("");
    }

	public void testDirtyDataOnGet() {
		testDirtyData(new Function<Person, Void>() {
			@Override
			public Void apply(final Person person) {
				person.getAge();
				person.getName();
				person.getLastName();
				return null;
			}
		}, 0, 0);
	}


	@Test
	public void testDirtyDataOnSet() {
		testDirtyData(new Function<Person, Void>() {
			@Override
			public Void apply(final Person person) {
				person.setAge(5);
				person.setName("name");
				person.setLastName("surname");
				return null;
			}
		}, 1, 3);
	}

	@Test
	public void testDirtyDataOnSetAndGet() {
		testDirtyData(new Function<Person, Void>() {
			@Override
			public Void apply(final Person person) {
				person.setAge(person.getAge());
				return null;
			}
		}, 1, 1);
	}

	@Test
	public void testDirtyDataOnInvokationOfOtherMethods() {
		testDirtyData(new Function<Person, Void>() {
			@Override
			public Void apply(final Person person) {
				person.toString();
				person.hashCode();
				person.equals(null);
				return null;
			}
		}, 0, 0);
	}

	/**
	 * Defines test that creates required mock-ups, then creates object, wraps it and invokes
	 * {@code scenario} that is expected to invoke various methods of wrapped object.
	 * Then test verifies that number of updates of {@link DirtyEntityManager} is as expected.
	 *
	 * @param scenario
	 * @param expectedDirtyEntityManagerCalls
	 */
	private void testDirtyData(final Function<Person, Void> scenario, final int expectedNumberOfDirtyEntities, final int expectedDirtyEntityManagerCalls) {
		final DirtyEntityManager dirtyEntityManager = getDirtyEntityManager();
		doReturn(dirtyEntityManager).when(ctx).getDirtyEntityManager();

		final Person person = new Person(30, "name", "surname", Gender.MALE);
		final Person personProxy = createWrapperFactory(Person.class).wrap(person);
		scenario.apply(personProxy);

		verifyDirtyEntityManager(
				dirtyEntityManager,
				expectedNumberOfDirtyEntities,
				expectedDirtyEntityManagerCalls,
				new BeanFieldMatcher<Person>(Person.class, "id", person.getId()));
	}


	/**
	 * Verifies that {@link DirtyEntityManager} was called expected number of times and that
	 * objects sent during calls match specified {@link Matcher}.
	 *
	 * <br/><br/>
	 *
	 * <i>
	 * Parameter  {@code expectedNumberOfDirtyEntities} is ignored here because it cannot be verified.
	 * It however is verified in sub class implementation {@link ObjectWrapperFactoryWithDirtyEntityManagerTest}.
	 * This is ugly but very simple design and IMHO good enough for tests.
	 * </i>
	 *
	 * @param dirtyEntityManager
	 * @param expectedNumberOfDirtyEntities
	 * @param expectedDirtyEntityManagerCalls
	 * @param matcher
	 *
	 * @see ObjectWrapperFactoryWithDirtyEntityManagerTest#verifyDirtyEntityManager(DirtyEntityManager, int, int, Matcher)
	 */
	@SuppressWarnings("unchecked")
	protected <T> void verifyDirtyEntityManager(final DirtyEntityManager dirtyEntityManager, final int expectedNumberOfDirtyEntities, final int expectedDirtyEntityManagerCalls, final Matcher<T> matcher) {
		// this line produces warning because class FieldMetadata is generic,
		// however it is impossible to create something like Class<Person, Object, Object>.class
		verify(dirtyEntityManager, times(expectedDirtyEntityManagerCalls)).
			addDirtyEntity(
					argThat(matcher),
					any(FieldMetadata.class));
	}

	protected <T> EntityMetadata<T> createEntityMetadata(final Class<T> clazz) {
		return new BeanMetadataFactory().create(clazz);
	}


    private <T> ObjectWrapperFactory<T> createWrapperFactory(final Class<T> clazz) {
        final EntityMetadata<T> emd = createEntityMetadata(clazz);
        doReturn(emd).when(ctx).getEntityMetadata(clazz);
        return createWrapperFactory(ctx);
    }

    @SuppressWarnings("unchecked")
	private <T> ObjectWrapperFactory<T> createWrapperFactory(final EntityContext ctx) {
        try {
			return (ObjectWrapperFactory<T>)constructor.newInstance(ctx);
		} catch (final ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
    }

    protected DirtyEntityManager getDirtyEntityManager() {
		final DirtyEntityManager dirtyEntityManager = mock(DirtyEntityManager.class);
		doReturn(dirtyEntityManager).when(ctx).getDirtyEntityManager();
    	return dirtyEntityManager;
    }
}
