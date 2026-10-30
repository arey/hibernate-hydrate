package com.javaetmoi.core.persistence.hibernate.joinInheritance;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;

import static jakarta.persistence.InheritanceType.JOINED;

@Entity
@Inheritance(strategy = JOINED)
public class ParentClass {
    @Id
    private Integer id;
}

