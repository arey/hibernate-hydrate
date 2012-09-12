package com.javaetmoi.core.persistence.hibernate.test3;

import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

@Embeddable
public class Transfer {

    private String  name;
    @ManyToOne
    @JoinColumn(name = "SUBPLAN")
    @Cascade(CascadeType.ALL)
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
