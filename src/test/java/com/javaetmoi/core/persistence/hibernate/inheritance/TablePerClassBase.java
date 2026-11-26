package com.javaetmoi.core.persistence.hibernate.inheritance;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;

import static jakarta.persistence.InheritanceType.TABLE_PER_CLASS;

@Entity
@Inheritance(strategy = TABLE_PER_CLASS)
public class TablePerClassBase {
    @Id
    private Integer id;

    protected TablePerClassBase() {}

    protected TablePerClassBase(Integer id) {
        this.id = id;
    }
}
