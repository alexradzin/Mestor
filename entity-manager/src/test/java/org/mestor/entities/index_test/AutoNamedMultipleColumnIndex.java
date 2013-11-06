package org.mestor.entities.index_test;

import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;

@Entity
@Table(indexes = { @Index(columnList = "column1, column2, column3"), })
public class AutoNamedMultipleColumnIndex extends IndexBase {
	// This class does not contain code. It is used for testing of annotation parsing.
}
