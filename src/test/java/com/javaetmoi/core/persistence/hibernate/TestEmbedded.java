package com.javaetmoi.core.persistence.hibernate;

import java.util.List;

import com.javaetmoi.core.persistence.hibernate.mapping.EmbeddedHolder;
import com.javaetmoi.core.persistence.hibernate.mapping.Outer;
import com.javaetmoi.core.persistence.hibernate.mapping.Part;
import com.javaetmoi.core.persistence.hibernate.mapping.Target;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the hydration of embedded attributes.
 */
class TestEmbedded extends AbstractTest {
    @BeforeEach
    void setUpEntities() {
        var association = new Target(10);
        var element = new Target(20);
        var nested = new Target(30);
        persist(association, element, nested,
                new EmbeddedHolder(1, new Outer(association, List.of(element), new Part(nested))),
                // Holder with an empty embeddable.
                new EmbeddedHolder(2, new Outer(null, List.of(), null)));
    }

    /**
     * Without hydration the attributes of the embeddable are not initialized.
     */
    @Test
    void notHydrated() {
        var dbOuter = findEntity(EmbeddedHolder.class, 1).getOuter();

        assertThat(Hibernate.isInitialized(dbOuter.getTarget())).isFalse();
        assertThat(Hibernate.isInitialized(dbOuter.getTargets())).isFalse();
        assertThat(Hibernate.isInitialized(dbOuter.getPart().getTarget())).isFalse();
    }

    @Test
    void association() {
        var dbOuter = findDeepHydratedEntity(EmbeddedHolder.class, 1).getOuter();

        assertThat(dbOuter.getTarget().getTags()).containsExactly("tag10");
    }

    @Test
    void association_withExclude() {
        var dbOuter = findDeepHydratedEntity(
                hydrator.withExclude(EmbeddedHolder.class, "outer.target"), EmbeddedHolder.class, 1).getOuter();

        assertThat(Hibernate.isInitialized(dbOuter.getTarget())).isFalse();
    }

    @Test
    void collection() {
        var dbOuter = findDeepHydratedEntity(EmbeddedHolder.class, 1).getOuter();

        assertThat(dbOuter.getTargets()).hasSize(1);
        assertThat(dbOuter.getTargets().get(0).getTags()).containsExactly("tag20");
    }

    @Test
    void collection_withExclude() {
        var dbOuter = findDeepHydratedEntity(
                hydrator.withExclude(EmbeddedHolder.class, "outer.targets"), EmbeddedHolder.class, 1).getOuter();

        assertThat(Hibernate.isInitialized(dbOuter.getTargets())).isFalse();
    }

    @Test
    void nestedEmbeddable() {
        var dbOuter = findDeepHydratedEntity(EmbeddedHolder.class, 1).getOuter();

        assertThat(dbOuter.getPart().getTarget().getTags()).containsExactly("tag30");
    }

    @Test
    void nestedEmbeddable_withExclude() {
        var dbOuter = findDeepHydratedEntity(
                hydrator.withExclude(EmbeddedHolder.class, "outer.part.target"), EmbeddedHolder.class, 1).getOuter();

        assertThat(Hibernate.isInitialized(dbOuter.getPart().getTarget())).isFalse();
    }

    /**
     * Excluding the embedded attribute excludes all of its attributes.
     */
    @Test
    void withExclude() {
        var dbOuter = findDeepHydratedEntity(
                hydrator.withExclude(EmbeddedHolder.class, "outer"), EmbeddedHolder.class, 1).getOuter();

        assertThat(Hibernate.isInitialized(dbOuter.getTarget())).isFalse();
        assertThat(Hibernate.isInitialized(dbOuter.getTargets())).isFalse();
        assertThat(Hibernate.isInitialized(dbOuter.getPart().getTarget())).isFalse();
    }

    @Test
    void emptyEmbeddable() {
        var dbOuter = findDeepHydratedEntity(EmbeddedHolder.class, 2).getOuter();

        assertThat(dbOuter.getTarget()).isNull();
        assertThat(dbOuter.getTargets()).isEmpty();
        assertThat(dbOuter.getPart()).isNull();
    }
}
