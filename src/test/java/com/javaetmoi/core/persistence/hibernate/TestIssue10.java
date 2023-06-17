package com.javaetmoi.core.persistence.hibernate;

import com.javaetmoi.core.persistence.hibernate.domain.Customer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Unit test for issue 10: Test reference back to parent does not cause infinite recursion
 *
 * @see <a href="https://github.com/arey/hibernate-hydrate/issues/10">Issue 10</a>
 */
class TestIssue10 extends AbstractTest {
    @Test
    void oneToOneBidirectionalRelationship() {
        var dbCustomer = findDeepHydratedEntity(Customer.class, 1);

        assertEquals(1, dbCustomer.getId());
        assertNotNull(dbCustomer.getPassport());
        assertEquals(1, dbCustomer.getPassport().getId());
        assertEquals(1, dbCustomer.getPassport().getOwner().getId());
    }
}
