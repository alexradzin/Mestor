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

package org.mestor.persistence.idgenerator;

import java.util.Iterator;

abstract class SimpleSequence<E> implements Iterator<E> {
	private boolean removed = false;

	@Override
	public final E next() {
		try {
			return nextImpl();
		} finally {
			removed = false;

		}
	}

	@Override
	public final void remove() {
		if (removed) {
			throw new IllegalStateException("Cannot remove 2 sequential elements");
		}

		next();

		removed = true;
	}

	protected abstract E nextImpl();
}