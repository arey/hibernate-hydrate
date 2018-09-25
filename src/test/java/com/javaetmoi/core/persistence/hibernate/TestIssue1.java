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

import com.javaetmoi.core.persistence.hibernate.domain.Foo;
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
 * Unit test for the https://github.com/arey/hibernate-hydrate/issues/1 fix
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration("TestLazyLoadingUtil-context.xml")
class TestIssue1 {

    @Autowired
    private SessionFactory      sessionFactory;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private TestDBUnitLoader dbUnitLoader;

    /**
     * Populate entities graph and embedded database
     */
    @BeforeEach
    @Transactional
    void setUp() {
        dbUnitLoader.loadDatabase(getClass());
    }

    @Test
    void nestedListInEmbeddable() {

        Foo dbFoo = transactionTemplate.execute(status -> {
            Foo foo = sessionFactory.getCurrentSession().get(Foo.class, 1);
            return LazyLoadingUtil.deepHydrate(sessionFactory.getCurrentSession(), foo);
        });
        assertNotNull(dbFoo.getBar());
        assertNotNull(dbFoo.getBar().getBizs());
        assertEquals(2, dbFoo.getBar().getBizs().size(), "Fix the LazyInitializationException");
        assertNotNull(dbFoo.getBar().getBizs().get(0));
    }

}
