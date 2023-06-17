package com.javaetmoi.core.persistence.hibernate;

import com.javaetmoi.core.persistence.hibernate.domain.Customer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Test reference back to parent does not cause infinite recursion
 * <p>
 * Unit test for the https://github.com/arey/hibernate-hydrate/issues/10 fix
 */
class TestIssue10 extends AbstractTest {
    @Test
    void oneToOneBidirectionalRelationship() {
        var customer = transactional(session ->
                LazyLoadingUtil.deepHydrate(sessionFactory.getCurrentSession(),
                        session.get(Customer.class, 1)));
        assertEquals(Integer.valueOf(1), customer.getId());
        assertNotNull(customer.getPassport());
        assertEquals(Integer.valueOf(1), customer.getPassport().getId());
        assertEquals(Integer.valueOf(1),customer.getPassport().getOwner().getId());
    }
}
