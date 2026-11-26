package com.javaetmoi.core.persistence.hibernate.deepInheritance;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import static jakarta.persistence.FetchType.LAZY;

/**
 * Holds an {@link Animal} by an association declared with the root type of the hierarchy.
 */
@Entity
public class AnimalHolder {
    @Id
    private Integer id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "ANIMAL_ID")
    private Animal animal;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Animal getAnimal() {
        return animal;
    }

    public void setAnimal(Animal animal) {
        this.animal = animal;
    }
}
