package com.javaetmoi.core.persistence.hibernate.listWithEmbeddable;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity
public class SubPlan {
	@Id
	private Integer id;

	@ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.REMOVE })
	@Fetch(FetchMode.SUBSELECT)
	private List<Event> events = new ArrayList<Event>();

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public List<Event> getEvents() {
		return events;
	}

	public void setEvents(List<Event> events) {
		this.events = events;
	}

}
