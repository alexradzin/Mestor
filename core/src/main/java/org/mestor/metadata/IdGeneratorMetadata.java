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

import static java.util.Objects.hash;

import java.util.Objects;

public class IdGeneratorMetadata<T, ID> {
	private final Class<T> classType;
	private final Class<ID> idType;
	private final GenerationType generationType;
	private final String generator;


	public IdGeneratorMetadata(final Class<ID> idType) {
		this(idType, null);
	}

	public IdGeneratorMetadata(final Class<ID> idType, final String generator) {
		this(null, idType, GenerationType.AUTO, generator);
	}

	public IdGeneratorMetadata(final Class<T> classType, final Class<ID> idType) {
		this(classType, idType, GenerationType.AUTO, null);
	}

	public IdGeneratorMetadata(final Class<T> classType, final Class<ID> idType, final GenerationType generationType) {
		this(classType, idType, generationType, null);
	}

	public IdGeneratorMetadata(final Class<T> classType, final Class<ID> idType, final GenerationType generationType, final String generator) {
		this.classType = classType;
		this.idType = idType;

		if (!GenerationType.AUTO.equals(generationType)) {
			throw new UnsupportedOperationException("Generation type other than AUTO is not supported right now");
		}
		this.generationType = generationType;
		this.generator = generator;
	}


	/**
	 * Copied from JPA
	 * @author alexr
	 */
	public enum GenerationType {
	    TABLE,
	    SEQUENCE,
	    IDENTITY,
	    AUTO
	}


	public Class<T> getClassType() {
		return classType;
	}

	public Class<ID> getIdType() {
		return idType;
	}

	public GenerationType getGenerationType() {
		return generationType;
	}

	public String getGenerator() {
		return generator;
	}


	@Override
	public int hashCode() {
		return hash(classType, idType, generationType, generator);
	}

	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof IdGeneratorMetadata)) {
			return false;
		}
		IdGeneratorMetadata<?, ?> other = (IdGeneratorMetadata<?, ?>)obj;

		return
				Objects.equals(classType, other.classType) &&
				Objects.equals(idType, other.idType) &&
				Objects.equals(generationType, other.generationType) &&
				Objects.equals(generator, other.generator);
	}

	@Override
	public String toString() {
		return Objects.toString(classType, "*") + "#" +
				Objects.toString(idType) + ":" +
				Objects.toString(generationType) +
				(generator == null ? "" : "," + generator);
	}
}
