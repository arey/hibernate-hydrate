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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import com.javaetmoi.core.persistence.hibernate.domain.Foo;

/**
 * Unit test for the https://github.com/arey/hibernate-hydrate/issues/1 fix
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("TestLazyLoadingUtil-context.xml")
public class TestIssue1Fix {

    @Autowired
    private HibernateTemplate   hibernateTemplate;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private DBUnitLoader        dbUnitLoader;

    /**
     * Populate entities graph and embbeded database
     */
    @Before
    @Transactional
    public void setUp() {
        dbUnitLoader.loadDatabase(getClass());
    }

    @Test
    public void nestedListInEmbeddable() {

        Foo dbFoo = transactionTemplate.execute(new TransactionCallback<Foo>() {

            public Foo doInTransaction(TransactionStatus status) {
                Foo foo = hibernateTemplate.get(Foo.class, 1);
                LazyLoadingUtil.deepHydrate(
                        hibernateTemplate.getSessionFactory().getCurrentSession(), foo);
                return foo;
            }
        });
        assertNotNull(dbFoo.getBar());
        assertNotNull(dbFoo.getBar().getBizs());
        assertEquals("Fix the LazyInitializationException", 2, dbFoo.getBar().getBizs().size());
        assertNotNull(dbFoo.getBar().getBizs().get(0));
    }

}
