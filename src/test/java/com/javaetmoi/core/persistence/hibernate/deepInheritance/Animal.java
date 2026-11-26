package com.javaetmoi.core.persistence.hibernate.deepInheritance;

import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;

/**
 * Root of a three level hierarchy: {@link Animal} ← {@link Mammal} ← {@link Dog}.
 */
@Entity
public class Animal {
    @Id
    private Integer id;

    @ElementCollection
    @CollectionTable(name = "ANIMAL_NAMES", joinColumns = @JoinColumn(name = "ANIMAL_ID"))
    @Column(name = "NAME")
    private List<String> animalNames;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public List<String> getAnimalNames() {
        return animalNames;
    }

    public void setAnimalNames(List<String> animalNames) {
        this.animalNames = animalNames;
    }
}
