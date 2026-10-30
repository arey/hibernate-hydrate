package com.javaetmoi.core.persistence.hibernate.manyToOneList;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

import static jakarta.persistence.CascadeType.ALL;

@Entity
@AttributeOverride(name = "id", column = @Column(name = "SYSTEM_KEY_PK"))
public class System extends BaseSystem {
	@OneToMany(mappedBy = "parent", orphanRemoval = true, cascade = ALL)
	@OrderBy(value = "systemNumber asc")
	private List<SubSystem> subSystems = new ArrayList<>();

	public List<SubSystem> getSubSystems() {
		return subSystems;
	}

	public void setSubSystems(List<SubSystem> subSystems) {
		this.subSystems = subSystems;
	}
}
