/**
 * Copyright 2012 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.javaetmoi.core.persistence.hibernate;

import com.javaetmoi.core.persistence.hibernate.domain.Parent;
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
 * Unit test for the https://github.com/arey/hibernate-hydrate/issues/2 fix
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration("TestLazyLoadingUtil-context.xml")
class TestIssue2 {

    @Autowired
    private SessionFactory      sessionFactory;

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
    void nestedListUsingMappedSuperclass() {

        Parent dbParent = transactionTemplate.execute(status -> {
            Parent parent = sessionFactory.getCurrentSession().get(Parent.class, 1L);
            return LazyLoadingUtil.deepHydrate(sessionFactory.getCurrentSession(), parent);
        });
        assertEquals(Long.valueOf(1), dbParent.getId());
        assertEquals("Parent 1", dbParent.getName());
        assertEquals(2, dbParent.getChildren().size());
        assertNotNull("Child 10", dbParent.getChildren().get(0).getName());
        assertNotNull("Parent 1", dbParent.getChildren().get(0).getParent().getName());
    }

}
