package com.javaetmoi.core.persistence.hibernate.enhanced;

import jakarta.persistence.Basic;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import org.hibernate.annotations.LazyGroup;

import static jakarta.persistence.FetchType.LAZY;

/**
 * Entity with attributes that are just lazy, if the entity is bytecode enhanced.
 * The attributes are in different lazy groups, because Hibernate loads all attributes of a lazy group at once.
 */
@Entity
public class EnhancedOwner {
    @Id
    private Integer id;

    @Basic(fetch = LAZY)
    @LazyGroup("content")
    private String content;

    @OneToOne(mappedBy = "owner", fetch = LAZY)
    @LazyGroup("inverse")
    private EnhancedInverse inverse;

    public EnhancedOwner() {
    }

    public EnhancedOwner(Integer id, String content) {
        this.id = id;
        this.content = content;
    }

    public Integer getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public EnhancedInverse getInverse() {
        return inverse;
    }
}
