package com.javaetmoi.core.persistence.hibernate;

import com.javaetmoi.core.persistence.hibernate.domain.Customer;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Test reference back to parent does not causes infinite recursion
 * <p>
 * Unit test for the https://github.com/arey/hibernate-hydrate/issues/10 fix
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration("TestLazyLoadingUtil-context.xml")
class TestIssue10 {

    @Autowired
    private SessionFactory      sessionFactory;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private DBUnitLoader        dbUnitLoader;

    /**
     * Populate entities graph and embedded database
     */
    @BeforeEach
    @Transactional
    void setUp() {
        dbUnitLoader.loadDatabase(getClass());
    }

    @Test
    void oneToOneBidirectionnalRelationship() {

        Customer dbContainer = transactionTemplate.execute(status -> {
            Customer customer = sessionFactory.getCurrentSession().get(Customer.class, 1);
            LazyLoadingUtil.deepHydrate(sessionFactory.getCurrentSession(), customer);
            return customer;
        });
        assertEquals(Integer.valueOf(1), dbContainer.getId());
        assertNotNull(dbContainer.getPassport());
        assertEquals(Integer.valueOf(1), dbContainer.getPassport().getId());
        assertEquals(Integer.valueOf(1),dbContainer.getPassport().getOwner().getId());
    }
}
