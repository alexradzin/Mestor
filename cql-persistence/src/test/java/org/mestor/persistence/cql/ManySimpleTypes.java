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

package org.mestor.persistence.cql;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.UUID;


public class ManySimpleTypes {
	private int intPrimitive;
	private long longPrimitive;
	private float floatPrimitive;
	private double doublePrimitive; 

	private int intWrapper;
	private long longWrapper;
	private float floatWrapper;
	private double doubleWrapper; 
	
	private BigDecimal bigDecimal;
	private BigInteger bigInteger;
	
	private boolean booleanPrimitive;
	private Boolean booleanWrapper;
	
	// ASCII, TEXT==VARCHAR, currently using TEXT
	private String string;

	private ByteBuffer bytebuffer;
	private byte[] bytearray;
	
	//TODO counter
	
	private InetAddress inet;
	private Date date;

	// UUID, TIMEUUID - ?
	private UUID uuid;

	public int getIntPrimitive() {
		return intPrimitive;
	}

	public void setIntPrimitive(int intPrimitive) {
		this.intPrimitive = intPrimitive;
	}

	public long getLongPrimitive() {
		return longPrimitive;
	}

	public void setLongPrimitive(long longPrimitive) {
		this.longPrimitive = longPrimitive;
	}

	public float getFloatPrimitive() {
		return floatPrimitive;
	}

	public void setFloatPrimitive(float floatPrimitive) {
		this.floatPrimitive = floatPrimitive;
	}

	public double getDoublePrimitive() {
		return doublePrimitive;
	}

	public void setDoublePrimitive(double doublePrimitive) {
		this.doublePrimitive = doublePrimitive;
	}

	public int getIntWrapper() {
		return intWrapper;
	}

	public void setIntWrapper(int intWrapper) {
		this.intWrapper = intWrapper;
	}

	public long getLongWrapper() {
		return longWrapper;
	}

	public void setLongWrapper(long longWrapper) {
		this.longWrapper = longWrapper;
	}

	public float getFloatWrapper() {
		return floatWrapper;
	}

	public void setFloatWrapper(float floatWrapper) {
		this.floatWrapper = floatWrapper;
	}

	public double getDoubleWrapper() {
		return doubleWrapper;
	}

	public void setDoubleWrapper(double doubleWrapper) {
		this.doubleWrapper = doubleWrapper;
	}

	public BigDecimal getBigDecimal() {
		return bigDecimal;
	}

	public void setBigDecimal(BigDecimal bigDecimal) {
		this.bigDecimal = bigDecimal;
	}

	public BigInteger getBigInteger() {
		return bigInteger;
	}

	public void setBigInteger(BigInteger bigInteger) {
		this.bigInteger = bigInteger;
	}

	public boolean isBooleanPrimitive() {
		return booleanPrimitive;
	}

	public void setBooleanPrimitive(boolean booleanPrimitive) {
		this.booleanPrimitive = booleanPrimitive;
	}

	public Boolean getBooleanWrapper() {
		return booleanWrapper;
	}

	public void setBooleanWrapper(Boolean booleanWrapper) {
		this.booleanWrapper = booleanWrapper;
	}

	public String getString() {
		return string;
	}

	public void setString(String string) {
		this.string = string;
	}

	public ByteBuffer getBytebuffer() {
		return bytebuffer;
	}

	public void setBytebuffer(ByteBuffer bytebuffer) {
		this.bytebuffer = bytebuffer;
	}

	public byte[] getBytearray() {
		return bytearray;
	}

	public void setBytearray(byte[] bytearray) {
		this.bytearray = bytearray;
	}

	public InetAddress getInet() {
		return inet;
	}

	public void setInet(InetAddress inet) {
		this.inet = inet;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public UUID getUuid() {
		return uuid;
	}

	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}
	

	
	
}
