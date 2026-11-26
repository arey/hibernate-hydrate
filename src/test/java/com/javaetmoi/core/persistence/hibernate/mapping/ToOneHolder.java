package com.javaetmoi.core.persistence.hibernate.mapping;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;

import static jakarta.persistence.FetchType.LAZY;

@Entity
public class ToOneHolder {
    @Id
    private Integer id;

    @ManyToOne(fetch = LAZY)
    private Target manyToOne;

    @OneToOne(fetch = LAZY)
    private Target oneToOne;

    @OneToOne(mappedBy = "holder")
    private ToOneInverse inverse;

    protected ToOneHolder() {}

    public ToOneHolder(Integer id, Target manyToOne, Target oneToOne) {
        this.id = id;
        this.manyToOne = manyToOne;
        this.oneToOne = oneToOne;
    }

    public Target getManyToOne() {
        return manyToOne;
    }

    public Target getOneToOne() {
        return oneToOne;
    }

    public ToOneInverse getInverse() {
        return inverse;
    }
}
