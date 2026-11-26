package com.javaetmoi.core.persistence.hibernate.mapping;

import jakarta.persistence.Embeddable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import static jakarta.persistence.FetchType.LAZY;

/**
 * Embeddable with an association.
 */
@Embeddable
public class Part {
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "PART_TARGET")
    private Target target;

    protected Part() {}

    public Part(Target target) {
        this.target = target;
    }

    public Target getTarget() {
        return target;
    }
}
