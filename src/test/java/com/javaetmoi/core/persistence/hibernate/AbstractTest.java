package com.javaetmoi.core.persistence.hibernate;

import org.assertj.core.api.AbstractThrowableAssert;
import org.hibernate.SessionFactory;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.stat.Statistics;
import org.hibernate.testing.transaction.TransactionUtil.JPATransactionFunction;
import org.hibernate.testing.transaction.TransactionUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.util.List;
import java.util.function.Consumer;

import static com.javaetmoi.core.persistence.hibernate.Hydrator.hydrator;
import static jakarta.persistence.Persistence.createEntityManagerFactory;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Base of the tests that use the persistence unit of the tests.
 * <p>
 * The {@code find*} methods run in a transaction of their own:
 * When they return, the transaction and the session are closed, so the returned entities are detached.
 * </p>
 */
public class AbstractTest {
    private final EntityManagerFactory entityManagerFactory =
            createEntityManagerFactory("hibernate-hydrate");
    protected final Hydrator hydrator = hydrator(entityManagerFactory);
    private final SessionFactory sessionFactory =
            entityManagerFactory.unwrap(SessionFactory.class);
    private final DBUnitLoader dbUnitLoader =
            new DBUnitLoader((String) entityManagerFactory.getProperties().get("hibernate.connection.url"));

    @BeforeEach
    void setUpDatabase() {
        // Tests without a data set create their entities on their own.
        if (getClass().getResource(getClass().getSimpleName() + "-dataset.xml") != null) {
            dbUnitLoader.loadDatabase(getClass());
        }

        // Reset Hibernate statistics.
        statistics().clear();
    }

    @AfterEach
    void tearDownDatabase() {
        entityManagerFactory.close();
    }

    //
    // Interface.
    //

    protected Statistics statistics() {
        return sessionFactory.getStatistics();
    }

    protected <E> E findEntity(Class<E> entityClass, Object entityId) {
        return doInJPA(entityManager ->
                entityManager.find(entityClass, entityId));
    }

    protected <E> E findDeepHydratedEntity(Class<E> entityClass, Object entityId) {
        return findDeepHydratedEntity(hydrator, entityClass, entityId);
    }

    /**
     * Find an entity and hydrate it with the given hydrator, e.g. one with excludes.
     */
    protected <E> E findDeepHydratedEntity(Hydrator hydrator, Class<E> entityClass, Object entityId) {
        return doInJPA(entityManager ->
                hydrator.deepHydrate(entityManager.find(entityClass, entityId)));
    }

    protected <E> List<E> findDeepHydratedEntities(Class<E> entityClass, Object... entityIds) {
        return doInJPA(entityManager -> {
            var entities = stream(entityIds)
                    .map(id -> entityManager.find(entityClass, id))
                    .collect(toList());
            return hydrator.deepHydrateCollection(entities);
        });
    }

    protected <E> E findDeepHydratedEntityReference(Class<E> entityClass, Object entityId) {
        return doInJPA(entityManager -> {
            var reference = entityManager.getReference(entityClass, entityId);
            assertProxy(entityClass, reference);
            return hydrator.deepHydrate(reference);
        });
    }

    protected <E> List<E> findDeepHydratedEntityReferences(Class<E> entityClass, Object... entityIds) {
        return doInJPA(entityManager -> {
            var references = stream(entityIds)
                    .map(id -> entityManager.getReference(entityClass, id))
                    .collect(toList());
            references.forEach(entity -> assertProxy(entityClass, entity));
            return hydrator.deepHydrateCollection(references);
        });
    }

    /**
     * Persist the entities in a transaction of their own.
     */
    protected void persist(Object... entities) {
        doInJPAVoid(entityManager ->
                stream(entities).forEach(entityManager::persist));
    }

    /**
     * Ensure that the exclude is rejected.
     */
    protected AbstractThrowableAssert<?, ? extends Throwable> assertInvalidExclude(Class<?> entityClass, String attribute) {
        return assertThatThrownBy(() -> hydrator.withExclude(entityClass, attribute))
                .isInstanceOf(IllegalArgumentException.class);
    }

    /**
     * Ensure that we got a proxy.
     */
    private <E> void assertProxy(Class<E> entityClass, E reference) {
        assertThat(reference)
                .isInstanceOf(HibernateProxy.class)
                .extracting(Object::getClass).isNotEqualTo(entityClass);
    }

    protected void doInJPAVoid(Consumer<EntityManager> action) {
        doInJPA(entityManager -> {
            action.accept(entityManager);
            return null;
        });
    }

    protected <R> R doInJPA(JPATransactionFunction<R> action) {
        return TransactionUtil.doInJPA(() -> entityManagerFactory, action);
    }
}