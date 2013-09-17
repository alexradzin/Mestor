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
	

}
