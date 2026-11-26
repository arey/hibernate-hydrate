package com.javaetmoi.core.persistence.hibernate.mapping;

import java.util.List;
import java.util.Set;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;

@Entity
public class CollectionHolder {
    @Id
    private Integer id;

    @OneToMany
    @JoinColumn(name = "LIST_HOLDER")
    private List<Target> list;

    @OneToMany
    @JoinColumn(name = "SET_HOLDER")
    private Set<Target> set;

    @ManyToMany
    private List<Target> manyToMany;

    @ElementCollection
    private List<String> basics;

    @ElementCollection
    private List<Part> embeddables;

    protected CollectionHolder() {}

    public CollectionHolder(
            Integer id, List<Target> list, Set<Target> set, List<Target> manyToMany,
            List<String> basics, List<Part> embeddables) {
        this.id = id;
        this.list = list;
        this.set = set;
        this.manyToMany = manyToMany;
        this.basics = basics;
        this.embeddables = embeddables;
    }

    public List<Target> getList() {
        return list;
    }

    public Set<Target> getSet() {
        return set;
    }

    public List<Target> getManyToMany() {
        return manyToMany;
    }

    public List<String> getBasics() {
        return basics;
    }

    public List<Part> getEmbeddables() {
        return embeddables;
    }
}
