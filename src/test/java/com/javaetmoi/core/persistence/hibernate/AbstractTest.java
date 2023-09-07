package com.javaetmoi.core.persistence.hibernate;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import org.hibernate.SessionFactory;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.stat.Statistics;
import org.hibernate.testing.transaction.TransactionUtil;
import org.hibernate.testing.transaction.TransactionUtil.JPATransactionFunction;
import org.junit.jupiter.api.BeforeEach;

import java.util.function.Consumer;

import static com.javaetmoi.core.persistence.hibernate.JpaLazyLoadingUtil.deepHydrate;
import static jakarta.persistence.Persistence.createEntityManagerFactory;
import static org.assertj.core.api.Assertions.assertThat;

public class AbstractTest {

    protected final EntityManagerFactory entityManagerFactory =
            createEntityManagerFactory("hibernate-hydrate");
    protected final SessionFactory sessionFactory =
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

    protected <E> E findDeepHydratedEntity(Class<E> entityClass, long entityId) {
        return doInJPA(entityManager ->
                deepHydrate(entityManager,
                        entityManager.find(entityClass, entityId)));
    }

    protected <E> E findDeepHydratedEntityReference(Class<E> entityClass, long entityId) {
        return doInJPA(entityManager -> {
            var reference = entityManager.getReference(entityClass, entityId);
            // Ensure that we got a proxy.
            assertThat(reference)
                    .isInstanceOf(HibernateProxy.class)
                    .extracting(Object::getClass).isNotEqualTo(entityClass);
            return deepHydrate(entityManager, reference);
        });
    }

    protected <E> E findEntity(Class<E> entityClass, long entityId) {
        return doInJPA(entityManager ->
                entityManager.find(entityClass, entityId));
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