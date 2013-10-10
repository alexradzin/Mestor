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
import java.util.Arrays;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

/**
 * @deprecated This class should be removed or transformed into unit test
 * @author alexr
 *
 */
@Deprecated
public class TryIndexAnnotationInit {

	/**
	 * @param args
	 * @throws JAXBException 
	 */
	public static void main(String[] args) throws JAXBException {
		IndexAnnotation eclipseLinkIndex = new IndexAnnotation();
		eclipseLinkIndex.setClassName("org.eclipse.persistence.annotations.Index");
		eclipseLinkIndex.setColumnNames("columnNames");
		eclipseLinkIndex.setName("name");
		
		
		IndexAnnotationContainer eclipseLinkIndexes = new IndexAnnotationContainer();
		eclipseLinkIndexes.setClassName("org.eclipse.persistence.annotations.Indexes");
		eclipseLinkIndexes.setIndexAnnotationClassName("org.eclipse.persistence.annotations.Index");


		IndexAnnotation hibernateIndex = new IndexAnnotation();
		hibernateIndex.setClassName("org.hibernate.annotations.Index");
		hibernateIndex.setColumnNames("columnNames");
		hibernateIndex.setName("name");
		
		IndexAnnotationContainer hibernateIndexes = new IndexAnnotationContainer();
		hibernateIndexes.setClassName("org.hibernate.annotations.Table");
		hibernateIndexes.setIndexAnnotationClassName("org.hibernate.annotations.Index");
		hibernateIndexes.setCollection("indexes");
		hibernateIndexes.setTargets(ElementType.TYPE);

		
		//javax.jdo.annotations
		IndexAnnotation jdoIndex = new IndexAnnotation();
		jdoIndex.setClassName("javax.jdo.annotations.Index");
		jdoIndex.setColumnNames("members");
		jdoIndex.setName("name");
		
		IndexAnnotationContainer jdoIndexes = new IndexAnnotationContainer();
		jdoIndexes.setClassName("javax.jdo.annotations.Indices");
		jdoIndexes.setIndexAnnotationClassName("javax.jdo.annotations.Index");
		jdoIndexes.setTargets(ElementType.TYPE);
		
		
		IndexAnnotations root = new IndexAnnotations();
		root.setIndexDefs(Arrays.asList(eclipseLinkIndex, eclipseLinkIndexes, hibernateIndex, hibernateIndexes, jdoIndex, jdoIndexes));
		
		
		JAXBContext jaxbContext = JAXBContext.newInstance("org.mestor.metadata.index");
		Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
 
		// output pretty printed
		jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
 
		jaxbMarshaller.marshal(root, System.out);		
	}
	
	
	

}
