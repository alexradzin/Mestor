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

package org.mestor.entities.queries;

import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import org.mestor.entities.annotated.AbstractEntity;

@Entity
@NamedQueries({ 
	@NamedQuery(name = "selectSorted", query = "SELECT OBJECT(e) FROM NamedQueriesEntity e ORDER BY e.identifier ASC"),
	@NamedQuery(name = "selectSorted", query = "SELECT OBJECT(e) FROM NamedQueriesEntity e where e.identifier > ? ORDER BY e.identifier ASC")
})
public class DuplicateNamedQueriesEntity extends AbstractEntity {
	private String name;

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}
}
