package com.javaetmoi.core.persistence.hibernate;

import com.javaetmoi.core.persistence.hibernate.compositeId.EmbeddedIdLink;
import com.javaetmoi.core.persistence.hibernate.compositeId.EmbeddedLinkId;
import com.javaetmoi.core.persistence.hibernate.compositeId.IdClassLink;
import com.javaetmoi.core.persistence.hibernate.compositeId.IdClassLinkId;
import com.javaetmoi.core.persistence.hibernate.compositeId.LinkTarget;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.Test;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.IdClass;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests the hydration of associations that are part of a composite identifier.
 */
class TestCompositeId extends AbstractTest {
    /**
     * Association in an {@link IdClass} identifier.
     */
    @Test
    void idClass() {
        var dbLink = doInJPA(entityManager -> {
            var link = entityManager.find(IdClassLink.class, new IdClassLinkId(10, 1));
            assertThat(Hibernate.isInitialized(link.getTarget())).isFalse();
            return hydrator.deepHydrate(link);
        });

        // At this step, transaction and session are closed.
        assertThat(Hibernate.isInitialized(dbLink.getTarget())).isTrue();
        assertThat(dbLink.getTarget().getTags()).containsExactly("idClass");
    }

    /**
     * Excluded association in an {@link IdClass} identifier.
     */
    @Test
    void idClass_withExclude() {
        var dbLink = findDeepHydratedEntity(
                hydrator.withExclude(IdClassLink.class, "target"), IdClassLink.class, new IdClassLinkId(10, 1));

        // At this step, transaction and session are closed.
        assertThat(Hibernate.isInitialized(dbLink.getTarget())).isFalse();
    }

    /**
     * Association in an {@link EmbeddedId} identifier.
     */
    @Test
    void embeddedId() {
        var dbLink = doInJPA(entityManager -> {
            var id = new EmbeddedLinkId(entityManager.getReference(LinkTarget.class, 20), 2);
            var link = entityManager.find(EmbeddedIdLink.class, id);
            assertThat(Hibernate.isInitialized(link.getId().getTarget())).isFalse();
            return hydrator.deepHydrate(link);
        });

        // At this step, transaction and session are closed.
        assertThat(Hibernate.isInitialized(dbLink.getId().getTarget())).isTrue();
        assertThat(dbLink.getId().getTarget().getTags()).containsExactly("embeddedId");
    }

    /**
     * Excluded association in an {@link EmbeddedId} identifier.
     */
    @Test
    void embeddedId_withExclude() {
        var dbLink = doInJPA(entityManager -> {
            var id = new EmbeddedLinkId(entityManager.getReference(LinkTarget.class, 20), 2);
            return hydrator.withExclude(EmbeddedIdLink.class, "id.target")
                    .deepHydrate(entityManager.find(EmbeddedIdLink.class, id));
        });

        // At this step, transaction and session are closed.
        assertThat(Hibernate.isInitialized(dbLink.getId().getTarget())).isFalse();
    }

    /**
     * Excluded {@link EmbeddedId} identifier.
     */
    @Test
    void embeddedId_withExcludedIdentifier() {
        var dbLink = doInJPA(entityManager -> {
            var id = new EmbeddedLinkId(entityManager.getReference(LinkTarget.class, 20), 2);
            return hydrator.withExclude(EmbeddedIdLink.class, "id")
                    .deepHydrate(entityManager.find(EmbeddedIdLink.class, id));
        });

        // At this step, transaction and session are closed.
        assertThat(Hibernate.isInitialized(dbLink.getId().getTarget())).isFalse();
    }

    /**
     * An entity with an {@link EmbeddedId} identifier has no other identifier attributes.
     */
    @Test
    void embeddedId_withUnknownExclude() {
        assertThatThrownBy(() -> hydrator.withExclude(EmbeddedIdLink.class, "target"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("target");
    }

    /**
     * New entities without an identifier must not fail.
     */
    @Test
    void embeddedId_newEntity() {
        var link = new EmbeddedIdLink();

        assertThat(hydrator.deepHydrate(link)).isSameAs(link);
    }
}
