package com.javaetmoi.core.persistence.hibernate;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;

import static com.javaetmoi.core.persistence.hibernate.JpaLazyLoadingUtil.deepHydrate;
import static jakarta.persistence.Persistence.createEntityManagerFactory;

public class AbstractTest {
    public static final String DATABASE_URL = "jdbc:h2:~/hibernate-hydrate";

    private final DBUnitLoader dbUnitLoader =
            new DBUnitLoader(DATABASE_URL);
    private final EntityManagerFactory entityManagerFactory =
            createEntityManagerFactory("hibernate-hydrate");

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
        return transactional(entityManager ->
                deepHydrate(entityManager,
                        entityManager.find(entityClass, entityId)));
    }

    protected <E> E findEntity(Class<E> entityClass, long entityId) {
        return transactional(entityManager ->
                entityManager.find(entityClass, entityId));
    }

    protected <R> R transactional(Transactional<R> action) {
        var entityManager = entityManagerFactory.createEntityManager();
        var transaction = entityManager.getTransaction();
        transaction.begin();
        var result = action.doInTransaction(entityManager);
        transaction.commit();
        // For Hibernate 6.1 / JPA 3.0 compatibility we cannot use try with resources for this.
        entityManager.close();
        return result;
    }

    @FunctionalInterface
    protected static interface Transactional<R> {
        R doInTransaction(EntityManager entityManager);
    }
}