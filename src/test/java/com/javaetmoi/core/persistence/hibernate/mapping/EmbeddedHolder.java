package com.javaetmoi.core.persistence.hibernate.mapping;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class EmbeddedHolder {
    @Id
    private Integer id;

    @Embedded
    private Outer outer;

    protected EmbeddedHolder() {}

    public EmbeddedHolder(Integer id, Outer outer) {
        this.id = id;
        this.outer = outer;
    }

    public Outer getOuter() {
        return outer;
    }
}
