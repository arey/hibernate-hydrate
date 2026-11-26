package com.javaetmoi.core.persistence.hibernate.inheritance;

import java.util.List;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

/**
 * Root of a three level hierarchy: {@link Animal} ← {@link Mammal} ← {@link Dog}.
 */
@Entity
public class Animal {
    @Id
    private Integer id;

    @ElementCollection
    private List<String> animalNames;

    protected Animal() {}

    protected Animal(Integer id, List<String> animalNames) {
        this.id = id;
        this.animalNames = animalNames;
    }

    public List<String> getAnimalNames() {
        return animalNames;
    }
}
