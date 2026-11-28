package com.javaetmoi.core.persistence.hibernate;

import com.javaetmoi.core.persistence.hibernate.mapping.EmbeddedHolder;
import com.javaetmoi.core.persistence.hibernate.mapping.Outer;
import com.javaetmoi.core.persistence.hibernate.mapping.Part;
import com.javaetmoi.core.persistence.hibernate.mapping.Target;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

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
        var value = new Target(50);
        persist(association, element, nested, value,
                new EmbeddedHolder(1, new Outer(
                        association, List.of(element), Map.of("key", value), new Part(nested))),
                // Holder with an empty embeddable.
                new EmbeddedHolder(2, new Outer(null, List.of(), Map.of(), null)));
    }

    /**
     * Without hydration the attributes of the embeddable are not initialized.
     */
    @Test
    void notHydrated() {
        var dbOuter = findEntity(EmbeddedHolder.class, 1).getOuter();

        assertThat(Hibernate.isInitialized(dbOuter.getTarget())).isFalse();
        assertThat(Hibernate.isInitialized(dbOuter.getTargets())).isFalse();
        assertThat(Hibernate.isInitialized(dbOuter.getMap())).isFalse();
        assertThat(Hibernate.isInitialized(dbOuter.getPart().getTarget())).isFalse();
    }

    @Test
    void association() {
        var dbOuter = findDeepHydratedOuter(hydrator);

        assertThat(dbOuter.getTarget().getTags()).containsExactly("tag10");
    }

    @Test
    void collection() {
        var dbOuter = findDeepHydratedOuter(hydrator);

        assertThat(dbOuter.getTargets()).flatExtracting(Target::getTags).containsExactly("tag20");
    }

    @Test
    void map() {
        var dbOuter = findDeepHydratedOuter(hydrator);

        assertThat(dbOuter.getMap().get("key").getTags()).containsExactly("tag50");
    }

    @Test
    void nestedEmbeddable() {
        var dbOuter = findDeepHydratedOuter(hydrator);

        assertThat(dbOuter.getPart().getTarget().getTags()).containsExactly("tag30");
    }

    /**
     * Excluding the embedded attribute excludes all of its attributes.
     */
    @Test
    void withExclude() {
        var dbOuter = findDeepHydratedOuter(hydrator.withExclude(EmbeddedHolder.class, "outer"));

        assertThat(Hibernate.isInitialized(dbOuter.getTarget())).isFalse();
        assertThat(Hibernate.isInitialized(dbOuter.getTargets())).isFalse();
        assertThat(Hibernate.isInitialized(dbOuter.getMap())).isFalse();
        assertThat(Hibernate.isInitialized(dbOuter.getPart().getTarget())).isFalse();
    }

    @Test
    void emptyEmbeddable() {
        var dbOuter = findDeepHydratedEntity(EmbeddedHolder.class, 2).getOuter();

        assertThat(dbOuter.getTarget()).isNull();
        assertThat(dbOuter.getTargets()).isEmpty();
        assertThat(dbOuter.getMap()).isEmpty();
        assertThat(dbOuter.getPart()).isNull();
    }

    private Outer findDeepHydratedOuter(Hydrator hydrator) {
        return findDeepHydratedEntity(hydrator, EmbeddedHolder.class, 1).getOuter();
    }
}
