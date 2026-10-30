package com.javaetmoi.core.persistence.hibernate.manyToOneList;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import org.hibernate.annotations.Fetch;

import static jakarta.persistence.GenerationType.AUTO;
import static org.hibernate.annotations.FetchMode.JOIN;

@Entity
public class Holder {
	@Id
	@GeneratedValue(strategy = AUTO)
	private Integer id;

	@ManyToOne
	@JoinColumn(name = "system")
	@Fetch(JOIN)
	private System system;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public System getSystem() {
		return system;
	}

	public void setSystem(System system) {
		this.system = system;
	}
}
