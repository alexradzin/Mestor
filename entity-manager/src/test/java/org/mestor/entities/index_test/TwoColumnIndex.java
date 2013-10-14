package org.mestor.entities.index_test;

import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;

@Entity
@Table(indexes = { @Index(name = "index1", columnList = "column1, column2"), })
public class TwoColumnIndex extends IndexBase {
}
