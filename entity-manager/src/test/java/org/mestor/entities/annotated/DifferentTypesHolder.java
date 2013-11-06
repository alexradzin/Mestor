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

package org.mestor.entities.annotated;

import java.util.List;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class DifferentTypesHolder {
	@Id
	private int id;
	private String name;
	private Map<String, String> properties;
	private List<Integer> integers;


	public int getId() {
		return id;
	}
	public void setId(final int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(final String name) {
		this.name = name;
	}
	public Map<String, String> getProperties() {
		return properties;
	}
	public void setProperties(final Map<String, String> properties) {
		this.properties = properties;
	}
	public List<Integer> getIntegers() {
		return integers;
	}
	public void setIntegers(final List<Integer> integers) {
		this.integers = integers;
	}


}
