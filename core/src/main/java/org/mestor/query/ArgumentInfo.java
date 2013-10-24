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

package org.mestor.query;

public class ArgumentInfo<T> {
	private final String name;
	private final Integer position;
	private final T value;


	public ArgumentInfo(final String name, final T value) {
		this(name, null, value);
	}

	public ArgumentInfo(final Integer position, final T value) {
		this(null, position, value);
	}

	public ArgumentInfo(final String name, final Integer position, final T value) {
		super();
		this.name = name;
		this.position = position;
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public Integer getPosition() {
		return position;
	}

	public T getValue() {
		return value;
	}
}
