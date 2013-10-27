package org.mestor.benchmarks.entities;

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

import java.util.Date;
import java.util.Random;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

@Entity
@Cacheable(value = false)
public class ObjectWithRandomBytesArray extends IdObject {
	
	@Transient
	private transient static Random r = new Random();
	
	@Column
	private String name;

	@Column
	@Temporal(TemporalType.TIMESTAMP)
	private Date date;
	
	@Column
	private byte[] bytes;

	public ObjectWithRandomBytesArray(final long id) {
		super(id);
		this.date = new Date();
		this.name = this.id.toString();
		this.bytes = new byte[1000];
		r.nextBytes(bytes);
	}

	public ObjectWithRandomBytesArray() {
		super();
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(final Date date) {
		this.date = date;
	}
}
