package com.javaetmoi.core.persistence.hibernate;

import com.javaetmoi.core.persistence.hibernate.domain.Customer;
import com.javaetmoi.core.persistence.hibernate.domain.Foo;
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
        var dbCustomer = getDeepHydratedEntity(Customer.class, 1);

        assertEquals(1, dbCustomer.getId());
        assertNotNull(dbCustomer.getPassport());
        assertEquals(1, dbCustomer.getPassport().getId());
        assertEquals(1, dbCustomer.getPassport().getOwner().getId());
    }
}
