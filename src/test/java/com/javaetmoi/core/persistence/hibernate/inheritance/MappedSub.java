package com.javaetmoi.core.persistence.hibernate.inheritance;

import com.javaetmoi.core.persistence.hibernate.mapping.Target;

import jakarta.persistence.Entity;

@Entity
public class MappedSub extends MappedBase {
    protected MappedSub() {}

    public MappedSub(Integer id, Target target) {
        super(id, target);
    }
}
