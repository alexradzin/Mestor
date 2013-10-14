package org.mestor.entities.index_test;

import javax.persistence.Entity;

import org.eclipse.persistence.annotations.Index;

@Entity
public class OneColumnFieldLevelIndex extends EntityBase {

	@Index(name="field_index1")
	private String column1;
}
