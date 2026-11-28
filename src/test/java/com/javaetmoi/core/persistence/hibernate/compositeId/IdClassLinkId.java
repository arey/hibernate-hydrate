package com.javaetmoi.core.persistence.hibernate.compositeId;

import java.io.Serializable;
import java.util.Objects;

public class IdClassLinkId implements Serializable {
    private Integer target;

    private int seq;

    public IdClassLinkId() {}

    public IdClassLinkId(Integer target, int seq) {
        this.target = target;
        this.seq = seq;
    }

    @Override
    public int hashCode() {
        return Objects.hash(target, seq);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof IdClassLinkId other && Objects.equals(target, other.target) && seq == other.seq;
    }
}
