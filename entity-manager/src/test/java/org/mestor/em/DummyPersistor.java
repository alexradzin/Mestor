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

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.mestor.context.Persistor;
import org.mestor.metadata.EntityMetadata;
import org.mestor.query.QueryInfo;
import org.mestor.wrap.ObjectWrapperFactory;

public class DummyPersistor implements Persistor {
	private final static Collection<Class<?>> nativeTypes = Arrays.<Class<?>>asList(
			short.class, int.class, long.class, char.class, byte.class,
			Short.class, Integer.class, Long.class, Character.class, Byte.class,
			String.class
	);



	@Override
	public <T> void store(final T entity) {
		// TODO Auto-generated method stub

	}

	@Override
	public <T, P> T fetch(final Class<T> entityClass, final P primaryKey) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> void remove(final T entity) {
		// TODO Auto-generated method stub

	}

	@Override
	public <T> boolean exists(final Class<T> entityClass, final Object primaryKey) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public <T> Object[] fetchProperty(final T entity, final String... propertyNames) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> ObjectWrapperFactory<T> getObjectWrapperFactory(final Class<T> type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void createSchema(final String name, final Map<String, Object> properties) {
		// TODO Auto-generated method stub

	}

	@Override
	public void dropSchema(final String name) {
		// TODO Auto-generated method stub

	}

	@Override
	public Iterable<String> getSchemaNames() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void useSchema(final String name) {
		// TODO Auto-generated method stub

	}

	@Override
	public <E> void createTable(final EntityMetadata<E> entityMetadata,
			final Map<String, Object> properties) {
		// TODO Auto-generated method stub

	}

	@Override
	public <E> void updateTable(final EntityMetadata<E> entityMetadata,
			final Map<String, Object> properties) {
		// TODO Auto-generated method stub

	}

	@Override
	public <E> void validateTable(final EntityMetadata<E> entityMetadata,
			final Map<String, Object> properties) {
		// TODO Auto-generated method stub

	}

	@Override
	public <E> void dropTable(final String keyspace, final String tableName) {
		// TODO Auto-generated method stub

	}

	@Override
	public Iterable<String> getTableNames(final String keyspace) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T, P> T fetch(final Class<T> entityClass, final Map<String, Object> criteria) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> List<T> selectQuery(final QueryInfo queryInfo, final Map<String, Object> parameterValues) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public Collection<Class<?>> getNativeTypes() {
		return nativeTypes;
	}

	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub

	}

}
