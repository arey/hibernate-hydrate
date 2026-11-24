package com.javaetmoi.core.persistence.hibernate;

import com.javaetmoi.core.persistence.hibernate.any.AnyHolder;
import com.javaetmoi.core.persistence.hibernate.any.AnyTarget;
import org.hibernate.Hibernate;
import org.hibernate.annotations.Any;
import org.hibernate.annotations.ManyToAny;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the hydration of {@link Any} and {@link ManyToAny} associations.
 */
class TestAny extends AbstractTest {
    @Test
    void any() {
        var dbHolder = findDeepHydratedEntity(AnyHolder.class, 1);

        // At this step, transaction and session are closed.
        assertThat(Hibernate.isInitialized(dbHolder.getTarget())).isTrue();
        var dbTarget = (AnyTarget) Hibernate.unproxy(dbHolder.getTarget());
        assertThat(dbTarget.getId()).isEqualTo(10);
        assertThat(dbTarget.getTags()).containsExactly("any");
    }

    @Test
    void manyToAny() {
        var dbHolder = findDeepHydratedEntity(AnyHolder.class, 1);

        // At this step, transaction and session are closed.
        assertThat(dbHolder.getTargets()).hasSize(1);
        var dbElement = dbHolder.getTargets().get(0);
        assertThat(Hibernate.isInitialized(dbElement)).isTrue();
        var dbTarget = (AnyTarget) Hibernate.unproxy(dbElement);
        assertThat(dbTarget.getId()).isEqualTo(20);
        assertThat(dbTarget.getTags()).containsExactly("manyToAny");
    }

    @Test
    void any_withExclude() {
        var dbHolder = doInJPA(entityManager ->
                hydrator.withExclude(AnyHolder.class, "target")
                        .deepHydrate(entityManager.find(AnyHolder.class, 1)));

        // At this step, transaction and session are closed.
        assertThat(Hibernate.isInitialized(dbHolder.getTarget())).isFalse();
        assertThat(Hibernate.isInitialized(dbHolder.getTargets())).isTrue();
    }
}
