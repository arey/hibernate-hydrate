package com.javaetmoi.core.persistence.hibernate.mapping;

import java.util.List;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

import static jakarta.persistence.FetchType.LAZY;

/**
 * Embeddable with an association, a collection and a nested embeddable.
 */
@Embeddable
public class Outer {
    @ManyToOne(fetch = LAZY)
    private Target target;

    @OneToMany
    @JoinColumn(name = "OUTER_HOLDER")
    private List<Target> targets;

    @Embedded
    private Part part;

    protected Outer() {}

    public Outer(Target target, List<Target> targets, Part part) {
        this.target = target;
        this.targets = targets;
        this.part = part;
    }

    public Target getTarget() {
        return target;
    }

    public List<Target> getTargets() {
        return targets;
    }

    public Part getPart() {
        return part;
    }
}
