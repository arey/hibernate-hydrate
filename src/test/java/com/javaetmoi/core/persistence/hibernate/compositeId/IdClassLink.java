package com.javaetmoi.core.persistence.hibernate.compositeId;

import com.javaetmoi.core.persistence.hibernate.mapping.Target;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.ManyToOne;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@IdClass(IdClassLinkId.class)
public class IdClassLink {
    @Id
    @ManyToOne(fetch = LAZY)
    private Target target;

    @Id
    private int seq;

    protected IdClassLink() {}

    public IdClassLink(Target target, int seq) {
        this.target = target;
        this.seq = seq;
    }

    public Target getTarget() {
        return target;
    }
}
