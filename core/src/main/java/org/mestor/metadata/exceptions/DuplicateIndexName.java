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
package org.mestor.metadata.exceptions;

@SuppressWarnings("serial")
public final class DuplicateIndexName extends IllegalArgumentException {
	private final String indexName;
	private final Class<?> entityClass;
	
	public DuplicateIndexName(Class<?> entityClass, String indexName){
		super("Duplicate indexes for class: " + entityClass.getName() + " (" + indexName + ")");
		this.indexName = indexName;
		this.entityClass = entityClass;
	}
	
	public String getIndexName() {
		return indexName;
	}
	
	public Class<?> getEntityClass() {
		return entityClass;
	}
	
}
