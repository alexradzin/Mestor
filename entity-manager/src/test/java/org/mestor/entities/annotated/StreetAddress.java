/******************************************************************************************************/
/*                                                                                                    */
/*    Infinidat Ltd.  -  Proprietary and Confidential Material                                        */
/*                                                                                                    */
/*    Copyright (C) 2013, Infinidat Ltd. - All Rights Reserved                                        */
/*                                                                                                    */
/*    NOTICE: All information contained herein is, and remains the property of Infinidat Ltd.         */
/*    All information contained herein is protected by trade secret or copyright law.                 */
/*    The intellectual and technical concepts contained herein are proprietary to Infinidat Ltd.,     */
/*    and may be protected by U.S. and Foreign Patents, or patents in progress.                       */
/*                                                                                                    */
/*    Redistribution or use, in source or binary forms, with or without modification,                 */
/*    are strictly forbidden unless prior written permission is obtained from Infinidat Ltd.          */
/*                                                                                                    */
/*                                                                                                    */
/******************************************************************************************************/

package org.mestor.entities.annotated;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.mestor.entities.Country;

@Entity
@Table(name = "address")
public class StreetAddress extends Address {
	@Column(name = "number")
	private String streetNumber;
	private String street;
	private String city;
	@Column(name = "zipcode")
	private int zip;
	private Country country;

	public StreetAddress() {
		//default constructor
	}

	public StreetAddress(final int id, final String streetNumber, final String street, final String city, final Country country) {
		this.identifier = id;
		this.streetNumber = streetNumber;
		this.street = street;
		this.city = city;
		this.country = country;
	}

	public String getStreetNumber() {
		return streetNumber;
	}
	public void setStreetNumber(final String streetNumber) {
		this.streetNumber = streetNumber;
	}
	public String getCity() {
		return city;
	}

	public void setCity(final String city) {
		this.city = city;
	}

	public String getStreet() {
		return street;
	}
	public void setStreet(final String street) {
		this.street = street;
	}
	public int getZip() {
		return zip;
	}
	public void setZip(final int zip) {
		this.zip = zip;
	}
	public Country getCountry() {
		return country;
	}
	public void setCountry(final Country country) {
		this.country = country;
	}


}
