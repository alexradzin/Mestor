package org.mestor.entities.index_test;

import javax.persistence.Entity;

import org.eclipse.persistence.annotations.Index;
import org.eclipse.persistence.annotations.Indexes;

@Entity
@Indexes({
	@Index(name = "foo bar", columnNames = "column2")
})
public class IndexNameWithSpace extends IndexBase {
	// This class does not contain code. It is used for testing of annotation parsing.
}
