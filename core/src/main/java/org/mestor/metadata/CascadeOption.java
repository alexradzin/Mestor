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

package org.mestor.metadata;

/**
 * This {@code enum} defines cascading options that are represented in JPA spec using
 * {@code enum}s {@code javax.persistence.CascadeType} and {@code javax.persistence.FetchType} as well as
 * using boolean flag  {@code orhpanRemoval()} declared in annotations {@code OneToMany} and {@code OneToOne}.
 * @author alexr
 */
public enum CascadeOption {
	// javax.persistence.CascadeType
    PERSIST,
    MERGE,
    REMOVE,
    REFRESH,
    DETACH,

    // javax.persistence.FetchType#EAGER
    FETCH,

    // javax.persistence.(OneToMany, OneToOne).orhpanRemoval()
    ORPHAN_REMOVAL,
}
