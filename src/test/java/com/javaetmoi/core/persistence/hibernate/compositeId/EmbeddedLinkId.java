package com.javaetmoi.core.persistence.hibernate.compositeId;

import com.javaetmoi.core.persistence.hibernate.mapping.Target;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Embeddable;
import jakarta.persistence.ManyToOne;

import static jakarta.persistence.FetchType.LAZY;

@Embeddable
public class EmbeddedLinkId implements Serializable {
    @ManyToOne(fetch = LAZY)
    private Target target;

    private int seq;

    public EmbeddedLinkId() {}

    public EmbeddedLinkId(Target target, int seq) {
        this.target = target;
        this.seq = seq;
    }

    public Target getTarget() {
        return target;
    }

    public int getSeq() {
        return seq;
    }

    @Override
    public int hashCode() {
        return Objects.hash(target.getId(), seq);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof EmbeddedLinkId other &&
                Objects.equals(target.getId(), other.target.getId()) && seq == other.seq;
    }
}
