package com.javaetmoi.core.persistence.hibernate.joinInheritance;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;

// Couple
@Entity
public class ParentReference {
    @Id
    private Integer id;

    @OneToOne
    private ParentClass parent;

    public ParentClass getParent() {
        return parent;
    }

    public void setParent(ParentClass parent) {
        this.parent = parent;
    }

    public Integer getId() {
        return id;
    }
}
