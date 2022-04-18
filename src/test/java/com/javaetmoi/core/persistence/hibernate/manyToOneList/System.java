package com.javaetmoi.core.persistence.hibernate.manyToOneList;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

@Entity
@AttributeOverrides(value = { @AttributeOverride(name = "id", column = @Column(name = "SYSTEM_KEY_PK")) })
public class System extends BaseSystem {
	@OneToMany(mappedBy = "parent", orphanRemoval = true)
	@Cascade(CascadeType.ALL)
	@OrderBy(value = "systemNumber asc")
	private List<SubSystem> subSystems = new ArrayList<SubSystem>();

	public List<SubSystem> getSubSystems() {
		return subSystems;
	}

	public void setSubSystems(List<SubSystem> subSystems) {
		this.subSystems = subSystems;
	}

}