package org.mestor.entities.annotated;

import javax.persistence.Entity;

import org.eclipse.persistence.annotations.Index;

@Entity
public class IndexColumnCollisionPerson extends PersonBase {
	
	@Index(name = "idx", columnNames = "name")
	private String indexColumn;
}
