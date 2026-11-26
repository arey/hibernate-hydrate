package com.javaetmoi.core.persistence.hibernate.enhanced;

import java.util.List;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;

import static jakarta.persistence.FetchType.LAZY;

/**
 * Owning side of the one-to-one association to {@link EnhancedOwner}.
 */
@Entity
public class EnhancedInverse {
    @Id
    private Integer id;

    @OneToOne(fetch = LAZY)
    private EnhancedOwner owner;

    @ElementCollection
    private List<String> tags;

    protected EnhancedInverse() {}

    public EnhancedInverse(Integer id, EnhancedOwner owner, List<String> tags) {
        this.id = id;
        this.owner = owner;
        this.tags = tags;
    }

    public Integer getId() {
        return id;
    }

    public EnhancedOwner getOwner() {
        return owner;
    }

    public List<String> getTags() {
        return tags;
    }
}
