package com.javaetmoi.core.persistence.hibernate.chain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

import static jakarta.persistence.FetchType.LAZY;

@Entity
public class ChainNode {
    @Id
    private Integer id;

    @ManyToOne(fetch = LAZY)
    private ChainNode next;

    protected ChainNode() {}

    public ChainNode(Integer id, ChainNode next) {
        this.id = id;
        this.next = next;
    }

    public Integer getId() {
        return id;
    }

    public ChainNode getNext() {
        return next;
    }

    public void setNext(ChainNode next) {
        this.next = next;
    }
}
