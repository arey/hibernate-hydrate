package com.javaetmoi.core.persistence.hibernate.inheritance;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;

import static jakarta.persistence.InheritanceType.JOINED;

@Entity
@Inheritance(strategy = JOINED)
public class JoinedBase {
    @Id
    private Integer id;

    protected JoinedBase() {}

    protected JoinedBase(Integer id) {
        this.id = id;
    }
}
