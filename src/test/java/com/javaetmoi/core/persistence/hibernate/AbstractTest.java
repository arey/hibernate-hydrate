package com.javaetmoi.core.persistence.hibernate;

import java.util.function.Function;

import javax.sql.DataSource;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import org.hibernate.SessionFactory;
import org.hibernate.internal.SessionFactoryImpl;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;

import static com.javaetmoi.core.persistence.hibernate.JpaLazyLoadingUtil.deepHydrate;
import static jakarta.persistence.Persistence.createEntityManagerFactory;

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
        return transactional(entityManager ->
                deepHydrate(entityManager,
                        entityManager.find(entityClass, entityId)));
    }

    protected <E> E findEntity(Class<E> entityClass, long entityId) {
        return transactional(entityManager ->
                entityManager.find(entityClass, entityId));
    }

    protected <R> R transactional(Function<EntityManager, R> action) {
        var entityManager = entityManagerFactory.createEntityManager();
        var transaction = entityManager.getTransaction();
        transaction.begin();
        var result = action.apply(entityManager);
        transaction.commit();
        // For Hibernate 6.1 / JPA 3.0 compatibility we cannot use try with resources for this.
        entityManager.close();
        return result;
    }
}