package com.javaetmoi.core.persistence.hibernate.domain;

import jakarta.persistence.*;
import java.io.Serializable;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.FetchType.LAZY;

@Entity
public class Customer implements Serializable {
    @Id
    private Integer id;

    @OneToOne(cascade = ALL, fetch = LAZY)
    @JoinColumn(name = "passport_fk")
    private Passport passport;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Passport getPassport() {
        return passport;
    }

    public void setPassport(Passport passport) {
        this.passport = passport;
    }
}
