package com.javaetmoi.core.persistence.hibernate.joinInheritance;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Data {

    @Id
    private Integer id;

    private String name;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
