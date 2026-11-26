package com.javaetmoi.core.persistence.hibernate.inheritance;

import java.util.List;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;

@Entity
public class Mammal extends Animal {
    @ElementCollection
    private List<String> mammalNames;

    protected Mammal() {}

    protected Mammal(Integer id, List<String> animalNames, List<String> mammalNames) {
        super(id, animalNames);
        this.mammalNames = mammalNames;
    }

    public List<String> getMammalNames() {
        return mammalNames;
    }
}
