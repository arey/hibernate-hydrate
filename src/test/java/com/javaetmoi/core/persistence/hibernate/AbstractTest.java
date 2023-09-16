package com.javaetmoi.core.persistence.hibernate;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.stat.Statistics;
import org.hibernate.testing.transaction.TransactionUtil;
import org.hibernate.testing.transaction.TransactionUtil.JPATransactionFunction;
import org.junit.jupiter.api.BeforeEach;

import java.util.List;
import java.util.function.Consumer;

import static com.javaetmoi.core.persistence.hibernate.Hydrator.hydrator;
import static jakarta.persistence.Persistence.createEntityManagerFactory;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

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
        dbUnitLoader.loadDatabase(getClass());

        // Reset Hibernate statistics.
        statistics().clear();
    }

    //
    // Interface.
    //

    protected Statistics statistics() {
        return sessionFactory.getStatistics();
    }

    protected <E> E findEntity(Class<E> entityClass, long entityId) {
        return doInJPA(entityManager ->
                entityManager.find(entityClass, entityId));
    }

    protected <E> E findDeepHydratedEntity(Class<E> entityClass, long entityId) {
        return doInJPA(entityManager ->
                hydrator.deepHydrate(entityManager.find(entityClass, entityId)));
    }

    protected <E> List<E> findDeepHydratedEntities(Class<E> entityClass, long... entityIds) {
        return doInJPA(entityManager -> {
            var entities = stream(entityIds)
                    .mapToObj(id -> entityManager.find(entityClass, id))
                    .collect(toList());
            return hydrator.deepHydrateCollection(entities);
        });
    }

    protected <E> E findDeepHydratedEntityReference(Class<E> entityClass, long entityId) {
        return doInJPA(entityManager -> {
            var reference = entityManager.getReference(entityClass, entityId);
            assertProxy(entityClass, reference);
            return hydrator.deepHydrate(reference);
        });
    }

    protected <E> List<E> findDeepHydratedEntityReferences(Class<E> entityClass, long... entityIds) {
        return doInJPA(entityManager -> {
            var references = stream(entityIds)
                    .mapToObj(id -> entityManager.getReference(entityClass, id))
                    .collect(toList());
            references.forEach(entity -> assertProxy(entityClass, entity));
            return hydrator.deepHydrateCollection(references);
        });
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