package com.javaetmoi.core.persistence.hibernate.inheritance;

import com.javaetmoi.core.persistence.hibernate.mapping.Target;

import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;

import static jakarta.persistence.FetchType.LAZY;

@MappedSuperclass
public class MappedBase {
    @Id
    private Integer id;

    @ManyToOne(fetch = LAZY)
    private Target target;

    protected MappedBase() {}

    protected MappedBase(Integer id, Target target) {
        this.id = id;
        this.target = target;
    }

    public Target getTarget() {
        return target;
    }
}
