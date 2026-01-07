package com.javaetmoi.core.persistence.hibernate.manyToOneList;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

@Entity
@AttributeOverrides(value = { @AttributeOverride(name = "id", column = @Column(name = "SYSTEM_KEY_PK")) })
public class System extends BaseSystem {
	@OneToMany(mappedBy = "parent", orphanRemoval = true, cascade = CascadeType.ALL)
	@OrderBy(value = "systemNumber asc")
	private List<SubSystem> subSystems = new ArrayList<SubSystem>();

	public List<SubSystem> getSubSystems() {
		return subSystems;
	}

	public void setSubSystems(List<SubSystem> subSystems) {
		this.subSystems = subSystems;
	}

}
