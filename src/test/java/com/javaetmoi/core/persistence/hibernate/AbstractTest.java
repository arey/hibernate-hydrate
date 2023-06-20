package com.javaetmoi.core.persistence.hibernate;

import jakarta.persistence.EntityManagerFactory;

import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.hibernate.testing.transaction.TransactionUtil;
import org.hibernate.testing.transaction.TransactionUtil.JPATransactionFunction;
import org.junit.jupiter.api.BeforeEach;

import static com.javaetmoi.core.persistence.hibernate.JpaLazyLoadingUtil.deepHydrate;
import static jakarta.persistence.Persistence.createEntityManagerFactory;
import static org.hibernate.testing.transaction.TransactionUtil.doInJPA;

public class AbstractTest {

    private final EntityManagerFactory entityManagerFactory =
            createEntityManagerFactory("hibernate-hydrate");
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
        return entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
    }

    protected <E> E findDeepHydratedEntity(Class<E> entityClass, long entityId) {
        return doInJPA(entityManager ->
                deepHydrate(entityManager,
                        entityManager.find(entityClass, entityId)));
    }

    protected <E> E findEntity(Class<E> entityClass, long entityId) {
        return doInJPA(entityManager ->
                entityManager.find(entityClass, entityId));
    }

    protected <R> R doInJPA(JPATransactionFunction<R> action) {
        return TransactionUtil.doInJPA(() -> entityManagerFactory, action);
    }
}