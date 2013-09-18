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

package org.mestor.persistence.cql;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class InnerCollections {
	private int id;
	private String[] stringArray;
	private List<String> stringList;
	private Set<String> stringSet;
	private Map<String, Integer> stringToIntegerMap;
	
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String[] getStringArray() {
		return stringArray;
	}
	public void setStringArray(String[] stringArray) {
		this.stringArray = stringArray;
	}
	public List<String> getStringList() {
		return stringList;
	}
	public void setStringList(List<String> stringList) {
		this.stringList = stringList;
	}
	public Set<String> getStringSet() {
		return stringSet;
	}
	public void setStringSet(Set<String> stringSet) {
		this.stringSet = stringSet;
	}
	public Map<String, Integer> getStringToIntegerMap() {
		return stringToIntegerMap;
	}
	public void setStringToIntegerMap(Map<String, Integer> stringToIntegerMap) {
		this.stringToIntegerMap = stringToIntegerMap;
	}
	
	
}
