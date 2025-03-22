package com.javaetmoi.core.persistence.hibernate.joinInheritance;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public class ParentClass {

    @Id
    private Integer id;
}

