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

import java.lang.reflect.AnnotatedElement;

import org.junit.internal.runners.statements.InvokeMethod;
import org.junit.runner.RunWith;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

/**
 * Test runner that should be used for Cassandra aware tests. 
 * Use {@link RunWith} annotation to enable it for specific test case. 
 * Use {@link Cassandra} annotation to control when cassandra is started and stopped. 
 * This class itself is annotated with {@link Cassandra}. This annotation provides default
 * values when test case class is not annotated.  
 * @author alexr
 */
@Cassandra
public class CassandraAwareTestRunner extends BlockJUnit4ClassRunner {
	private final Cassandra cassandra;

	public CassandraAwareTestRunner(Class<?> klass) throws InitializationError {
		super(klass);
		cassandra = getCassandra(klass, getClass());
	}

	private static Cassandra getCassandra(AnnotatedElement ae, AnnotatedElement defaultAe) {
		Cassandra c = ae.getAnnotation(Cassandra.class);
		if (c == null) {
			c = defaultAe.getAnnotation(Cassandra.class);
		}
		return c;
	}
	
	@Override
	public void run(final RunNotifier notifier) {
		try {
			cassandra.mode().before();
			super.run(notifier);
		} finally {
			cassandra.mode().after();
		}
	}
	
	
	@Override
	protected Statement methodInvoker(FrameworkMethod method, Object test) {
		return new InvokeCassandraAwareMethod(method, test);
	}
	
	private static class InvokeCassandraAwareMethod extends InvokeMethod {
		private final Cassandra cassandra;

		public InvokeCassandraAwareMethod(FrameworkMethod testMethod, Object target) {
			super(testMethod, target);
			cassandra = getCassandra(testMethod.getMethod(), CassandraAwareTestRunner.class);
		}
		
		@Override
		public void evaluate() throws Throwable {
			try {
				
				cassandra.mode().before();
				super.evaluate();
			} finally {
				cassandra.mode().after();
			}
		}
	}
	
}
