package com.javaetmoi.core.persistence.hibernate;

import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import com.javaetmoi.core.persistence.hibernate.joinInheritance.SubClass;
import com.javaetmoi.core.persistence.hibernate.joinInheritance.ParentReference;


import static org.junit.jupiter.api.Assertions.*;

/**
 * Test the lazy initialization of a collection of subclasses with a join inheritance strategy.
 * <p>
 * Unit test for the <a href="https://github.com/arey/hibernate-hydrate/issues/50">Lazyloading of a subclass with a join inheritance strategy</a> fix
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration("TestLazyLoadingUtil-context.xml")
class TestIssue50 {

    @Autowired
    private SessionFactory sessionFactory;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private DBUnitLoader dbUnitLoader;

    /**
     * Populate entities graph and embedded database
     */
    @BeforeEach
    @Transactional
    void setUp() {
        dbUnitLoader.loadDatabase(getClass());
    }

    @Test
    void should_lazyload_subclass_with_join_inheritance() {

        ParentReference dbContainer = transactionTemplate.execute(status -> {
            ParentReference parentReference = sessionFactory.getCurrentSession().get(ParentReference.class, 1);
            LazyLoadingUtil.deepHydrate(sessionFactory.getCurrentSession(), parentReference);
            return parentReference;
        });
        assertNotNull(dbContainer);
        assertEquals(Integer.valueOf(1), dbContainer.getId());
        assertInstanceOf(SubClass.class, dbContainer.getParent());
        assertEquals(1, ((SubClass) dbContainer.getParent()).getDatas().size());
        assertEquals("data", ((SubClass) dbContainer.getParent()).getDatas().get(0).getName());
    }
}