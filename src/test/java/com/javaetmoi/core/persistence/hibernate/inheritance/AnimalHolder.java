package com.javaetmoi.core.persistence.hibernate.inheritance;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
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
    private Animal animal;

    public AnimalHolder() {}

    public AnimalHolder(Integer id, Animal animal) {
        this.id = id;
        this.animal = animal;
    }

    public Animal getAnimal() {
        return animal;
    }

    public void setAnimal(Animal animal) {
        this.animal = animal;
    }
}
