package org.mestor.entities;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import org.mestor.entities.index_test.EntityBase;

@MappedSuperclass
public class Human extends EntityBase {

	@Column(nullable = false)
	protected String name;

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

}
