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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;

import java.lang.reflect.Constructor;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.mestor.context.DirtyEntityManager;
import org.mestor.context.Persistor;
import org.mestor.entities.Person;
import org.mestor.metadata.EntityMetadata;
import org.mestor.metadata.ObjectWrapperFactoryTest;
import org.mestor.wrap.ObjectWrapperFactory;
import org.mockito.Mock;

public class ObjectWrapperFactoryWithDirtyEntityManagerTest<W extends ObjectWrapperFactory<?>> extends ObjectWrapperFactoryTest<W> {
	@Mock private Persistor persistor;
	@Mock private ObjectWrapperFactory<Person> owf;
	
	private DirtyEntityManager dem; 


    public ObjectWrapperFactoryWithDirtyEntityManagerTest(final String name, final Constructor<W> constructor) {
    	super(name, constructor); 
    }
    

	@Override
	@Before
	public void setUp() {
		super.setUp();
		doReturn(persistor).when(ctx).getPersistor();
		doReturn(owf).when(persistor).getObjectWrapperFactory(Person.class);
		
		dem = new EntityTransactionImpl(ctx);
		assertFalse(dem.getDirtyEntities().iterator().hasNext());
	}
    
    
    
    @Override
	protected DirtyEntityManager getDirtyEntityManager() {
    	return dem;
    }

	@Override
	protected <T> EntityMetadata<T> createEntityMetadata(Class<T> clazz) {
		EntityMetadata<T> emd = super.createEntityMetadata(clazz);
		emd.setPrimaryKey(emd.getFieldByName("id"));
		return emd;
	}

	/**
	 * Verifies that {@link DirtyEntityManager} contains expected number of objects and that objects 
	 * match specified {@link Matcher}.
	 *  
	 * <br/><br/>
	 * 
	 * <i>
	 * Parameter  {@code expectedDirtyEntityManagerCalls} is ignored here because it cannot be verified. 
	 * It however is verified in super class implementation {@link ObjectWrapperFactoryTest}. 
	 * This is ugly but very simple design and IMHO good enough for tests.
	 * </i>
	 * 
	 * @param dirtyEntityManager
	 * @param expectedNumberOfDirtyEntities
	 * @param expectedDirtyEntityManagerCalls
	 * @param matcher
	 * 
	 * @see ObjectWrapperFactoryTest#verifyDirtyEntityManager(DirtyEntityManager, int, int, Matcher)
	 */
	@Override
	protected <T> void verifyDirtyEntityManager(DirtyEntityManager dirtyEntityManager, int expectedNumberOfDirtyEntities, int expectedDirtyEntityManagerCalls, Matcher<T> matcher) {
		int count = 0;
		for (Object entity : dirtyEntityManager.getDirtyEntities()) {
			assertTrue(matcher.matches(entity));
			count++;
		}
		assertEquals(expectedNumberOfDirtyEntities, count);
	}
	
}
