package com.javaetmoi.core.persistence.hibernate;

import com.javaetmoi.core.persistence.hibernate.mapping.ArrayHolder;
import com.javaetmoi.core.persistence.hibernate.mapping.Target;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the hydration of persistent arrays.
 * Arrays themselves are never lazy, but their elements have to be hydrated.
 */
class TestArray extends AbstractTest {
    @BeforeEach
    void setUpEntities() {
        var first = new Target(10);
        var second = new Target(20);
        persist(first, second,
                new ArrayHolder(1, new String[] {"first", "second"}, new int[] {1, 2}, new Target[] {first, second}),
                // Holder with empty arrays.
                new ArrayHolder(2, new String[0], new int[0], new Target[0]));
    }

    /**
     * Arrays are never lazy, but without hydration the tags of their elements are not initialized.
     */
    @Test
    void notHydrated() {
        var dbHolder = findEntity(ArrayHolder.class, 1);

        assertThat(dbHolder.getElements()).hasSize(2);
        assertThat(Hibernate.isInitialized(dbHolder.getElements()[0].getTags())).isFalse();
    }

    @Test
    void basicArray() {
        var dbHolder = findDeepHydratedEntity(ArrayHolder.class, 1);

        assertThat(dbHolder.getNames()).containsExactly("first", "second");
    }

    @Test
    void primitiveArray() {
        var dbHolder = findDeepHydratedEntity(ArrayHolder.class, 1);

        assertThat(dbHolder.getNumbers()).containsExactly(1, 2);
    }

    @Test
    void entityArray() {
        var dbHolder = findDeepHydratedEntity(ArrayHolder.class, 1);

        assertThat(dbHolder.getElements()).hasSize(2);
        assertThat(dbHolder.getElements()[0].getTags()).containsExactly("tag10");
        assertThat(dbHolder.getElements()[1].getTags()).containsExactly("tag20");
    }

    @Test
    void entityArray_withExclude() {
        var dbHolder = findDeepHydratedEntity(
                hydrator.withExclude(ArrayHolder.class, "elements"), ArrayHolder.class, 1);

        assertThat(dbHolder.getElements()).hasSize(2);
        assertThat(Hibernate.isInitialized(dbHolder.getElements()[0].getTags())).isFalse();
    }

    @Test
    void emptyArrays() {
        var dbHolder = findDeepHydratedEntity(ArrayHolder.class, 2);

        assertThat(dbHolder.getNames()).isEmpty();
        assertThat(dbHolder.getNumbers()).isEmpty();
        assertThat(dbHolder.getElements()).isEmpty();
    }
}
