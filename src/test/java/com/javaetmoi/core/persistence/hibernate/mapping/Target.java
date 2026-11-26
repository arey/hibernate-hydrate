package com.javaetmoi.core.persistence.hibernate.mapping;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.util.List;

/**
 * Target of the associations under test.
 * Its lazy tags show whether it has been hydrated.
 */
@Entity
public class Target {
    @Id
    private Integer id;

    @ElementCollection
    private List<String> tags;

    protected Target() {}

    public Target(Integer id) {
        this.id = id;
        this.tags = List.of("tag" + id);
    }

    public Integer getId() {
        return id;
    }

    public List<String> getTags() {
        return tags;
    }
}
