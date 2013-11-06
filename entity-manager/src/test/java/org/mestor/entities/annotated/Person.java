package org.mestor.entities.annotated;

import static com.google.common.base.Objects.equal;

import java.util.List;

import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.mestor.metadata.jpa.conversion.EnumNameConverter;

import com.google.common.base.Objects;

@Entity
@Table(indexes = {
		@Index(name = "name", columnList = "name"),
		@Index(name = "age", columnList = "age"),
		@Index(name = "full_name", columnList = "name,last_name") })
public class Person extends AbstractEntity {
	private final static String identifier = "THIS IS A STATIC FIELD THAT MUST BE IGNORED WHEN CREATING CLASS METADATA";

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
	public Person(final String name, final String lastName, final Gender gender) {
		this.name = name;
		this.lastName = lastName;
		this.gender = gender;
	}

	public String getName() {
		return name;
	}
	public void setName(final String name) {
		this.name = name;
	}
	public int getAge() {
		return age;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(final String lastName) {
		this.lastName = lastName;
	}
	public void setAge(final int age) {
		this.age = age;
	}

	public Gender getGender() {
		return gender;
	}
	public void setGender(final Gender gender) {
		this.gender = gender;
	}




	@Override
	public boolean equals(final Object obj) {
		if (!(obj instanceof Person)) {
			return false;
		}
		final Person other = (Person)obj;
		return
				equal(super.identifier, other.getIdentifier()) &&
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

	public void setAddresses(final List<Address> addresses) {
		this.addresses = addresses;
	}

	public List<User> getAccounts() {
		return accounts;
	}

	public void setAccounts(final List<User> accounts) {
		this.accounts = accounts;
	}
}
