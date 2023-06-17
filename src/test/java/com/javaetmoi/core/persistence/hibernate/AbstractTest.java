package com.javaetmoi.core.persistence.hibernate;

import javax.sql.DataSource;

import org.h2.jdbcx.JdbcDataSource;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.BeforeEach;

public class AbstractTest {
    public static final String DATABASE_URL = "jdbc:h2:~/hibernate-hydrate";

    private final DataSource dataSource = dataSource();
    private final DBUnitLoader dbUnitLoader = dbUnitLoader(dataSource);
    protected final SessionFactory sessionFactory = sessionFactory(dataSource);

    /**
     * Populate entities graph and embedded database
     */
    @BeforeEach
    void setUpDatabase() {
        dbUnitLoader.loadDatabase(getClass());

        // Reset Hibernate Statistics
        sessionFactory.getStatistics().clear();
    }

    private static DataSource dataSource() {
        var dataSource = new JdbcDataSource();
        dataSource.setURL(DATABASE_URL);
        return dataSource;
    }

    private static DBUnitLoader dbUnitLoader(DataSource dataSource) {
        return new DBUnitLoader(dataSource);
    }

    private static SessionFactory sessionFactory(DataSource dataSource) {
        var config = new Configuration();
        config.setProperty("hibernate.connection.url", DATABASE_URL);
        config.setProperty("hibernate.archive.autodetection", "class");
        //config.setProperty("hibernate.format_sql", "true");
        config.setProperty("hibernate.show_sql", "true");
        config.setProperty("hibernate.hbm2ddl.auto", "create-drop");
        config.setProperty("hibernate.generate_statistics", "true");
        return config.buildSessionFactory();
    }

    protected <R> R transactional(Transactional<R> action) {
        var session = sessionFactory.openSession();
        var tx = session.beginTransaction();
        var result = action.doInTransaction(session);
        tx.commit();
        session.close();
        return result;
    }

    @FunctionalInterface
    protected static interface Transactional<R> {
        R doInTransaction(Session session);
    }
}