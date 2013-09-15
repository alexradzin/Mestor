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

package org.mestor.context;

import java.util.Collection;
import java.util.Map;

import org.mestor.metadata.EntityMetadata;

public interface EntityContext {
	public Map<String, Object> getProperties();
	public Collection<EntityMetadata<?>> getEntityMetadata();
	public Collection<Class<?>> getEntityClasses();
	public <T> EntityMetadata<T> getEntityMetadata(Class<T> clazz);
	public Persistor getPersistor();
	public DirtyEntityManager getDirtyEntityManager();
}
