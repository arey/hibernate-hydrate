package com.javaetmoi.core.persistence.hibernate.compositeId;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Embeddable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import static jakarta.persistence.FetchType.LAZY;

@Embeddable
public class EmbeddedLinkId implements Serializable {
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "TARGET_ID")
    private LinkTarget target;

    private int seq;

    public EmbeddedLinkId() {}

    public EmbeddedLinkId(LinkTarget target, int seq) {
        this.target = target;
        this.seq = seq;
    }

    public LinkTarget getTarget() {
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
