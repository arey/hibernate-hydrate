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
	@JoinColumn(name = "SYSTEM_KEY_FK", referencedColumnName = "SYSTEM_KEY_PK")
	private System parent;

	public System getParent() {
		return parent;
	}

	public void setParent(System parent) {
		this.parent = parent;
	}
	
}