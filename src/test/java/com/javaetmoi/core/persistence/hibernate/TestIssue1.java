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

import javax.sql.DataSource;

import com.javaetmoi.core.persistence.hibernate.domain.Foo;
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
 * Unit test for the https://github.com/arey/hibernate-hydrate/issues/1 fix
 */
class TestIssue1 {

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
    void nestedListInEmbeddable() {
        var dbFoo = transactional(sessionFactory, session -> {
            var foo = session.get(Foo.class, 1);
            return LazyLoadingUtil.deepHydrate(sessionFactory.getCurrentSession(), foo);
        });
        assertNotNull(dbFoo.getBar());
        assertNotNull(dbFoo.getBar().getBizs());
        assertEquals(2, dbFoo.getBar().getBizs().size(), "Fix the LazyInitializationException");
        assertNotNull(dbFoo.getBar().getBizs().get(0));
    }

}
