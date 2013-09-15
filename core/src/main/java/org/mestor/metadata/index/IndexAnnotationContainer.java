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

package org.mestor.metadata.index;

import java.lang.annotation.ElementType;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "index-container")
@XmlAccessorType(XmlAccessType.FIELD)
public class IndexAnnotationContainer {
	@XmlElement(name = "class")
	private String className;
	@XmlElement(defaultValue = "value")
	private String collection;
	@XmlElement(name = "index-class")
	private String indexAnnotationClassName;
	@XmlElement(name = "target")
	@XmlElementWrapper(name = "targets")
	private ElementType[] targets = new ElementType[] {ElementType.TYPE};
	
	
	public String getClassName() {
		return className;
	}
	public void setClassName(String className) {
		this.className = className;
	}
	public String getCollection() {
		return collection;
	}
	public void setCollection(String collection) {
		this.collection = collection;
	}
	public String getIndexAnnotationClassName() {
		return indexAnnotationClassName;
	}
	public void setIndexAnnotationClassName(String indexAnnotationClassName) {
		this.indexAnnotationClassName = indexAnnotationClassName;
	}
	public ElementType[] getTargets() {
		return targets;
	}
	public void setTargets(ElementType ... targets) {
		this.targets = targets;
	}
	
	
	
}
