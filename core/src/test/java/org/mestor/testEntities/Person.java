package org.mestor.testEntities;

import static com.google.common.base.Objects.equal;

import java.util.List;
import java.util.Map;

import com.google.common.base.Objects;

public class Person extends AbstractEntity {
	private int id;

	// 2 ways to create composite primary key.
	// 1st way: fields are here and corresponding fields are in IdClass (Passport) 
	private Country country;
	private String passportId;
	
	// 2nd way: hold here field of type Passport directly.
	private Passport passport;
	
	private String name;
	private String lastName;
	private int age;
	private Gender gender;
	private Person spouse; 
	private List<Person> children;
	private Map<ParentRole, Person> parents;
	
	public static enum Gender {
		MALE, FEMALE;
	}
	
	public static enum ParentRole {
		MOTHER, FATHER;
	}
	
	
	public Person() {
		// default empty constructor
	}
	
	// convenience constructor 
	public Person(int id, String name, String lastName, Gender gender) {
		this.id = id;
		this.name = name;
		this.lastName = lastName;
		this.gender = gender;
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
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
	
	
	
	public Person getSpouse() {
		return spouse;
	}
	public void setSpouse(Person spouse) {
		this.spouse = spouse;
	}
	public List<Person> getChildren() {
		return children;
	}
	public void setChildren(List<Person> children) {
		this.children = children;
	}
	public Map<ParentRole, Person> getParents() {
		return parents;
	}
	public void setParents(Map<ParentRole, Person> parents) {
		this.parents = parents;
	}
	public Country getCountry() {
		return country;
	}
	public void setCountry(Country country) {
		this.country = country;
	}

	public String getPassportId() {
		return passportId;
	}

	public void setPassportId(String passportId) {
		this.passportId = passportId;
	}

	public Passport getPassport() {
		return passport;
	}

	public void setPassport(Passport passport) {
		this.passport = passport;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Person)) {
			return false;
		}
		Person other = (Person)obj;
		return 
				equal(id, other.id) && 
				equal(name, other.name) && 
				equal(lastName, other.lastName) && 
				equal(age, other.age);   
	}
	
	@Override
	public int hashCode() {
		return Objects.hashCode(id, name, lastName, age);
	}
}
