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

import java.util.NoSuchElementException;

public class SimpleNumericSequence extends SimpleSequence<Long> {
	private long value;
	private final long step;


	public SimpleNumericSequence() {
		this(1, 1);
	}


	public SimpleNumericSequence(long initialValue) {
		this(initialValue, 1);
	}


	public SimpleNumericSequence(long initialValue, long step) {
		this.value = initialValue;
		this.step = step;
	}

	@Override
	public boolean hasNext() {
		if (step > 0) {
			return value < Long.MAX_VALUE;
		} else if (step < 0) {
			return value > Long.MIN_VALUE;
		} else {
			return true;
		}
	}

	@Override
	protected Long nextImpl() {
		if (!hasNext()) {
			throw new NoSuchElementException();
		}
		long result = value;
		value += step;
		return result;
	}
}
