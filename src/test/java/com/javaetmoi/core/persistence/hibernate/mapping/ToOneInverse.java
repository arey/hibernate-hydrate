package com.javaetmoi.core.persistence.hibernate.mapping;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;

import static jakarta.persistence.FetchType.LAZY;

/**
 * Owning side of the bidirectional one-to-one association to {@link ToOneHolder}.
 */
@Entity
public class ToOneInverse {
    @Id
    private Integer id;

    @OneToOne(fetch = LAZY)
    private ToOneHolder holder;

    @ManyToOne(fetch = LAZY)
    private Target target;

    protected ToOneInverse() {}

    public ToOneInverse(Integer id, ToOneHolder holder, Target target) {
        this.id = id;
        this.holder = holder;
        this.target = target;
    }

    public ToOneHolder getHolder() {
        return holder;
    }

    public Target getTarget() {
        return target;
    }
}
