package org.mestor.entities.index_test;

import javax.persistence.Entity;

import org.eclipse.persistence.annotations.Index;

@Entity
public class TwoColumnMethodLevelIndex extends IndexBase {

	@Override
	@Index(name = "method_index1")
	public String getColumn1() {
		return super.getColumn1();
	}
	
	@Override
	@Index(name = "method_index1")
	public String getColumn2() {
		return super.getColumn2();
	}
}
