package com.javaetmoi.core.persistence.hibernate;

import com.javaetmoi.core.persistence.hibernate.domain.Customer;
import com.javaetmoi.core.persistence.hibernate.listWithEmbeddable.Plan;
import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Test reference back to parent does not causes infinite recursion
 * <p>
 * Unit test for the https://github.com/arey/hibernate-hydrate/issues/10 fix
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("TestLazyLoadingUtil-context.xml")
public class TestIssue10 {

    @Autowired
    private SessionFactory sessionFactory;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private DBUnitLoader dbUnitLoader;

    /**
     * Populate entities graph and embbeded database
     */
    @Before
    @Transactional
    public void setUp() {
        dbUnitLoader.loadDatabase(getClass());
    }

    @Test
    public void oneToOneBidirectionnalRelationship() {

        Customer dbContainer = transactionTemplate
                .execute(new TransactionCallback<Customer>() {

                    public Customer doInTransaction(TransactionStatus status) {
                        Customer customer = (Customer) sessionFactory.getCurrentSession().get(Customer.class, 1);
                        LazyLoadingUtil.deepHydrate(sessionFactory.getCurrentSession(), customer);
                        return customer;
                    }
                });
        assertEquals(Integer.valueOf(1), dbContainer.getId());
        assertNotNull(dbContainer.getPassport());
        assertEquals(Integer.valueOf(1), dbContainer.getPassport().getId());
        assertEquals(Integer.valueOf(1),dbContainer.getPassport().getOwner().getId());
    }
}
