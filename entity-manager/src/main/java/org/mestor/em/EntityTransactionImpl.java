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

import java.util.HashSet;
import java.util.Set;

import javax.persistence.EntityTransaction;
import javax.persistence.RollbackException;

import org.mestor.context.DirtyEntityManager;
import org.mestor.context.Persistor;


public class EntityTransactionImpl implements EntityTransaction, DirtyEntityManager {
    private boolean active = false;
    private boolean rollbackOnly = false;
    
    private final Persistor persistor;
    
    private final static ThreadLocal<EntityTransaction> entityTransactions = new ThreadLocal<EntityTransaction>();
    private Set<Object> dirtyEntities = new HashSet<Object>();
    


    public static EntityTransaction getTransaction(Persistor persistor) {
    	EntityTransaction transaction = entityTransactions.get();
    	if(transaction == null) {
    		transaction = new EntityTransactionImpl(persistor);    		
    		entityTransactions.set(transaction);
    	}
    	return transaction;
    }
    
    
    
    public static DirtyEntityManager getDirtyEntityManager() {
    	return (DirtyEntityManager)entityTransactions.get();
    }
    
    
    private EntityTransactionImpl(Persistor persistor) {
    	this.persistor = persistor;
    }


	@Override
	public void begin() {
		checkIsActive();        
        active = true;
        //persistor.beginTransaction();
	}

	@Override
	public void commit() {
		try {
			commitInternal();
		} catch (RuntimeException e) {
			throw new javax.persistence.RollbackException(e);
		} finally {
			entityTransactions.remove();
		}
	}
	
	protected void commitInternal() {
		checkIsActive();        
		try {
			for (Object dirty : getDirtyEntities()) {
				persistor.store(dirty);
			}
		} catch (RuntimeException ex) {
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
		checkIsActive();
		try {
			getTransaction(persistor).rollback();
		} finally {
			this.active = false;
			this.rollbackOnly = false;
			entityTransactions.remove();
		}

	}

	@Override
	public void setRollbackOnly() {
		checkIsActive();        
		rollbackOnly = true;
	}

	@Override
	public boolean getRollbackOnly() {
		checkIsActive();        
		return rollbackOnly;
	}

	@Override
	public boolean isActive() {
		return active;
	}

	private void checkIsActive() {
		if (isActive()) {
			throw new IllegalStateException("Transaction is not active");
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
	public <E> void addDirtyEntity(E entity) {
		dirtyEntities.add(entity);
	}



	@Override
	public <E> void removeDirtyEntity(E entity) {
		dirtyEntities.remove(entity);	
	}



	@SuppressWarnings("unchecked")
	@Override
	public <E> Iterable<E> getDirtyEntities() {
		return (Iterable<E>)dirtyEntities;
	}
	
}
