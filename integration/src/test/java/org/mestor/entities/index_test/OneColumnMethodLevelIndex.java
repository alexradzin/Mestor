package org.mestor.entities.index_test;

import javax.persistence.Entity;

import org.eclipse.persistence.annotations.Index;

@Entity
public class OneColumnMethodLevelIndex extends IndexBase {

	@Override
	@Index(name = "method_index1")
	public String getColumn1() {
		return super.getColumn1();
	}
}
