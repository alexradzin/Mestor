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

package org.mestor.persistence.query;

import javax.persistence.Parameter;

public class ParameterImpl<T> implements Parameter<T> {
	private final String name;
	private final Integer position;
	private final Class<T> type;


	public ParameterImpl(String name, Class<T> type) {
		this(name, null, type);
	}
	
	public ParameterImpl(int position, Class<T> type) {
		this(null, position, type);
	}
	
	public ParameterImpl(String name, int position, Class<T> type) {
		this(name, new Integer(position), type);
	}
	
	private ParameterImpl(String name, Integer position, Class<T> type) {
		this.name = name;
		this.position = position;
		this.type = type;
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public Integer getPosition() {
		return position;
	}

	@Override
	public Class<T> getParameterType() {
		return type;
	}

}
