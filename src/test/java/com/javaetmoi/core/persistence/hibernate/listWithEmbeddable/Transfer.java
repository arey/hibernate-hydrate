package com.javaetmoi.core.persistence.hibernate.listWithEmbeddable;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Embeddable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Embeddable
public class Transfer {

    private String  name;
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "SUBPLAN")
    private SubPlan subPlan;

    public SubPlan getSubPlan() {
        return subPlan;
    }

    public void setSubPlan(SubPlan subPlan) {
        this.subPlan = subPlan;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
