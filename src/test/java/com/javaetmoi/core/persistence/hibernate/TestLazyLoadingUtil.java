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
import static org.junit.Assert.assertTrue;

import javax.persistence.ManyToOne;

import org.hibernate.LazyInitializationException;
import org.hibernate.classic.Session;
import org.hibernate.collection.PersistentCollection;
import org.hibernate.collection.PersistentMap;
import org.hibernate.stat.Statistics;
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

import com.javaetmoi.core.persistence.hibernate.domain.Address;
import com.javaetmoi.core.persistence.hibernate.domain.Country;
import com.javaetmoi.core.persistence.hibernate.domain.Employee;
import com.javaetmoi.core.persistence.hibernate.domain.Project;

/**
 * Unit test of the {@link LazyLoadingUtil} class.
 * 
 * @author arey
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class TestLazyLoadingUtil {

    @Autowired
    HibernateTemplate           hibernateTemplate;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private DBUnitLoader        dbUnitLoader;

    private Employee            james, tom;

    private Project             android, iphone;

    private Address             paris, lyon, ladefense;

    private Country             france;

    /**
     * Populate entities graph and embbeded database
     */
    @Before
    @Transactional
    public void setUp() {
        dbUnitLoader.loadDatabase(getClass());

        // Reset Hibernate Statistics
        hibernateTemplate.getSessionFactory().getStatistics().clear();

        france = new Country(1000, "France");
        james = new Employee(1, "James", "Developer");
        tom = new Employee(2, "Tom", "Project Manager");
        android = new Project(10, "Android Project");
        iphone = new Project(20, "iPhone Project");
        paris = new Address(100, "home", "Paris", james, france);
        lyon = new Address(200, "work", "Lyon", tom, france);
        ladefense = new Address(300, "work", "La Défense", james, france);

        james.getProjects().add(android);
        james.getProjects().add(iphone);
        james.getAddresses().put(paris.getType(), paris);
        james.getAddresses().put(ladefense.getType(), ladefense);
        android.getMembers().add(james);
        android.getMembers().add(tom);
        tom.getProjects().add(android);
        tom.getAddresses().put(lyon.getType(), lyon);
        iphone.getMembers().add(james);
    }

    /**
     * Verify the {@link LazyInitializationException} is thrown when accessing entity relations
     * outside a transaction, an Hibernate {@link PersistentMap} in this test.
     */
    @Test(expected = LazyInitializationException.class)
    public void lazyInitializationExceptionOnPersistentMap() {
        // Load each entity in a transaction
        Employee dbJames = hibernateTemplate.get(Employee.class, 1);
        // At this step, transaction and session are closed
        dbJames.getAddresses().get(0);
    }

    /**
     * Verify the {@link LazyInitializationException} is thrown when accessing entity relations
     * outside a transaction, an Hibernate {@link PersistentCollection} in this test.
     */
    @Test(expected = LazyInitializationException.class)
    public void lazyInitializationExceptionOnPersistentCollection() {
        // Load each entity in a transaction
        Employee dbJames = hibernateTemplate.get(Employee.class, 1);
        // At this step, transaction and session are closed
        dbJames.getProjects().contains(android);
    }

    /**
     * Verify the {@link LazyInitializationException} is thrown when accessing entity relations
     * outside a transaction, a {@link ManyToOne} relationship in this test.
     */
    @Test(expected = LazyInitializationException.class)
    public void lazyInitializationExceptionWithManyToOne() {
        // Load each entity in a transaction
        Address dbLyon = hibernateTemplate.get(Address.class, 200);
        // At this step, transaction and session are closed
        dbLyon.getEmployee().getName();
    }

    /**
     * Tests the method {@link LazyLoadingUtil#deepHydrate(org.hibernate.Session, Object)
     **/
    @Test
    public void deepResolveEmployee() {
        // Loading an entity and hydrating its graph is done in a single transaction
        Employee dbJames = transactionTemplate.execute(new TransactionCallback<Employee>() {

            @Override
            public Employee doInTransaction(TransactionStatus status) {
                Employee employee = hibernateTemplate.get(Employee.class, 1);
                return LazyLoadingUtil.deepHydrate(
                        hibernateTemplate.getSessionFactory().getCurrentSession(), employee);
            }
        });

        assertNotNull("No LazyInitializationException should be thrown",
                dbJames.getAddresses().get("home"));
        assertTrue(dbJames.getAddresses().equals(james.getAddresses()));
        assertTrue(dbJames.getProjects().contains(android));
        assertEquals("Compare in-memory and database loaded employees", james, dbJames);

        Statistics statistics = hibernateTemplate.getSessionFactory().getStatistics();
        assertEquals(
                "All 8 entities are loaded: france, james, tom, android, iphone, paris, la défense and lyon",
                8, statistics.getEntityLoadCount());
        assertEquals(
                "6 collections should be fetched: james' adresses, james' projects, iPhone members, tom's adresses, tom's projects, android members",
                6, statistics.getCollectionFetchCount());
    }

    /**
     * Tests the method {@link LazyLoadingUtil#deepHydrate(org.hibernate.Session, Object)
     **/
    @Test
    public void deepResolveAddress() {
        // Loading an entity and hydrating its graph is done in a single transaction
        Address dbLyon = transactionTemplate.execute(new TransactionCallback<Address>() {

            @Override
            public Address doInTransaction(TransactionStatus status) {
                Address address = hibernateTemplate.get(Address.class, 200);
                LazyLoadingUtil.deepHydrate(
                        hibernateTemplate.getSessionFactory().getCurrentSession(), address);
                return address;
            }
        });

        assertEquals("No LazyInitializationException should be thrown",
                dbLyon.getEmployee().getName(), tom.getName());
        assertEquals("Compare in-memory and database loaded addresses", lyon, dbLyon);
        assertEquals("Compare projetcs size", lyon.getEmployee().getProjects().size(),
                dbLyon.getEmployee().getProjects().size());
    }
}
