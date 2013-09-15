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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;

import org.mestor.context.EntityContext;
import org.mestor.context.Persistor;
import org.mestor.em.SchemaViolationHandler.ViolationLevel;
import org.mestor.metadata.EntityMetadata;
import org.mestor.util.CollectionUtils;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Sets;

/**
 * The values are taken from Hibernate 
 * @author alexr
 */
public enum SchemaMode {
	/**
	* try to create the schema (DDL) when the session factory is started. When running with JPA, it happens when the EJB module is being deployed.
	*/
	CREATE {
		@Override
		public void init(EntityContext ctx) {
			dropSchema(ctx);
			createSchema(ctx);
		}
	},
	/**
	 * like 'create', but also try to destroy the schema when the factory is destroyed (with JPA - when the EJB module is undeployed)
	 */
	CREATE_DROP {
		@Override
		public void init(EntityContext ctx) {
			CREATE.init(ctx);
		}
		
		@Override
		public void destroy(EntityContext ctx) {
			dropSchema(ctx);
		}
	},
	/**
	 * just ensure the schema is as expected based on the mapping files
	 */
	VALIDATE {
		@Override
		public void init(EntityContext ctx) {
			validateSchema(ctx);
		}
	},
	
	/**
	 * try to make incremental changes to the existing schema if it does not match the expected schema.
	 */
	UPDATE {
		@Override
		public void init(EntityContext ctx) {
			updateSchema(ctx);
		}
	},
	
	NONE {
		@Override
		public void init(EntityContext ctx) {
			// do nothing. None mean nothing.
		}
	},
	;
	
	private final static String CASSANDRA_KEYSPACE_PROPERTY = "cassandra.keyspace";
	
	private final String property;
	
	private SchemaMode() {
		property = name().toLowerCase().replace('_', '-');
	}
	
	
	public String property() {
		return property;
	}
	
	public static SchemaMode forProperty(String property) {
		return SchemaMode.valueOf(property.toUpperCase().replace('-', '_'));
	}
	
	public abstract void init(EntityContext ctx);
	public void destroy(EntityContext ctx) {
		// Empty implementation. Most modes do not destroy entity context.
	}
	
	protected void dropSchema(EntityContext ctx) {
		Persistor persistor = ctx.getPersistor();
		Set<String> existingSchemas = Sets.newHashSet(persistor.getSchemaNames());
		
		for (String schema : getEntitySchemas(ctx)) {
			if (existingSchemas.contains(schema)) {
				persistor.dropSchema(schema);
			}
		}
	}
	
	protected void createSchema(EntityContext ctx) {
		Persistor persistor = ctx.getPersistor();
		
		for (String schema : getEntitySchemas(ctx)) {
			persistor.createSchema(schema, CollectionUtils.subMap(ctx.getProperties(), CASSANDRA_KEYSPACE_PROPERTY));
		}

		for (EntityMetadata<?> emd : ctx.getEntityMetadata()) {
			persistor.createTable(emd, null);
		}
	}

	protected void updateSchema(EntityContext ctx) {
		Persistor persistor = ctx.getPersistor();
		Set<String> existingSchemas = Sets.newHashSet(persistor.getSchemaNames());

		
		for (String schema : getEntitySchemas(ctx)) {
			if (!existingSchemas.contains(schema)) {
				persistor.createSchema(schema, CollectionUtils.subMap(ctx.getProperties(), CASSANDRA_KEYSPACE_PROPERTY));
			} else {
				// TODO: update schema: alter keyspace is not currently supported by persistor.
			}

			Set<String> existingTables = Sets.newHashSet(ctx.getPersistor().getTableNames(schema));
			
			for (EntityMetadata<?> emd : ctx.getEntityMetadata()) {
				if (!schema.equals(emd.getSchemaName())) {
					continue;
				}
				if (!existingTables.contains(emd.getTableName())) {
					persistor.createTable(emd, null);
				} else {
					persistor.updateTable(emd, null);
				}
			}
		}
	}
	
	protected void validateSchema(EntityContext ctx) {
		SchemaViolationHandler schemaViolationHandler = new DefaultSchemaViolationHandler();
		
		
		Persistor persistor = ctx.getPersistor();
		Set<String> existingSchemas = Sets.newHashSet(persistor.getSchemaNames());

		
		for (String schema : getEntitySchemas(ctx)) {
			if (!existingSchemas.contains(schema)) {
				schemaViolationHandler.handle(ViolationLevel.FATAL, "Schema " + schema + " does not exist");
			} else {
				// TODO: validate schema: alter keyspace is not currently supported by persistor.
			}

			Set<String> existingTables = Sets.newHashSet(ctx.getPersistor().getTableNames(schema));
			
			for (EntityMetadata<?> emd : ctx.getEntityMetadata()) {
				if (!schema.equals(emd.getSchemaName())) {
					continue;
				}
				String table = emd.getTableName();
				if (!existingTables.contains(table)) {
					schemaViolationHandler.handle(ViolationLevel.FATAL, "Table " + table + " does not exist");
				} else {
					persistor.validateTable(emd, null);
				}
			}
		}
	}
	
	
	protected Collection<String> getEntitySchemas(EntityContext ctx) {
		return new HashSet<String>(Collections2.transform(ctx.getEntityMetadata(), new Function<EntityMetadata<?>, String>() {
			@Override
			public String apply(@Nullable EntityMetadata<?> emd) {
				return emd.getSchemaName();
			}
		}));
	}
	
}
