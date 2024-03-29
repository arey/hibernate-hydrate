package com.javaetmoi.core.persistence.hibernate.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import java.io.Serializable;

@Entity
public class Passport implements Serializable {

    @Id
    private Integer id;

    @OneToOne(mappedBy = "passport")
    private Customer owner;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Customer getOwner() {
        return owner;
    }

    public void setOwner(Customer owner) {
        this.owner = owner;
    }
}