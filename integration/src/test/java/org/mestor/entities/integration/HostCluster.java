package org.mestor.entities.integration;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import org.codehaus.jackson.annotate.JsonProperty;
import org.eclipse.persistence.annotations.Index;
import org.eclipse.persistence.annotations.Indexes;

@Entity
@Table(name = "host_cluster")
@Indexes({ @Index(columnNames = { "name" }), })
@NamedQueries({ @NamedQuery(name = "findAllHostClusters", query = "SELECT OBJECT(hc) FROM HostCluster hc order by hc.id asc"),
		@NamedQuery(name = "countHostClusters", query = "SELECT COUNT(c) from HostCluster c"),
		@NamedQuery(name = "findHostClusterByName", query = "SELECT OBJECT(hc) FROM HostCluster hc WHERE hc.name = :name")
		//@NamedQuery(name = "findAllClusterLuns", query = "SELECT DISTINCT lun.lun from HostCluster hc JOIN hc.luns lun WHERE hc.id = :id"),
		//@NamedQuery(name = "findClusterLun", query = "SELECT OBJECT(lun) from HostCluster hc JOIN hc.luns lun WHERE hc.id = :id AND lun.lun = :lun")
})
@IdClass(HostClusterId.class)
public class HostCluster {

	public HostCluster() {
	}

	public HostCluster(final String name) {
		this.name = name;
	}

	@Id
	@JsonProperty
	private Long id;

	@Column(unique = true, nullable = false)
	@JsonProperty("name")
	private String name;

	@Column
	@JsonProperty("created_at")
	private long createdAt;

	public Long getId() {
		return id;
	}

	public void setId(final Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public long getCreatedAt() {
		return createdAt / 1000;
	}

	public void setCreatedAt(final long createdAt) {
		this.createdAt = createdAt;
	}

	@Override
	public String toString() {
		return "HostCluster [id=" + id + ", name=" + name + ", createdAt=" + createdAt + "]";
	}
}
