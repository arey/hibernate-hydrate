package com.javaetmoi.core.persistence.hibernate;

import com.javaetmoi.core.persistence.hibernate.mapping.Target;
import com.javaetmoi.core.persistence.hibernate.mapping.ToOneHolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test of the {@link LazyLoadingUtil} class.
 */
class LazyLoadingUtilTest extends AbstractTest {
    @BeforeEach
    void setUpEntities() {
        var target = new Target(10);
        persist(target, new ToOneHolder(1, target, null));
    }

    @Test
    void deepHydrate_entityManager() {
        var dbHolder = doInJPA(entityManager ->
                LazyLoadingUtil.deepHydrate(entityManager, entityManager.find(ToOneHolder.class, 1)));

        assertHydrated(dbHolder);
    }

    @Test
    void deepHydrate_entityManagerFactory() {
        var dbHolder = doInJPA(entityManager ->
                LazyLoadingUtil.deepHydrate(
                        entityManager.getEntityManagerFactory(), entityManager.find(ToOneHolder.class, 1)));

        assertHydrated(dbHolder);
    }

    @Test
    void deepHydrate_entityManager_collection() {
        var dbHolders = doInJPA(entityManager ->
                LazyLoadingUtil.deepHydrate(entityManager, List.of(entityManager.find(ToOneHolder.class, 1))));

        assertThat(dbHolders).singleElement().satisfies(this::assertHydrated);
    }

    @Test
    void deepHydrate_entityManagerFactory_collection() {
        var dbHolders = doInJPA(entityManager ->
                LazyLoadingUtil.deepHydrate(
                        entityManager.getEntityManagerFactory(), List.of(entityManager.find(ToOneHolder.class, 1))));

        assertThat(dbHolders).singleElement().satisfies(this::assertHydrated);
    }

    private void assertHydrated(ToOneHolder dbHolder) {
        assertThat(dbHolder.getManyToOne().getTags()).containsExactly("tag10");
    }
}
