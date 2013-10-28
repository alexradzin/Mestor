package org.mestor.entities.index_test;

import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;

@Entity
@Table(indexes = {
		@Index(name = "index1", columnList = "column1"),
		@Index(name = "index2", columnList = "column2"),
		@Index(name = "index3", columnList = "column3, column4"),
		})
public class ThreeGoodIndexes extends IndexBase {
	// This class does not contain code. It is used for testing of annotation parsing.
}

