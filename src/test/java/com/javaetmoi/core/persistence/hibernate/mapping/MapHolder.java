package com.javaetmoi.core.persistence.hibernate.mapping;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.MapKey;
import jakarta.persistence.MapKeyJoinColumn;
import jakarta.persistence.OneToMany;

import java.util.Map;

@Entity
public class MapHolder {
    @Id
    private Integer id;

    @ManyToMany
    @MapKeyJoinColumn(name = "KEY_ID")
    private Map<Target, Target> entities;

    @OneToMany
    @MapKey(name = "id")
    @JoinColumn(name = "MAP_HOLDER")
    private Map<Integer, Target> byId;

    @ElementCollection
    private Map<String, String> basics;

    @ElementCollection
    private Map<String, Part> embeddables;

    @ElementCollection
    private Map<Part, String> embeddableKeys;

    protected MapHolder() {}

    public MapHolder(
            Integer id, Map<Target, Target> entities, Map<Integer, Target> byId,
            Map<String, String> basics, Map<String, Part> embeddables, Map<Part, String> embeddableKeys) {
        this.id = id;
        this.entities = entities;
        this.byId = byId;
        this.basics = basics;
        this.embeddables = embeddables;
        this.embeddableKeys = embeddableKeys;
    }

    public Map<Target, Target> getEntities() {
        return entities;
    }

    public Map<Integer, Target> getById() {
        return byId;
    }

    public Map<String, String> getBasics() {
        return basics;
    }

    public Map<String, Part> getEmbeddables() {
        return embeddables;
    }

    public Map<Part, String> getEmbeddableKeys() {
        return embeddableKeys;
    }
}
