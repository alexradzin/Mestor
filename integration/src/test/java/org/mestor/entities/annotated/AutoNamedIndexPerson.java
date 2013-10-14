package org.mestor.entities.annotated;

import javax.persistence.Entity;

import org.eclipse.persistence.annotations.Index;

@Entity
public class AutoNamedIndexPerson extends PersonBase {

	@Index
	private String autoNamedIndex;

	public String getAutoNamedIndex() {
		return autoNamedIndex;
	}

	public void setAutoNamedIndex(String autoNamedIndex) {
		this.autoNamedIndex = autoNamedIndex;
	}

}
