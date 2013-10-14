package org.mestor.entities.annotated;

import javax.persistence.Entity;

import org.eclipse.persistence.annotations.Index;
import org.eclipse.persistence.annotations.Indexes;

@Entity
@Indexes({ 
	@Index(name = "name", columnNames = { "name", "name" }),
})
public class SameColumnTwiceInIndexPerson extends PersonBase {
}
