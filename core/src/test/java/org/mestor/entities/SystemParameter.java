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

package org.mestor.entities;

public class SystemParameter {
    public static class SystemParameterPK {
    	final String key;
    	final String category;

    	public SystemParameterPK(final String category, final String key) {
    		this.category = category;
    		this.key = key;
    	}
    }

	private String category;
	private String key;
	private String value;



	public String getCategory() {
		return category;
	}
	public void setCategory(final String category) {
		this.category = category;
	}
	public String getKey() {
		return key;
	}
	public void setKey(final String key) {
		this.key = key;
	}
	public String getValue() {
		return value;
	}
	public void setValue(final String value) {
		this.value = value;
	}


}
