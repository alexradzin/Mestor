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

package org.mestor.em;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class DefaultSchemaViolationHandler implements SchemaViolationHandler {
	private final static Logger logger = LoggerFactory.getLogger(DefaultSchemaViolationHandler.class);
	
	private ViolationLevel worseViolationLevel = null;
	private ViolationLevel violationLevelThreshold = null;

	
	DefaultSchemaViolationHandler() {
		this(ViolationLevel.ERROR);
	}
	
	
	DefaultSchemaViolationHandler(ViolationLevel violationLevelThreshold) {
		this.violationLevelThreshold = violationLevelThreshold;
	}
	
	@Override
	public void handle(ViolationLevel level, String msg) {
		updateWorseViolationLevel(level);
		
		switch(level) {
			case ERROR:
				logger.error(msg);
				break;
			case FATAL:
				logger.error(msg);
				break;
			case WARNING:
				logger.warn(msg);
				break;
			default:
				throw new IllegalArgumentException("Unknown violation level " + level + " with message " + msg);
		}
		
		
		if (level.ordinal() >= violationLevelThreshold.ordinal()) {
			throw new IllegalStateException(msg);
		}
	}
	
	
	@Override
	public ViolationLevel getViolationLevel() {
		return worseViolationLevel;
	}

	
	private void updateWorseViolationLevel(ViolationLevel level) {
		if(worseViolationLevel == null || level.ordinal() > worseViolationLevel.ordinal()) {
			worseViolationLevel = level;
		}
	}
}
