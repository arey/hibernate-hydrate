package com.javaetmoi.core.persistence.hibernate.joinInheritance;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public class ParentClass {

    @Id
    private Integer id;
}

