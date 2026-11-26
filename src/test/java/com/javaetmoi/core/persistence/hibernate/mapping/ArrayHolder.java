package com.javaetmoi.core.persistence.hibernate.mapping;


import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;

@Entity
public class ArrayHolder {
    @Id
    private Integer id;

    @ElementCollection
    @OrderColumn
    private String[] names;

    @ElementCollection
    @OrderColumn
    private int[] numbers;

    @OneToMany
    @JoinColumn(name = "ARRAY_HOLDER")
    @OrderColumn
    private Target[] elements;

    protected ArrayHolder() {}

    public ArrayHolder(Integer id, String[] names, int[] numbers, Target[] elements) {
        this.id = id;
        this.names = names;
        this.numbers = numbers;
        this.elements = elements;
    }

    public String[] getNames() {
        return names;
    }

    public int[] getNumbers() {
        return numbers;
    }

    public Target[] getElements() {
        return elements;
    }
}
