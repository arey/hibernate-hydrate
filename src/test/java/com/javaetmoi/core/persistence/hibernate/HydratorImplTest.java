/**
 * Copyright 2012-2026 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.javaetmoi.core.persistence.hibernate;

import com.javaetmoi.core.persistence.hibernate.domain.Address;
import com.javaetmoi.core.persistence.hibernate.domain.Country;
import com.javaetmoi.core.persistence.hibernate.domain.Employee;
import com.javaetmoi.core.persistence.hibernate.domain.Foo;
import com.javaetmoi.core.persistence.hibernate.domain.Project;
import org.hibernate.Hibernate;
import org.hibernate.LazyInitializationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import static com.javaetmoi.core.persistence.hibernate.Hydrator.hydrator;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.unitils.reflectionassert.ReflectionAssert.assertReflectionEquals;
import static org.unitils.reflectionassert.ReflectionComparatorMode.LENIENT_ORDER;

/**
 * Unit test of the {@link HydratorImpl} class.
 * <p>
 * Tests its interface with a graph of entities.
 * The single kinds of mappings are tested by the other tests.
 * </p>
 *
 * @author Antoine Rey
 */
class HydratorImplTest extends AbstractTest {
    private Employee james, tom;

    private Project android, iphone;

    private Address lyon;

    /**
     * Persist the graph of entities and create an equal graph of plain objects as expectation.
     */
    @BeforeEach
    void setUpEntities() {
        persist(createGraph());
        createGraph();
    }

    /**
     * Create the graph of entities.
     *
     * @return the entities to persist. They cascade to the other ones.
     */
    private Object[] createGraph() {
        var france = new Country(1000, "France");
        james = new Employee(1, "James", "Developer");
        tom = new Employee(2, "Tom", "Project Manager");
        android = new Project(10, "Android Project");
        iphone = new Project(20, "iPhone Project");
        var paris = new Address(100, "home", "Paris", james, france);
        lyon = new Address(200, "work", "Lyon", tom, france);
        var ladefense = new Address(300, "work", "La Defense", james, france);

        james.getProjects().add(android);
        james.getProjects().add(iphone);
        james.getAddresses().put(paris.getType(), paris);
        james.getAddresses().put(ladefense.getType(), ladefense);
        android.getMembers().add(james);
        android.getMembers().add(tom);
        tom.getProjects().add(android);
        tom.getAddresses().put(lyon.getType(), lyon);
        iphone.getMembers().add(james);

        return new Object[] {france, james, tom};
    }

    /**
     * Without hydration the associations are not initialized.
     */
    @Test
    void notHydrated() {
        var dbJames = findEntity(Employee.class, 1);
        var dbLyon = findEntity(Address.class, 200);

        assertThat(Hibernate.isInitialized(dbJames.getProjects())).isFalse();
        assertThat(Hibernate.isInitialized(dbJames.getAddresses())).isFalse();
        assertThat(Hibernate.isInitialized(dbLyon.getEmployee())).isFalse();
    }

    //
    // deepHydrate.
    //

    @Test
    void deepHydrate_nullEntity() {
        assertThat(hydrator.deepHydrate((Object) null)).isNull();
    }

    @Test
    void deepHydrate_nonEntity() {
        assertThat(hydrator.deepHydrate("no entity")).isEqualTo("no entity");
    }

    @Test
    void deepHydrate_newEntity() {
        assertThat(hydrator.deepHydrate(james)).isSameAs(james);
    }

    /**
     * The hydration needs an open session: A detached entity cannot be hydrated.
     */
    @Test
    void deepHydrate_detachedEntity() {
        var dbJames = findEntity(Employee.class, 1);

        assertThatThrownBy(() -> hydrator.deepHydrate(dbJames))
                .isInstanceOf(LazyInitializationException.class);
    }

    @Test
    void deepHydrate_attachedEntityProxy() {
        var dbJames = findDeepHydratedEntityReference(Employee.class, 1);

        assertThat(dbJames).isEqualTo(james);
    }

    /**
     * The entire graph is hydrated: All entities that are reachable from the employee.
     */
    @Test
    void deepHydrate_entireGraph() {
        var dbJames = findDeepHydratedEntity(Employee.class, 1);

        assertReflectionEquals(james, dbJames, LENIENT_ORDER);
    }

    /**
     * The entire graph is hydrated, starting at another entity of it.
     */
    @Test
    void deepHydrate_entireGraphFromAddress() {
        var dbLyon = findDeepHydratedEntity(Address.class, 200);

        assertThat(dbLyon).isEqualTo(lyon);
        assertThat(dbLyon.getEmployee().getName()).isEqualTo("Tom");
        assertThat(dbLyon.getEmployee().getProjects()).containsExactly(android);
    }

    /**
     * Each entity and each collection is loaded just once.
     */
    @Test
    void deepHydrate_loadsOnce() {
        findDeepHydratedEntity(Employee.class, 1);

        assertThat(statistics().getEntityLoadCount())
                .as("france, james, tom, android, iphone, paris, la defense and lyon")
                .isEqualTo(8);
        assertThat(statistics().getCollectionFetchCount())
                .as("addresses and projects of james and tom, members of android and iphone")
                .isEqualTo(6);
    }

    /**
     * Tests the factory {@link Hydrator#hydrator(jakarta.persistence.EntityManager)}.
     */
    @Test
    void deepHydrate_hydratorOfEntityManager() {
        var dbJames = doInJPA(entityManager ->
                hydrator(entityManager).deepHydrate(entityManager.find(Employee.class, 1)));

        assertThat(dbJames).isEqualTo(james);
    }

    //
    // deepHydrateCollection.
    //

    @Test
    void deepHydrateCollection_nullCollection() {
        assertThat(hydrator.deepHydrateCollection((Collection<Object>) null)).isNull();
    }

    @Test
    void deepHydrateCollection_emptyCollection() {
        assertThat(hydrator.deepHydrateCollection(List.of())).isEmpty();
    }

    /**
     * Elements that are {@code null} are ignored.
     */
    @Test
    void deepHydrateCollection_nullElement() {
        var dbEmployees = doInJPA(entityManager ->
                hydrator.deepHydrateCollection(asList(entityManager.find(Employee.class, 1), null)));

        assertThat(dbEmployees).containsExactly(james, null);
    }

    @Test
    void deepHydrateCollection_nonEntity() {
        assertThat(hydrator.deepHydrateCollection(Set.of("no entity"))).containsOnly("no entity");
    }

    @Test
    void deepHydrateCollection_attachedEntity() {
        var dbEmployees = findDeepHydratedEntities(Employee.class, 1, 2);

        assertThat(dbEmployees).containsExactly(james, tom);
    }

    @Test
    void deepHydrateCollection_attachedEntityProxy() {
        var dbEmployees = findDeepHydratedEntityReferences(Employee.class, 1, 2);

        assertThat(dbEmployees).containsExactly(james, tom);
    }

    //
    // withExclude.
    //

    /**
     * Exclude of an attribute of the hydrated entity.
     */
    @Test
    void withExclude() {
        var dbJames = findDeepHydratedEntity(
                hydrator.withExclude(Employee.class, "addresses"), Employee.class, 1);

        assertThat(dbJames.getProjects()).containsExactly(android, iphone);
        assertThat(Hibernate.isInitialized(dbJames.getAddresses())).isFalse();
    }

    /**
     * Exclude of an attribute of an entity that is reached during hydration.
     */
    @Test
    void withExclude_ofReachedEntity() {
        var dbJames = findDeepHydratedEntity(
                hydrator.withExclude(Project.class, "members"), Employee.class, 1);

        assertThat(dbJames.getAddresses()).containsOnlyKeys("home", "work");
        assertThat(Hibernate.isInitialized(dbJames.getProjects().get(0).getMembers())).isFalse();
    }

    @Test
    void withExclude_severalExcludes() {
        var dbJames = findDeepHydratedEntity(
                hydrator.withExclude(Project.class, "members").withExclude(Employee.class, "addresses"),
                Employee.class, 1);

        assertThat(Hibernate.isInitialized(dbJames.getProjects().get(0).getMembers())).isFalse();
        assertThat(Hibernate.isInitialized(dbJames.getAddresses())).isFalse();
    }

    /**
     * A hydrator is immutable: An exclude results in a new hydrator.
     */
    @Test
    void withExclude_keepsHydrator() {
        assertThat(hydrator.withExclude(Employee.class, "addresses")).isNotSameAs(hydrator);

        var dbJames = findDeepHydratedEntity(Employee.class, 1);

        assertThat(dbJames).isEqualTo(james);
    }

    /**
     * An entity that is reached via an excluded attribute first has to be hydrated nevertheless,
     * if it is reachable via another path that is not excluded.
     */
    @Test
    void withExclude_entityReachableViaOtherPath() {
        var dbTom = doInJPA(entityManager -> {
            var lyon = entityManager.find(Address.class, 200);
            var tom = entityManager.find(Employee.class, 2);
            // Lyon first, so Tom is reached via the excluded attribute before he is reached directly.
            hydrator.withExclude(Address.class, "employee")
                    .deepHydrateCollection(List.of(lyon, tom));
            return tom;
        });

        assertThat(dbTom.getProjects()).containsExactly(android);
        assertThat(dbTom.getAddresses()).containsOnlyKeys("work");
    }

    @Test
    void withExclude_nullEntityClass() {
        assertInvalidExclude(null, "name");
    }

    @Test
    void withExclude_nullAttribute() {
        assertInvalidExclude(Employee.class, null);
    }

    @Test
    void withExclude_nonEntity() {
        assertInvalidExclude(String.class, "value")
                .hasMessageContaining(String.class.getName());
    }

    @Test
    void withExclude_unknownAttribute() {
        assertInvalidExclude(Employee.class, "unknown")
                .hasMessageContaining("unknown");
    }

    @Test
    void withExclude_emptyAttribute() {
        assertInvalidExclude(Employee.class, "");
    }

    /**
     * Basic attributes are never hydrated.
     */
    @Test
    void withExclude_basicAttribute() {
        assertInvalidExclude(Employee.class, "name")
                .hasMessageContaining("name");
    }

    @Test
    void withExclude_unknownAttributeOfEmbeddable() {
        assertInvalidExclude(Foo.class, "bar.unknown")
                .hasMessageContaining("bar.unknown");
    }

    @Test
    void withExclude_incompletePath() {
        assertInvalidExclude(Foo.class, "bar.");
    }

    /**
     * Paths are just supported for embeddables.
     */
    @Test
    void withExclude_pathIntoCollection() {
        assertInvalidExclude(Employee.class, "projects.members")
                .hasMessageContaining("projects.members");
    }
}
