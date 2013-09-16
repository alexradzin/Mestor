package org.mestor.context;

import java.util.Map;

import org.mestor.metadata.EntityMetadata;
import org.mestor.wrap.ObjectWrapperFactory;

public interface Persistor {
	public <T> void store(T entity);
	public <T, P> T fetch(Class<T> entityClass, P primaryKey);
	public <T> void remove(T entity);
	public <T> boolean exists(Class<T> entityClass, Object primaryKey);
	
	public <T> Object[] fetchProperty(T entity, String ... propertyNames);
	
	public <T> ObjectWrapperFactory<T> getObjectWrapperFactory();
	
	public void createSchema(String name, Map<String, Object> properties);
	public void dropSchema(String name);
	public Iterable<String> getSchemaNames();
	public void useSchema(String name);
	
	public <E> void createTable(EntityMetadata<E> entityMetadata, Map<String, Object> properties);
	public <E> void updateTable(EntityMetadata<E> entityMetadata, Map<String, Object> properties);
	public <E> void validateTable(EntityMetadata<E> entityMetadata, Map<String, Object> properties);
	public <E> void dropTable(String keyspace, String tableName);
	public Iterable<String> getTableNames(String keyspace);
}
