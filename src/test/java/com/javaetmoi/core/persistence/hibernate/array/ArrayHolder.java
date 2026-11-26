package com.javaetmoi.core.persistence.hibernate.array;

import com.javaetmoi.core.persistence.hibernate.mapping.Target;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
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
    @CollectionTable(name = "ARRAY_HOLDER_NAMES", joinColumns = @JoinColumn(name = "HOLDER_ID"))
    @OrderColumn(name = "POS")
    @Column(name = "NAME")
    private String[] names;

    @ElementCollection
    @CollectionTable(name = "ARRAY_HOLDER_NUMBERS", joinColumns = @JoinColumn(name = "HOLDER_ID"))
    @OrderColumn(name = "POS")
    @Column(name = "NUMBER")
    private int[] numbers;

    @OneToMany
    @JoinColumn(name = "ARRAY_HOLDER")
    @OrderColumn(name = "ARRAY_POS")
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
