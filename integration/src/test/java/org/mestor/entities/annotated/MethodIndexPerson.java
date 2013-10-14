package org.mestor.entities.annotated;

import javax.persistence.Entity;

import org.eclipse.persistence.annotations.Index;

@Entity
public class MethodIndexPerson extends PersonBase {
	
	private String column;
	
	private String index2Col1;
	private String index2Col2;
	
	@Index(name = "index")
	public String getColumn() {
		return column;
	}
	
	public void setColumn(String column) {
		this.column = column;
	}
	
	@Index(name = "index2")
	public String getIndex2Col1() {
		return index2Col1;
	}
	
	public void setIndex2Col1(String index2Col1) {
		this.index2Col1 = index2Col1;
	}
	
	@Index(name = "index2")
	public String getIndex2Col2() {
		return index2Col2;
	}
	
	public void setIndex2Col2(String index2Col2) {
		this.index2Col2 = index2Col2;
	}
}
