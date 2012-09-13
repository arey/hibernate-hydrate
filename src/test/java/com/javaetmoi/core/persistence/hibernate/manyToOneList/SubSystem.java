package com.javaetmoi.core.persistence.hibernate.manyToOneList;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

@Entity
public class SubSystem extends BaseSystem {

	@ManyToOne
	@Cascade(CascadeType.ALL)
	@JoinColumn(name = "parent", referencedColumnName = "ID")
	private System parent;

	public System getParent() {
		return parent;
	}

	public void setParent(System parent) {
		this.parent = parent;
	}
	
}