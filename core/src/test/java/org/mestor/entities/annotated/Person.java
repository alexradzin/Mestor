package org.mestor.entities.annotated;

import static com.google.common.base.Objects.equal;

import java.util.List;

import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;

import org.mestor.metadata.jpa.conversion.EnumNameConverter;

import com.google.common.base.Objects;

@Entity
public class Person extends AbstractEntity {
	private String name;
	private String lastName;
	private int age;
	@Convert(converter = EnumNameConverter.class)
	private Gender gender;
	@ManyToMany
	private List<Address> addresses;
	@OneToMany
	private List<User> accounts;
	
	
	public static enum Gender {
		MALE, FEMALE;
	}
	
	
	public Person() {
		// default empty constructor
	}
	
	// convenience constructor 
	public Person(String name, String lastName, Gender gender) {
		this.name = name;
		this.lastName = lastName;
		this.gender = gender;
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
	
	public Gender getGender() {
		return gender;
	}
	public void setGender(Gender gender) {
		this.gender = gender;
	}
	
	
	

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Person)) {
			return false;
		}
		Person other = (Person)obj;
		return 
				equal(identifier, other.identifier) && 
				equal(name, other.name) && 
				equal(lastName, other.lastName) && 
				equal(age, other.age);   
	}
	
	@Override
	public int hashCode() {
		return Objects.hashCode(identifier, name, lastName, age);
	}

	public List<Address> getAddresses() {
		return addresses;
	}

	public void setAddresses(List<Address> addresses) {
		this.addresses = addresses;
	}

	public List<User> getAccounts() {
		return accounts;
	}

	public void setAccounts(List<User> accounts) {
		this.accounts = accounts;
	}
}
