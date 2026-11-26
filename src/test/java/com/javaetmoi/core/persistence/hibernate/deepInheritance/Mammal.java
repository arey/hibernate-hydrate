package com.javaetmoi.core.persistence.hibernate.deepInheritance;

import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;

@Entity
public class Mammal extends Animal {
    @ElementCollection
    @CollectionTable(name = "MAMMAL_NAMES", joinColumns = @JoinColumn(name = "MAMMAL_ID"))
    @Column(name = "NAME")
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
