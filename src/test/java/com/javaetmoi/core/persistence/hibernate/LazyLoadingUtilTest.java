package com.javaetmoi.core.persistence.hibernate;

import java.util.List;

import com.javaetmoi.core.persistence.hibernate.mapping.Target;
import com.javaetmoi.core.persistence.hibernate.mapping.ToOneHolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

        // At this step, transaction and session are closed.
        assertThat(dbHolder.getManyToOne().getTags()).containsExactly("tag10");
    }

    @Test
    void deepHydrate_entityManagerFactory() {
        var dbHolder = doInJPA(entityManager ->
                LazyLoadingUtil.deepHydrate(
                        entityManager.getEntityManagerFactory(), entityManager.find(ToOneHolder.class, 1)));

        // At this step, transaction and session are closed.
        assertThat(dbHolder.getManyToOne().getTags()).containsExactly("tag10");
    }

    @Test
    void deepHydrate_entityManager_collection() {
        var dbHolders = doInJPA(entityManager ->
                LazyLoadingUtil.deepHydrate(entityManager, List.of(entityManager.find(ToOneHolder.class, 1))));

        // At this step, transaction and session are closed.
        assertThat(dbHolders).hasSize(1);
        assertThat(dbHolders.get(0).getManyToOne().getTags()).containsExactly("tag10");
    }

    @Test
    void deepHydrate_entityManagerFactory_collection() {
        var dbHolders = doInJPA(entityManager ->
                LazyLoadingUtil.deepHydrate(
                        entityManager.getEntityManagerFactory(), List.of(entityManager.find(ToOneHolder.class, 1))));

        // At this step, transaction and session are closed.
        assertThat(dbHolders).hasSize(1);
        assertThat(dbHolders.get(0).getManyToOne().getTags()).containsExactly("tag10");
    }
}
