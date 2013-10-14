package org.mestor.entities.annotated;

import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public class PersonBase {
	@Id
	protected int identifier;
	
	private String name;
	private String lastName;
	private int age;

	public PersonBase() {
		// default empty constructor
	}

	// convenience constructor
	public PersonBase(String name, String lastName) {
		this.name = name;
		this.lastName = lastName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getAge() {
		return age;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public void setAge(int age) {
		this.age = age;
	}
}
