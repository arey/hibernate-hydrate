package com.javaetmoi.core.persistence.hibernate;

import com.javaetmoi.core.persistence.hibernate.compositeId.EmbeddedIdLink;
import com.javaetmoi.core.persistence.hibernate.compositeId.EmbeddedLinkId;
import com.javaetmoi.core.persistence.hibernate.compositeId.IdClassLink;
import com.javaetmoi.core.persistence.hibernate.compositeId.IdClassLinkId;
import com.javaetmoi.core.persistence.hibernate.mapping.Target;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.EntityManager;
import jakarta.persistence.IdClass;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the hydration of associations that are part of a composite identifier.
 */
class TestCompositeId extends AbstractTest {
    private static final IdClassLinkId ID_CLASS_LINK_ID = new IdClassLinkId(10, 1);

    @BeforeEach
    void setUpEntities() {
        var idClass = new Target(10);
        var embeddedId = new Target(20);
        persist(idClass, embeddedId,
                new IdClassLink(idClass, 1),
                new EmbeddedIdLink(new EmbeddedLinkId(embeddedId, 2)));
    }

    /**
     * Without hydration the associations are not initialized.
     */
    @Test
    void notHydrated() {
        var dbIdClassLink = findEntity(IdClassLink.class, ID_CLASS_LINK_ID);
        var dbEmbeddedIdLink = doInJPA(this::findEmbeddedIdLink);

        assertThat(Hibernate.isInitialized(dbIdClassLink.getTarget())).isFalse();
        assertThat(Hibernate.isInitialized(dbEmbeddedIdLink.getId().getTarget())).isFalse();
    }

    /**
     * Association in an {@link IdClass} identifier.
     */
    @Test
    void idClass() {
        var dbLink = findDeepHydratedEntity(IdClassLink.class, ID_CLASS_LINK_ID);

        assertThat(dbLink.getTarget().getTags()).containsExactly("tag10");
    }

    @Test
    void idClass_withExclude() {
        var dbLink = findDeepHydratedEntity(
                hydrator.withExclude(IdClassLink.class, "target"), IdClassLink.class, ID_CLASS_LINK_ID);

        assertThat(Hibernate.isInitialized(dbLink.getTarget())).isFalse();
    }

    /**
     * Association in an {@link EmbeddedId} identifier.
     */
    @Test
    void embeddedId() {
        var dbLink = findDeepHydratedEmbeddedIdLink(hydrator);

        assertThat(dbLink.getId().getTarget().getTags()).containsExactly("tag20");
    }

    @Test
    void embeddedId_withExclude() {
        var dbLink = findDeepHydratedEmbeddedIdLink(
                hydrator.withExclude(EmbeddedIdLink.class, "id.target"));

        assertThat(Hibernate.isInitialized(dbLink.getId().getTarget())).isFalse();
    }

    /**
     * Excluding the {@link EmbeddedId} identifier excludes all of its attributes.
     */
    @Test
    void embeddedId_withExcludeOfIdentifier() {
        var dbLink = findDeepHydratedEmbeddedIdLink(
                hydrator.withExclude(EmbeddedIdLink.class, "id"));

        assertThat(Hibernate.isInitialized(dbLink.getId().getTarget())).isFalse();
    }

    /**
     * The attributes of an {@link EmbeddedId} identifier are no attributes of the entity.
     */
    @Test
    void embeddedId_withUnknownExclude() {
        assertInvalidExclude(EmbeddedIdLink.class, "target")
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

    private EmbeddedIdLink findDeepHydratedEmbeddedIdLink(Hydrator hydrator) {
        return doInJPA(entityManager ->
                hydrator.deepHydrate(findEmbeddedIdLink(entityManager)));
    }

    /**
     * Find the entity with the {@link EmbeddedId} identifier.
     * The identifier needs a reference to its target of the same session.
     */
    private EmbeddedIdLink findEmbeddedIdLink(EntityManager entityManager) {
        var id = new EmbeddedLinkId(entityManager.getReference(Target.class, 20), 2);
        return entityManager.find(EmbeddedIdLink.class, id);
    }
}
