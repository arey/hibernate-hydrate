package com.javaetmoi.core.persistence.hibernate;

import com.javaetmoi.core.persistence.hibernate.array.ArrayHolder;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the hydration of persistent arrays.
 */
class TestArray extends AbstractTest {
    /**
     * Arrays of basic values.
     */
    @Test
    void basicArrays() {
        var dbHolder = findDeepHydratedEntity(ArrayHolder.class, 1);

        // At this step, transaction and session are closed.
        assertThat(dbHolder.getNames()).containsExactly("first", "second");
        assertThat(dbHolder.getNumbers()).containsExactly(1, 2);
    }

    /**
     * Array of entities.
     */
    @Test
    void entityArray() {
        var dbHolder = findDeepHydratedEntity(ArrayHolder.class, 1);

        // At this step, transaction and session are closed.
        assertThat(dbHolder.getElements()).hasSize(2);
        assertThat(dbHolder.getElements()[0].getTags()).containsExactly("first");
        assertThat(dbHolder.getElements()[1].getTags()).containsExactly("second");
    }

    /**
     * Excluded array of entities.
     */
    @Test
    void entityArray_withExclude() {
        var dbHolder = doInJPA(entityManager ->
                hydrator.withExclude(ArrayHolder.class, "elements")
                        .deepHydrate(entityManager.find(ArrayHolder.class, 1)));

        // At this step, transaction and session are closed.
        assertThat(dbHolder.getElements()).hasSize(2);
        assertThat(Hibernate.isInitialized(dbHolder.getElements()[0].getTags())).isFalse();
    }
}
