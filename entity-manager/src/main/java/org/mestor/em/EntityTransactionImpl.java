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

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

import javax.persistence.EntityTransaction;
import javax.persistence.RollbackException;

import org.mestor.context.DirtyEntityManager;
import org.mestor.context.EntityContext;
import org.mestor.context.Persistor;
import org.mestor.metadata.EntityComparator;
import org.mestor.metadata.FieldMetadata;
import org.mestor.reflection.ClassAccessor;
import org.mestor.reflection.PropertyAccessor;


public class EntityTransactionImpl implements EntityTransaction, DirtyEntityManager {
    private boolean active = false;
    private boolean rollbackOnly = false;
    
    private final EntityContext context;
    private final Persistor persistor;
    
    private final static ThreadLocal<EntityTransaction> entityTransactions = new ThreadLocal<EntityTransaction>();
    

    private final Map<Object, Object> dirtyEntities;
    


    public static EntityTransaction getTransaction(final EntityContext context) {
    	EntityTransaction transaction = entityTransactions.get();
    	if(transaction == null) {
    		transaction = new EntityTransactionImpl(context);    		
    		entityTransactions.set(transaction);
    	}
    	return transaction;
    }
    
    
    
    public static DirtyEntityManager getDirtyEntityManager() {
    	return (DirtyEntityManager)entityTransactions.get();
    }
    
    // package protected access for tests
    EntityTransactionImpl(final EntityContext context) {
    	this.context = context;
    	this.persistor = context.getPersistor();
    	
    	final Comparator<Object> comparator= new EntityComparator<>(context);
    	dirtyEntities = new TreeMap<>(comparator);
    }


	@Override
	public void begin() {
		assertNotActive();
        active = true;
        //persistor.beginTransaction();
	}

	@Override
	public void commit() {
		try {
			commitInternal();
		} catch (final RuntimeException e) {
			throw new javax.persistence.RollbackException(e);
		} finally {
			entityTransactions.remove();
		}
	}
	
	protected void commitInternal() {
		assertIsActive();        
		try {
			for (final Object dirty : getDirtyEntities()) {
				persistor.store(dirty);
			}
		} catch (final RuntimeException ex) {
			if (!this.rollbackOnly) {
				throw new RollbackException(ex);
			} 
			// it's a RollbackException
			throw ex;
		} finally {
			this.active = false;
			this.rollbackOnly = false;
		}
	}	
	

	@Override
	public void rollback() {
		assertIsActive();
		try {
			getTransaction(context).rollback();
		} finally {
			this.active = false;
			this.rollbackOnly = false;
			entityTransactions.remove();
		}

	}

	@Override
	public void setRollbackOnly() {
		assertIsActive();        
		rollbackOnly = true;
	}

	@Override
	public boolean getRollbackOnly() {
		assertIsActive();        
		return rollbackOnly;
	}

	@Override
	public boolean isActive() {
		return active;
	}

	private void assertIsActive() {
		if (!isActive()) {
			throw new IllegalStateException("Transaction is not active");
		}
	}
	
	private void assertNotActive() {
		if (isActive()) {
			throw new IllegalStateException("Transaction is already active");
		}
	}
	
	@Override
	protected void finalize() throws Throwable {
		try {
			if (isActive()) {
				this.rollback();
			}
		} finally {
			entityTransactions.remove();
			super.finalize();
		}
	}



	@Override
	public <E> void addDirtyEntity(final E entity, final FieldMetadata<E, Object, Object> fmd) {
		@SuppressWarnings("unchecked")
		final E existingEntity = (E) dirtyEntities.get(entity);
		if (existingEntity != null) {
			final PropertyAccessor<E, Object> accessor = fmd.getAccessor();
			accessor.setValue(existingEntity, accessor.getValue(entity));
		} else {
			final Class<E> clazz = fmd.getClassType();
			final E newEntity = ClassAccessor.newInstance(clazz);
			context.getEntityMetadata(clazz).copy(entity, newEntity);
			dirtyEntities.put(newEntity, newEntity);
		}
	}


	@Override
	public <E> void removeDirtyEntity(final E entity) {
		dirtyEntities.remove(entity);	
	}



	@SuppressWarnings("unchecked")
	@Override
	public <E> Iterable<E> getDirtyEntities() {
		return (Iterable<E>)dirtyEntities.values();
	}
	
}
