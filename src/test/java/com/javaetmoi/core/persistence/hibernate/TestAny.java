package com.javaetmoi.core.persistence.hibernate;

import com.javaetmoi.core.persistence.hibernate.mapping.AnyHolder;
import com.javaetmoi.core.persistence.hibernate.mapping.Target;
import org.hibernate.Hibernate;
import org.hibernate.annotations.Any;
import org.hibernate.annotations.ManyToAny;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the hydration of {@link Any} and {@link ManyToAny} associations.
 */
class TestAny extends AbstractTest {
    @BeforeEach
    void setUpEntities() {
        var any = new Target(10);
        var manyToAny = new Target(20);
        persist(any, manyToAny, new AnyHolder(1, any, List.of(manyToAny)),
                // Holder without any associations.
                new AnyHolder(2, null, List.of()));
    }

    /**
     * Without hydration the associations are not initialized.
     */
    @Test
    void notHydrated() {
        var dbHolder = findEntity(AnyHolder.class, 1);

        assertThat(Hibernate.isInitialized(dbHolder.getTarget())).isFalse();
        assertThat(Hibernate.isInitialized(dbHolder.getTargets())).isFalse();
    }

    @Test
    void any() {
        var dbHolder = findDeepHydratedEntity(AnyHolder.class, 1);

        var dbTarget = (Target) Hibernate.unproxy(dbHolder.getTarget());
        assertThat(dbTarget.getTags()).containsExactly("tag10");
    }

    @Test
    void any_withExclude() {
        var dbHolder = findDeepHydratedEntity(
                hydrator.withExclude(AnyHolder.class, "target"), AnyHolder.class, 1);

        assertThat(Hibernate.isInitialized(dbHolder.getTarget())).isFalse();
    }

    @Test
    void manyToAny() {
        var dbHolder = findDeepHydratedEntity(AnyHolder.class, 1);

        assertThat(dbHolder.getTargets()).hasSize(1);
        var dbTarget = (Target) Hibernate.unproxy(dbHolder.getTargets().get(0));
        assertThat(dbTarget.getTags()).containsExactly("tag20");
    }

    @Test
    void manyToAny_withExclude() {
        var dbHolder = findDeepHydratedEntity(
                hydrator.withExclude(AnyHolder.class, "targets"), AnyHolder.class, 1);

        assertThat(Hibernate.isInitialized(dbHolder.getTargets())).isFalse();
    }

    @Test
    void emptyAssociations() {
        var dbHolder = findDeepHydratedEntity(AnyHolder.class, 2);

        assertThat(dbHolder.getTarget()).isNull();
        assertThat(dbHolder.getTargets()).isEmpty();
    }
}
