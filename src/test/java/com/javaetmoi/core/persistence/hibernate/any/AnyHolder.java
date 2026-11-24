package com.javaetmoi.core.persistence.hibernate.any;

import java.util.List;

import org.hibernate.annotations.Any;
import org.hibernate.annotations.AnyDiscriminatorValue;
import org.hibernate.annotations.AnyKeyJavaClass;
import org.hibernate.annotations.ManyToAny;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;

import static jakarta.persistence.FetchType.LAZY;

@Entity
public class AnyHolder {
    @Id
    private Integer id;

    @Any(fetch = LAZY)
    @AnyKeyJavaClass(Integer.class)
    @AnyDiscriminatorValue(discriminator = "T", entity = AnyTarget.class)
    @Column(name = "TARGET_TYPE")
    @JoinColumn(name = "TARGET_ID")
    private Object target;

    @ManyToAny(fetch = LAZY)
    @AnyKeyJavaClass(Integer.class)
    @AnyDiscriminatorValue(discriminator = "T", entity = AnyTarget.class)
    @Column(name = "TARGET_TYPE")
    @JoinTable(
            name = "ANY_HOLDER_TARGETS",
            joinColumns = @JoinColumn(name = "HOLDER_ID"),
            inverseJoinColumns = @JoinColumn(name = "TARGET_ID"))
    private List<Object> targets;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Object getTarget() {
        return target;
    }

    public void setTarget(Object target) {
        this.target = target;
    }

    public List<Object> getTargets() {
        return targets;
    }

    public void setTargets(List<Object> targets) {
        this.targets = targets;
    }
}
