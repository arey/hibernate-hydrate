package com.javaetmoi.core.persistence.hibernate;

import com.javaetmoi.core.persistence.hibernate.mapping.Target;
import com.javaetmoi.core.persistence.hibernate.mapping.ToOneHolder;
import com.javaetmoi.core.persistence.hibernate.mapping.ToOneInverse;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the hydration of to-one associations.
 */
class TestToOne extends AbstractTest {
    @BeforeEach
    void setUpEntities() {
        var manyToOne = new Target(10);
        var oneToOne = new Target(20);
        var inverse = new Target(30);
        var holder = new ToOneHolder(1, manyToOne, oneToOne);
        persist(manyToOne, oneToOne, inverse, holder, new ToOneInverse(2, holder, inverse),
                // Holder without any associations.
                new ToOneHolder(3, null, null));
    }

    /**
     * Without hydration the associations are not initialized.
     */
    @Test
    void notHydrated() {
        var dbHolder = findEntity(ToOneHolder.class, 1);

        assertThat(Hibernate.isInitialized(dbHolder.getManyToOne())).isFalse();
        assertThat(Hibernate.isInitialized(dbHolder.getOneToOne())).isFalse();
        assertThat(Hibernate.isInitialized(dbHolder.getInverse().getTarget())).isFalse();
    }

    @Test
    void manyToOne() {
        var dbHolder = findDeepHydratedEntity(ToOneHolder.class, 1);

        assertThat(dbHolder.getManyToOne().getTags()).containsExactly("tag10");
    }

    @Test
    void manyToOne_withExclude() {
        var dbHolder = findDeepHydratedEntity(
                hydrator.withExclude(ToOneHolder.class, "manyToOne"), ToOneHolder.class, 1);

        assertThat(Hibernate.isInitialized(dbHolder.getManyToOne())).isFalse();
    }

    @Test
    void oneToOne() {
        var dbHolder = findDeepHydratedEntity(ToOneHolder.class, 1);

        assertThat(dbHolder.getOneToOne().getTags()).containsExactly("tag20");
    }

    @Test
    void oneToOne_withExclude() {
        var dbHolder = findDeepHydratedEntity(
                hydrator.withExclude(ToOneHolder.class, "oneToOne"), ToOneHolder.class, 1);

        assertThat(Hibernate.isInitialized(dbHolder.getOneToOne())).isFalse();
    }

    /**
     * The inverse side of a bidirectional association is hydrated, and the cycle back to the holder ends.
     */
    @Test
    void oneToOne_inverse() {
        var dbHolder = findDeepHydratedEntity(ToOneHolder.class, 1);

        assertThat(dbHolder.getInverse().getTarget().getTags()).containsExactly("tag30");
        assertThat(dbHolder.getInverse().getHolder()).isSameAs(dbHolder);
    }

    @Test
    void oneToOne_inverse_withExclude() {
        var dbHolder = findDeepHydratedEntity(
                hydrator.withExclude(ToOneHolder.class, "inverse"), ToOneHolder.class, 1);

        assertThat(Hibernate.isInitialized(dbHolder.getInverse().getTarget())).isFalse();
    }

    @Test
    void nullAssociations() {
        var dbHolder = findDeepHydratedEntity(ToOneHolder.class, 3);

        assertThat(dbHolder.getManyToOne()).isNull();
        assertThat(dbHolder.getOneToOne()).isNull();
        assertThat(dbHolder.getInverse()).isNull();
    }
}
