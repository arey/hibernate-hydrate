package com.javaetmoi.core.persistence.hibernate;

import com.javaetmoi.core.persistence.hibernate.mapping.CollectionHolder;
import com.javaetmoi.core.persistence.hibernate.mapping.Part;
import com.javaetmoi.core.persistence.hibernate.mapping.Target;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the hydration of collections.
 */
class TestCollection extends AbstractTest {
    @BeforeEach
    void setUpEntities() {
        var list = new Target(10);
        var set = new Target(20);
        var manyToMany = new Target(30);
        var embeddable = new Target(40);
        persist(list, set, manyToMany, embeddable,
                new CollectionHolder(1, List.of(list), Set.of(set), List.of(manyToMany),
                        List.of("basic"), List.of(new Part(embeddable))),
                // Holder with empty collections.
                new CollectionHolder(2, List.of(), Set.of(), List.of(), List.of(), List.of()));
    }

    /**
     * Without hydration the collections are not initialized.
     */
    @Test
    void notHydrated() {
        var dbHolder = findEntity(CollectionHolder.class, 1);

        assertThat(Hibernate.isInitialized(dbHolder.getList())).isFalse();
        assertThat(Hibernate.isInitialized(dbHolder.getSet())).isFalse();
        assertThat(Hibernate.isInitialized(dbHolder.getManyToMany())).isFalse();
        assertThat(Hibernate.isInitialized(dbHolder.getBasics())).isFalse();
        assertThat(Hibernate.isInitialized(dbHolder.getEmbeddables())).isFalse();
    }

    @Test
    void list() {
        var dbHolder = findDeepHydratedEntity(CollectionHolder.class, 1);

        assertThat(dbHolder.getList()).flatExtracting(Target::getTags).containsExactly("tag10");
    }

    @Test
    void set() {
        var dbHolder = findDeepHydratedEntity(CollectionHolder.class, 1);

        assertThat(dbHolder.getSet()).flatExtracting(Target::getTags).containsExactly("tag20");
    }

    @Test
    void manyToMany() {
        var dbHolder = findDeepHydratedEntity(CollectionHolder.class, 1);

        assertThat(dbHolder.getManyToMany()).flatExtracting(Target::getTags).containsExactly("tag30");
    }

    @Test
    void basicElements() {
        var dbHolder = findDeepHydratedEntity(CollectionHolder.class, 1);

        assertThat(dbHolder.getBasics()).containsExactly("basic");
    }

    @Test
    void embeddableElements() {
        var dbHolder = findDeepHydratedEntity(CollectionHolder.class, 1);

        assertThat(dbHolder.getEmbeddables()).extracting(Part::getTarget).flatExtracting(Target::getTags).containsExactly("tag40");
    }

    @Test
    void emptyCollections() {
        var dbHolder = findDeepHydratedEntity(CollectionHolder.class, 2);

        assertThat(dbHolder.getList()).isEmpty();
        assertThat(dbHolder.getSet()).isEmpty();
        assertThat(dbHolder.getManyToMany()).isEmpty();
        assertThat(dbHolder.getBasics()).isEmpty();
        assertThat(dbHolder.getEmbeddables()).isEmpty();
    }

    @Test
    void withExclude() {
        var dbHolder = findDeepHydratedEntity(
                hydrator.withExclude(CollectionHolder.class, "list"), CollectionHolder.class, 1);

        assertThat(Hibernate.isInitialized(dbHolder.getList())).isFalse();
    }
}
