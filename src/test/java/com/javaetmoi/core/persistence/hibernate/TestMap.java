package com.javaetmoi.core.persistence.hibernate;

import com.javaetmoi.core.persistence.hibernate.mapping.MapHolder;
import com.javaetmoi.core.persistence.hibernate.mapping.Part;
import com.javaetmoi.core.persistence.hibernate.mapping.Target;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the hydration of maps.
 */
class TestMap extends AbstractTest {
    @BeforeEach
    void setUpEntities() {
        var key = new Target(10);
        var value = new Target(20);
        var byId = new Target(30);
        var embeddable = new Target(40);
        var embeddableKey = new Target(50);
        persist(key, value, byId, embeddable, embeddableKey,
                new MapHolder(1, Map.of(key, value), Map.of(30, byId),
                        Map.of("key", "basic"), Map.of("key", new Part(embeddable)),
                        Map.of(new Part(embeddableKey), "value")),
                // Holder with empty maps.
                new MapHolder(2, Map.of(), Map.of(), Map.of(), Map.of(), Map.of()));
    }

    /**
     * Without hydration the maps are not initialized.
     */
    @Test
    void notHydrated() {
        var dbHolder = findEntity(MapHolder.class, 1);

        assertThat(Hibernate.isInitialized(dbHolder.getEntities())).isFalse();
        assertThat(Hibernate.isInitialized(dbHolder.getById())).isFalse();
        assertThat(Hibernate.isInitialized(dbHolder.getBasics())).isFalse();
        assertThat(Hibernate.isInitialized(dbHolder.getEmbeddables())).isFalse();
        assertThat(Hibernate.isInitialized(dbHolder.getEmbeddableKeys())).isFalse();
    }

    @Test
    void entityKeys() {
        var dbHolder = findDeepHydratedEntity(MapHolder.class, 1);

        assertThat(dbHolder.getEntities().keySet()).flatExtracting(Target::getTags).containsExactly("tag10");
    }

    @Test
    void entityValues() {
        var dbHolder = findDeepHydratedEntity(MapHolder.class, 1);

        assertThat(dbHolder.getEntities().values()).flatExtracting(Target::getTags).containsExactly("tag20");
    }

    /**
     * Map with an attribute of its values as key.
     */
    @Test
    void mapKey() {
        var dbHolder = findDeepHydratedEntity(MapHolder.class, 1);

        assertThat(dbHolder.getById().get(30).getTags()).containsExactly("tag30");
    }

    @Test
    void basicValues() {
        var dbHolder = findDeepHydratedEntity(MapHolder.class, 1);

        assertThat(dbHolder.getBasics()).containsExactly(Map.entry("key", "basic"));
    }

    @Test
    void embeddableValues() {
        var dbHolder = findDeepHydratedEntity(MapHolder.class, 1);

        assertThat(dbHolder.getEmbeddables().get("key").getTarget().getTags()).containsExactly("tag40");
    }

    @Test
    void embeddableKeys() {
        var dbHolder = findDeepHydratedEntity(MapHolder.class, 1);

        assertThat(dbHolder.getEmbeddableKeys().keySet())
                .extracting(Part::getTarget).flatExtracting(Target::getTags).containsExactly("tag50");
    }

    @Test
    void emptyMaps() {
        var dbHolder = findDeepHydratedEntity(MapHolder.class, 2);

        assertThat(dbHolder.getEntities()).isEmpty();
        assertThat(dbHolder.getById()).isEmpty();
        assertThat(dbHolder.getBasics()).isEmpty();
        assertThat(dbHolder.getEmbeddables()).isEmpty();
        assertThat(dbHolder.getEmbeddableKeys()).isEmpty();
    }

    @Test
    void withExclude() {
        var dbHolder = findDeepHydratedEntity(
                hydrator.withExclude(MapHolder.class, "entities"), MapHolder.class, 1);

        assertThat(Hibernate.isInitialized(dbHolder.getEntities())).isFalse();
    }
}
