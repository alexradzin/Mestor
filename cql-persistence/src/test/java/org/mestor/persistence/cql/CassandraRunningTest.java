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

import java.io.IOException;

import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.service.CassandraDaemon;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

//TODO create test runner that lazyly runs cassandra and registers shutdown hook that shuts it down if needed.
@Deprecated
@Ignore
public class CassandraRunningTest {
	private static CassandraDaemon cassandraDaemon;
	
	@BeforeClass
	public static void setUp() throws IOException {
		System.out.println("starting");
		DatabaseDescriptor.createAllDirectories();
		cassandraDaemon = new CassandraDaemon();
        cassandraDaemon.init(null);
		cassandraDaemon.start();
		System.out.println("started");
	}

	//@AfterClass
	public static void tearDown() {
		System.out.println("closing");
    	cassandraDaemon.deactivate();
    	cassandraDaemon.destroy();
    	cassandraDaemon.stop();
		System.out.println("closed");
	}
	
	@Test
	public void test() {
		System.out.println("my test");
	}
}
