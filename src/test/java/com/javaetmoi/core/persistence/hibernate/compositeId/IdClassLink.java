package com.javaetmoi.core.persistence.hibernate.compositeId;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@IdClass(IdClassLinkId.class)
public class IdClassLink {
    @Id
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "TARGET_ID")
    private LinkTarget target;

    @Id
    private int seq;

    public LinkTarget getTarget() {
        return target;
    }

    public void setTarget(LinkTarget target) {
        this.target = target;
    }

    public int getSeq() {
        return seq;
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }
}
