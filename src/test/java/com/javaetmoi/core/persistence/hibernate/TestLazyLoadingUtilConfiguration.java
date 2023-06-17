package com.javaetmoi.core.persistence.hibernate;

import javax.sql.DataSource;

import org.h2.jdbcx.JdbcDataSource;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class TestLazyLoadingUtilConfiguration {
    public static final String DATABASE_URL = "jdbc:h2:~/hibernate-hydrate";

    public static DataSource dataSource() {
        var dataSource = new JdbcDataSource();
        dataSource.setURL(DATABASE_URL);
        return dataSource;
    }

    public static DBUnitLoader dbUnitLoader(DataSource dataSource) {
        return new DBUnitLoader(dataSource);
    }

    public static SessionFactory sessionFactory(DataSource dataSource) {
        var config = new Configuration();
        //config.setProperty("hibernate.format_sql", "true");
        config.setProperty("hibernate.show_sql", "true");
        config.setProperty("hibernate.hbm2ddl.auto", "create-drop");
        config.setProperty("hibernate.generate_statistics", "true");
        config.configure();
        return config.buildSessionFactory();
    }

    public static <R> R transactional(SessionFactory sessionFactory, Transactional<R> action) {
        var session = sessionFactory.openSession();
        var tx = session.beginTransaction();
        var result = action.doInTransaction(session);
        tx.commit();
        session.close();
        return result;
    }

    @FunctionalInterface
    public static interface Transactional<R> {
        R doInTransaction(Session session);
    }
}