package com.javaetmoi.core.persistence.hibernate.mapping;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.OneToMany;

import java.util.List;
import java.util.Map;

import static jakarta.persistence.FetchType.LAZY;

/**
 * Embeddable with one attribute of each kind that is hydrated.
 */
@Embeddable
public class Outer {
    @ManyToOne(fetch = LAZY)
    private Target target;

    @OneToMany
    @JoinColumn(name = "OUTER_HOLDER")
    private List<Target> targets;

    @OneToMany
    @JoinColumn(name = "OUTER_MAP_HOLDER")
    @MapKeyColumn(name = "OUTER_MAP_KEY")
    private Map<String, Target> map;

    @Embedded
    private Part part;

    protected Outer() {}

    public Outer(Target target, List<Target> targets, Map<String, Target> map, Part part) {
        this.target = target;
        this.targets = targets;
        this.map = map;
        this.part = part;
    }

    public Target getTarget() {
        return target;
    }

    public List<Target> getTargets() {
        return targets;
    }

    public Map<String, Target> getMap() {
        return map;
    }

    public Part getPart() {
        return part;
    }
}
