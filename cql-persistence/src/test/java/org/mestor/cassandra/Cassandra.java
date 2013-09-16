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

package org.mestor.cassandra;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;


@Target({TYPE, METHOD}) 
@Retention(RUNTIME)
public @interface Cassandra {
	public Mode mode() default Mode.Required;
	
	public static enum Mode {
		Required {
			@Override
			public void before() {
				CassandraController c = new CassandraController();
				if (!c.isRunning()) {
					c.start();
					Runtime.getRuntime().addShutdownHook(new Thread() {
						@Override
						public void run() {
							new CassandraController().shutdown();
						}
					});
				}
			}
		},
		RequiresNew {
			@Override
			public void before() {
				CassandraController c = new CassandraController();
				if (c.isRunning()) {
					c.shutdown();
				}
				c.start();
			}
			
			@Override
			public void after() {
				new CassandraController().shutdown();
			}
		},
		
		Mandatory {
			@Override
			public void before() {
				if (!new CassandraController().isRunning()) {
					throw new IllegalStateException("Cassandra must run at this phase");
				}
			}
		},
		
		Supports,
		
		Never {
			@Override
			public void before() {
				if (new CassandraController().isRunning()) {
					throw new IllegalStateException("Cassandra should not run at this phase");
				}
			}
		},
		;
		
		public void before() {
			// empty implementation
		}

		public void after() {
			// empty implementation
		}
	}
}
