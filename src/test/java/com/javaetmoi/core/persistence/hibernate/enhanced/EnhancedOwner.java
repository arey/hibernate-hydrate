package com.javaetmoi.core.persistence.hibernate.enhanced;

import org.hibernate.annotations.LazyGroup;

import jakarta.persistence.Basic;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;

import static jakarta.persistence.FetchType.LAZY;

/**
 * Entity with attributes that are just lazy, if the entity is bytecode enhanced.
 * Hibernate loads all attributes of a lazy group at once, so the content and the inverse side have lazy groups
 * of their own. The summary and the details share a lazy group to test that.
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

    @Basic(fetch = LAZY)
    @LazyGroup("group")
    private String summary = "summary";

    @Basic(fetch = LAZY)
    @LazyGroup("group")
    private String details = "details";

    protected EnhancedOwner() {}

    public EnhancedOwner(Integer id, String content) {
        this.id = id;
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public EnhancedInverse getInverse() {
        return inverse;
    }
}
