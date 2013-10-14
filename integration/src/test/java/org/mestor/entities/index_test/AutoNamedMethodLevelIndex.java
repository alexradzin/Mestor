package org.mestor.entities.index_test;

import javax.persistence.Entity;

import org.eclipse.persistence.annotations.Index;

@Entity
public class AutoNamedMethodLevelIndex extends EntityBase {

	private String method_column1;

	@Index
	public String getMethod_column1(){
		return method_column1;
	}


}
