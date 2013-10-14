package org.mestor.entities.annotated;

import javax.persistence.Entity;

import org.eclipse.persistence.annotations.Index;

@Entity
public class FieldIndexedPerson extends PersonBase {

	@Index(name="simple")
	private String simpleIndex;

	@Index(name="complex")
	private String complexIndex1;
	@Index(name="complex")
	private String complexIndex2;

	public String getSimpleIndex() {
		return simpleIndex;
	}

	public void setSimpleIndex(String simpleIndex) {
		this.simpleIndex = simpleIndex;
	}

	public String getComplexIndex1() {
		return complexIndex1;
	}

	public void setComplexIndex1(String complexIndex1) {
		this.complexIndex1 = complexIndex1;
	}

	public String getComplexIndex2() {
		return complexIndex2;
	}

	public void setComplexIndex2(String complexIndex2) {
		this.complexIndex2 = complexIndex2;
	}

}
