package com.javaetmoi.core.persistence.hibernate.deepInheritance;

import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;

@Entity
public class Dog extends Mammal {
    @ElementCollection
    @CollectionTable(name = "DOG_NAMES", joinColumns = @JoinColumn(name = "DOG_ID"))
    @Column(name = "NAME")
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
