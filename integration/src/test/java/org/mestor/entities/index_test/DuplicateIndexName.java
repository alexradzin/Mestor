package org.mestor.entities.index_test;

import javax.persistence.Entity;

import org.eclipse.persistence.annotations.Index;
import org.eclipse.persistence.annotations.Indexes;

@Entity
@Indexes({ 
	@Index(name = "index1", columnNames = { "column1" }),
	@Index(name = "index1", columnNames = "column2") 
})
public class DuplicateIndexName extends IndexBase {
}
