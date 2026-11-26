package com.javaetmoi.core.persistence.hibernate.mapping;

import java.util.List;
import java.util.Map;

import org.hibernate.annotations.Any;
import org.hibernate.annotations.AnyDiscriminatorValue;
import org.hibernate.annotations.AnyKeyJavaClass;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.OneToMany;

import static jakarta.persistence.FetchType.LAZY;

/**
 * Embeddable with one attribute of each kind that is hydrated.
 * Except for arrays: Hibernate fails to load arrays of entities inside of embeddables.
 */
@Embeddable
public class Outer {
    @ManyToOne(fetch = LAZY)
    private Target target;

    @Any(fetch = LAZY)
    @AnyKeyJavaClass(Integer.class)
    @AnyDiscriminatorValue(discriminator = "T", entity = Target.class)
    @Column(name = "OUTER_ANY_TYPE")
    @JoinColumn(name = "OUTER_ANY_ID")
    private Object any;

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

    public Outer(Target target, Object any, List<Target> targets, Map<String, Target> map, Part part) {
        this.target = target;
        this.any = any;
        this.targets = targets;
        this.map = map;
        this.part = part;
    }

    public Target getTarget() {
        return target;
    }

    public Object getAny() {
        return any;
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
