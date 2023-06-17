package com.javaetmoi.core.persistence.hibernate;

import javax.sql.DataSource;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import org.h2.jdbcx.JdbcDataSource;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;

import static jakarta.persistence.Persistence.createEntityManagerFactory;

public class AbstractTest {
    public static final String DATABASE_URL = "jdbc:h2:~/hibernate-hydrate";

    private final DataSource dataSource = dataSource();
    private final DBUnitLoader dbUnitLoader = dbUnitLoader(dataSource);
    protected final EntityManagerFactory entityManagerFactory = entityManagerFactory(dataSource);
    protected EntityManager entityManager = entityManagerFactory.createEntityManager();

    /**
     * Populate entities graph and embedded database
     */
    @BeforeEach
    void setUpDatabase() {
        dbUnitLoader.loadDatabase(getClass());

        // Reset Hibernate Statistics
        entityManagerFactory.unwrap(SessionFactory.class).getStatistics().clear();
    }

    protected <E> E findDeepHydratedEntity(Class<E> entityClass, long entityId) {
        return transactional(entityManager ->
                JpaLazyLoadingUtil.deepHydrate(entityManager,
                        entityManager.find(entityClass, entityId)));
    }

    protected <E> E findEntity(Class<E> entityClass, long entityId) {
        return transactional(entityManager ->
                entityManager.find(entityClass, entityId));
    }

    private static DataSource dataSource() {
        var dataSource = new JdbcDataSource();
        dataSource.setURL(DATABASE_URL);
        return dataSource;
    }

    private static DBUnitLoader dbUnitLoader(DataSource dataSource) {
        return new DBUnitLoader(dataSource);
    }

    private static EntityManagerFactory entityManagerFactory(DataSource dataSource) {
        return createEntityManagerFactory("hibernate-hydrate");
    }

    protected <R> R transactional(Transactional<R> action) {
        var transaction = entityManager.getTransaction();
        transaction.begin();
        var result = action.doInTransaction(entityManager);
        transaction.commit();
        return result;
    }

    @FunctionalInterface
    protected static interface Transactional<R> {
        R doInTransaction(EntityManager entityManager);
    }
}