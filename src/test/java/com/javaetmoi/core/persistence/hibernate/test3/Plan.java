package com.javaetmoi.core.persistence.hibernate.test3;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OrderColumn;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;



@Entity
public class Plan {
	@Id
	private Integer id;
	@ElementCollection
	@Fetch(FetchMode.SELECT)
	@OrderColumn(name = "elementOrder")
	@Cascade(CascadeType.ALL)
	@CollectionTable(name = "TRANSFER", joinColumns = @JoinColumn(name = "PLAN_ID"))
	private List<Transfer> transfers = new ArrayList<Transfer>();

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public List<Transfer> getTransfers() {
		return transfers;
	}

	public void setTransfers(List<Transfer> transfers) {
		this.transfers = transfers;
	}

}
