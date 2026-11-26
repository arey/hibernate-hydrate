package com.javaetmoi.core.persistence.hibernate;

import com.javaetmoi.core.persistence.hibernate.deepInheritance.Animal;
import com.javaetmoi.core.persistence.hibernate.deepInheritance.AnimalHolder;
import com.javaetmoi.core.persistence.hibernate.deepInheritance.Dog;
import com.javaetmoi.core.persistence.hibernate.deepInheritance.Mammal;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the hydration of entities of an inheritance hierarchy with three levels.
 */
class TestDeepInheritance extends AbstractTest {
    /**
     * The attributes of all levels are hydrated,
     * even if the association is declared with the root type of the hierarchy.
     */
    @Test
    void allLevels() {
        var dbHolder = findDeepHydratedEntity(AnimalHolder.class, 1);

        // At this step, transaction and session are closed.
        var dbDog = (Dog) Hibernate.unproxy(dbHolder.getAnimal());
        assertThat(dbDog.getAnimalNames()).containsExactly("animal");
        assertThat(dbDog.getMammalNames()).containsExactly("mammal");
        assertThat(dbDog.getDogNames()).containsExactly("dog");
    }

    /**
     * Excludes declared at a superclass apply to instances of subclasses.
     */
    @Test
    void excludeOfSuperclass() {
        var dbHolder = doInJPA(entityManager ->
                hydrator.withExclude(Animal.class, "animalNames")
                        .withExclude(Mammal.class, "mammalNames")
                        .deepHydrate(entityManager.find(AnimalHolder.class, 1)));

        // At this step, transaction and session are closed.
        var dbDog = (Dog) Hibernate.unproxy(dbHolder.getAnimal());
        assertThat(Hibernate.isInitialized(dbDog.getAnimalNames())).isFalse();
        assertThat(Hibernate.isInitialized(dbDog.getMammalNames())).isFalse();
        assertThat(dbDog.getDogNames()).containsExactly("dog");
    }

    /**
     * Excludes of inherited attributes declared at a subclass apply to the attributes of the superclass.
     */
    @Test
    void excludeOfInheritedAttribute() {
        var dbHolder = doInJPA(entityManager ->
                hydrator.withExclude(Dog.class, "animalNames")
                        .deepHydrate(entityManager.find(AnimalHolder.class, 1)));

        // At this step, transaction and session are closed.
        var dbDog = (Dog) Hibernate.unproxy(dbHolder.getAnimal());
        assertThat(Hibernate.isInitialized(dbDog.getAnimalNames())).isFalse();
        assertThat(dbDog.getMammalNames()).containsExactly("mammal");
        assertThat(dbDog.getDogNames()).containsExactly("dog");
    }
}
