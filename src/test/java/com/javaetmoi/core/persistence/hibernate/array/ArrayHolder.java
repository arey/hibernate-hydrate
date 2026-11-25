package com.javaetmoi.core.persistence.hibernate.array;

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
    @JoinColumn(name = "HOLDER_ID")
    @OrderColumn(name = "POS")
    private ArrayElement[] elements;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String[] getNames() {
        return names;
    }

    public void setNames(String[] names) {
        this.names = names;
    }

    public int[] getNumbers() {
        return numbers;
    }

    public void setNumbers(int[] numbers) {
        this.numbers = numbers;
    }

    public ArrayElement[] getElements() {
        return elements;
    }

    public void setElements(ArrayElement[] elements) {
        this.elements = elements;
    }
}
