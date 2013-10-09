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

import java.util.Map;

import org.mestor.context.Persistor;
import org.mestor.metadata.EntityMetadata;
import org.mestor.wrap.ObjectWrapperFactory;

public class DummyPersistor implements Persistor {

	@Override
	public <T> void store(T entity) {
		// TODO Auto-generated method stub

	}

	@Override
	public <T, P> T fetch(Class<T> entityClass, P primaryKey) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> void remove(T entity) {
		// TODO Auto-generated method stub

	}

	@Override
	public <T> boolean exists(Class<T> entityClass, Object primaryKey) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public <T> Object[] fetchProperty(T entity, String... propertyNames) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> ObjectWrapperFactory<T> getObjectWrapperFactory(Class<T> type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void createSchema(String name, Map<String, Object> properties) {
		// TODO Auto-generated method stub

	}

	@Override
	public void dropSchema(String name) {
		// TODO Auto-generated method stub

	}

	@Override
	public Iterable<String> getSchemaNames() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void useSchema(String name) {
		// TODO Auto-generated method stub

	}

	@Override
	public <E> void createTable(EntityMetadata<E> entityMetadata,
			Map<String, Object> properties) {
		// TODO Auto-generated method stub

	}

	@Override
	public <E> void updateTable(EntityMetadata<E> entityMetadata,
			Map<String, Object> properties) {
		// TODO Auto-generated method stub

	}

	@Override
	public <E> void validateTable(EntityMetadata<E> entityMetadata,
			Map<String, Object> properties) {
		// TODO Auto-generated method stub

	}

	@Override
	public <E> void dropTable(String keyspace, String tableName) {
		// TODO Auto-generated method stub

	}

	@Override
	public Iterable<String> getTableNames(String keyspace) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T, P> T fetch(Class<T> entityClass, Map<String, Object> criteria) {
		// TODO Auto-generated method stub
		return null;
	}


}
