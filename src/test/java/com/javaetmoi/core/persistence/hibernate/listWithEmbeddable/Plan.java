package com.javaetmoi.core.persistence.hibernate.listWithEmbeddable;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;

import org.hibernate.annotations.Fetch;

import static org.hibernate.annotations.FetchMode.SELECT;

@Entity
public class Plan {
	@Id
	private Integer id;

	@ElementCollection
	@Fetch(SELECT)
	@OrderColumn(name = "elementOrder")
	@CollectionTable(name = "TRANSFER", joinColumns = @JoinColumn(name = "PLAN_ID"))
	private List<Transfer> transfers = new ArrayList<>();

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
