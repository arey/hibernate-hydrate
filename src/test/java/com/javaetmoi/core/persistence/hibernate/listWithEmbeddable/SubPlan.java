package com.javaetmoi.core.persistence.hibernate.listWithEmbeddable;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity
public class SubPlan {
	@Id
	private Integer id;

	@ManyToMany
	@Fetch(FetchMode.SUBSELECT)
	@Cascade(value = { CascadeType.SAVE_UPDATE, CascadeType.DELETE })
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
