package com.javaetmoi.core.persistence.hibernate.array;

import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;

@Entity
public class ArrayElement {
    @Id
    private Integer id;

    @ElementCollection
    @CollectionTable(name = "ARRAY_ELEMENT_TAGS", joinColumns = @JoinColumn(name = "ELEMENT_ID"))
    @Column(name = "TAG")
    private List<String> tags;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }
}
