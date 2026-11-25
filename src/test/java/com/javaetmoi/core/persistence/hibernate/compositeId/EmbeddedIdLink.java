package com.javaetmoi.core.persistence.hibernate.compositeId;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;

@Entity
public class EmbeddedIdLink {
    @EmbeddedId
    private EmbeddedLinkId id;

    public EmbeddedLinkId getId() {
        return id;
    }

    public void setId(EmbeddedLinkId id) {
        this.id = id;
    }
}
