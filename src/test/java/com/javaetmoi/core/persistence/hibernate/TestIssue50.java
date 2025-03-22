package com.javaetmoi.core.persistence.hibernate;

import org.junit.jupiter.api.Test;

import com.javaetmoi.core.persistence.hibernate.joinInheritance.ParentReference;
import com.javaetmoi.core.persistence.hibernate.joinInheritance.SubClass;


import static org.junit.jupiter.api.Assertions.*;


class TestIssue50 extends AbstractTest {

    @Test
    void should_lazyload_subclass_with_join_inheritance() {
        var dbContainer = findDeepHydratedEntity(ParentReference.class, 1);

        assertNotNull(dbContainer);
        assertEquals(Integer.valueOf(1), dbContainer.getId());
        assertInstanceOf(SubClass.class, dbContainer.getParent());
        assertEquals(1, ((SubClass) dbContainer.getParent()).getDatas().size());
        assertEquals("data", ((SubClass) dbContainer.getParent()).getDatas().get(0).getName());
    }
}