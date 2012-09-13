package com.javaetmoi.core.persistence.hibernate.manyToOneList;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

@Entity
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