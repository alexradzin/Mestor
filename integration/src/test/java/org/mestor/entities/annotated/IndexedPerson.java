package org.mestor.entities.annotated;

import javax.persistence.Entity;

import org.eclipse.persistence.annotations.Index;
import org.eclipse.persistence.annotations.Indexes;

@Entity
@Indexes({ 
	@Index(name = "name", columnNames = { "name" }),
	@Index(name = "age", columnNames = "age"),
	@Index(name = "full_name", columnNames = { "name", "last_name" }) 
})
public class IndexedPerson extends PersonBase {
}
