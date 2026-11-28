package com.javaetmoi.core.persistence.hibernate.inheritance;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;

import java.util.List;

@Entity
public class Dog extends Mammal {
    @ElementCollection
    private List<String> dogNames;

    public Dog() {}

    public Dog(Integer id, List<String> animalNames, List<String> mammalNames, List<String> dogNames) {
        super(id, animalNames, mammalNames);
        this.dogNames = dogNames;
    }

    public List<String> getDogNames() {
        return dogNames;
    }

    public void setDogNames(List<String> dogNames) {
        this.dogNames = dogNames;
    }
}
