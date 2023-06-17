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

import com.javaetmoi.core.persistence.hibernate.domain.Address;
import com.javaetmoi.core.persistence.hibernate.domain.Country;
import com.javaetmoi.core.persistence.hibernate.domain.Employee;
import com.javaetmoi.core.persistence.hibernate.domain.Project;
import org.hibernate.LazyInitializationException;
import org.hibernate.collection.spi.PersistentMap;
import org.hibernate.collection.spi.PersistentCollection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unitils.reflectionassert.ReflectionComparatorMode;

import jakarta.persistence.ManyToOne;

import static org.junit.jupiter.api.Assertions.*;
import static org.unitils.reflectionassert.ReflectionAssert.assertReflectionEquals;

/**
 * Unit test of the {@link LazyLoadingUtil} class.
 * 
 * @author arey
 */
class TestLazyLoadingUtil extends AbstractTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestLazyLoadingUtil.class);

    private Employee james, tom;

    private Project  android, iphone;

    private Address  paris, lyon, ladefense;

    private Country  france;

    /**
     * Populate entities graph and embedded database
     */
    @BeforeEach
    void setUpEntities() {
        france = new Country(1000, "France");
        james = new Employee(1, "James", "Developer");
        tom = new Employee(2, "Tom", "Project Manager");
        android = new Project(10, "Android Project");
        iphone = new Project(20, "iPhone Project");
        paris = new Address(100, "home", "Paris", james, france);
        lyon = new Address(200, "work", "Lyon", tom, france);
        ladefense = new Address(300, "work", "La Defense", james, france);

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
    @Test
    void lazyInitializationExceptionOnPersistentMap() {
        // Load each entity in a transaction
        var dbJames = findEntity(Employee.class, 1);

        // At this step, transaction and session are closed
        assertThrows(LazyInitializationException.class, () -> dbJames.getAddresses().get(0));
    }

    /**
     * Verify the {@link LazyInitializationException} is thrown when accessing entity relations
     * outside a transaction, an Hibernate {@link PersistentCollection} in this test.
     */
    @Test
    void lazyInitializationExceptionOnPersistentCollection() {
        // Load each entity in a transaction
        var dbJames = findEntity(Employee.class, 1);

        // At this step, transaction and session are closed
        assertThrows(LazyInitializationException.class, () -> dbJames.getProjects().contains(android));
    }

    /**
     * Verify the {@link LazyInitializationException} is thrown when accessing entity relations
     * outside a transaction, a {@link ManyToOne} relationship in this test.
     */
    @Test
    void lazyInitializationExceptionWithManyToOne() {
        // Load each entity in a transaction
        var dbLyon = findEntity(Address.class, 200);

        // At this step, transaction and session are closed
        assertThrows(LazyInitializationException.class, () -> dbLyon.getEmployee().getName());
    }

    /**
     * Tests the method {@link LazyLoadingUtil#deepHydrate(org.hibernate.Session, Object)

     **/
    @Test
    void deepResolveEmployee() {
        // Loading an entity and hydrating its graph is done in a single transaction
        var dbJames = findDeepHydratedEntity(Employee.class, 1);

        // Assertions

        // - LazyInitializationException not thrown
        assertNotNull(dbJames.getAddresses().get("home"),
                "No LazyInitializationException should be thrown");

        // - Addresses
        assertEquals(james.getAddresses().size(),
                dbJames.getAddresses().size(),
                "Same addresses size");
        var dbJamesParis = dbJames.getAddresses().get(paris.getType());
        LOGGER.debug("James Paris address toString(): {}", dbJamesParis.toString());
        assertReflectionEquals(
                "Comparing James Paris address with ReflectionAssert", paris, dbJamesParis,
                ReflectionComparatorMode.LENIENT_ORDER);
        assertEquals(paris, dbJamesParis, "Compare James Paris address");
        var dbJamesLaDefense = dbJames.getAddresses().get(ladefense.getType());
        LOGGER.debug("James La Defense address toString(): {}", dbJamesLaDefense.toString());
        assertReflectionEquals(
                "Comparing James La Defense address with ReflectionAssert", ladefense,
                dbJamesLaDefense, ReflectionComparatorMode.LENIENT_ORDER);
        assertEquals(dbJamesLaDefense, ladefense, "Compare James La Defense address");

        // - Projects
        assertTrue(dbJames.getProjects().contains(android));
        assertReflectionEquals(
                "Compare in-memory and database loaded projects with RelectionUtils",
                james.getProjects(), dbJames.getProjects(), ReflectionComparatorMode.LENIENT_ORDER);
        assertReflectionEquals(james.getProjects(), dbJames.getProjects(), ReflectionComparatorMode.LENIENT_ORDER);

        // - Full employee
        LOGGER.debug("James toString(): {}", dbJames.toString());
        assertReflectionEquals(
                "Compare in-memory and database loaded employees with RelectionUtils", dbJames,
                james, ReflectionComparatorMode.LENIENT_ORDER);
        assertReflectionEquals("Compare in-memory and database loaded employees with the equals method",
                james, dbJames, ReflectionComparatorMode.LENIENT_ORDER);

        // - Generated SQL statements number
        assertEquals(8, statistics().getEntityLoadCount(),
                "All 8 entities are loaded: france, james, tom, android, iphone, paris, la d�fense and lyon");
        assertEquals(6, statistics().getCollectionFetchCount(),
                "6 collections should be fetched: james' adresses, james' projects, iPhone members, tom's adresses, tom's projects, android members");
    }

    /**
     * Tests the method {@link LazyLoadingUtil#deepHydrate(org.hibernate.Session, Object)

     **/
    @Test
    void deepResolveAddress() {
        // Loading an entity and hydrating its graph is done in a single transaction
        var dbLyon = findDeepHydratedEntity(Address.class, 200);

        assertEquals(dbLyon.getEmployee().getName(), tom.getName(),
                "No LazyInitializationException should be thrown");
        assertEquals(lyon, dbLyon,
                "Compare in-memory and database loaded addresses");
        assertEquals(
                lyon.getEmployee().getProjects().size(),
                dbLyon.getEmployee().getProjects().size(),
                "Compare projetcs size");
    }
}
