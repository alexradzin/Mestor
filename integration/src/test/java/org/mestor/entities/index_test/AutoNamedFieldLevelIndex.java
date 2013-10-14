package org.mestor.entities.index_test;

import javax.persistence.Entity;

import org.eclipse.persistence.annotations.Index;

@Entity
public class AutoNamedFieldLevelIndex extends EntityBase {

	@Index
	private String field_column1;


}
