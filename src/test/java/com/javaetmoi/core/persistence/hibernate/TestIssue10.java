package com.javaetmoi.core.persistence.hibernate;

import javax.sql.DataSource;

import com.javaetmoi.core.persistence.hibernate.domain.Customer;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.javaetmoi.core.persistence.hibernate.TestLazyLoadingUtilConfiguration.dataSource;
import static com.javaetmoi.core.persistence.hibernate.TestLazyLoadingUtilConfiguration.dbUnitLoader;
import static com.javaetmoi.core.persistence.hibernate.TestLazyLoadingUtilConfiguration.sessionFactory;
import static com.javaetmoi.core.persistence.hibernate.TestLazyLoadingUtilConfiguration.transactional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Test reference back to parent does not cause infinite recursion
 * <p>
 * Unit test for the https://github.com/arey/hibernate-hydrate/issues/10 fix
 */
class TestIssue10 {

    private final DataSource dataSource = dataSource();
    private final DBUnitLoader dbUnitLoader = dbUnitLoader(dataSource);
    private final SessionFactory sessionFactory = sessionFactory(dataSource);

    /**
     * Populate entities graph and embedded database
     */
    @BeforeEach
    void setUp() {
        dbUnitLoader.loadDatabase(getClass());
    }

    @Test
    void oneToOneBidirectionalRelationship() {
        var dbContainer = transactional(sessionFactory, session -> {
            var customer = session.get(Customer.class, 1);
            return LazyLoadingUtil.deepHydrate(sessionFactory.getCurrentSession(), customer);
        });
        assertEquals(Integer.valueOf(1), dbContainer.getId());
        assertNotNull(dbContainer.getPassport());
        assertEquals(Integer.valueOf(1), dbContainer.getPassport().getId());
        assertEquals(Integer.valueOf(1),dbContainer.getPassport().getOwner().getId());
    }
}
