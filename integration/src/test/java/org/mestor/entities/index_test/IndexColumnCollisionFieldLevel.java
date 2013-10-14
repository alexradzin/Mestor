package org.mestor.entities.index_test;

import javax.persistence.Entity;

import org.eclipse.persistence.annotations.Index;

@Entity
public class IndexColumnCollisionFieldLevel extends EntityBase {

	@Index(name="field_index1", columnNames="ignored_column")
	private String column1;
}
