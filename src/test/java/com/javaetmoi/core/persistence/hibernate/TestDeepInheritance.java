package com.javaetmoi.core.persistence.hibernate;

import com.javaetmoi.core.persistence.hibernate.deepInheritance.Animal;
import com.javaetmoi.core.persistence.hibernate.deepInheritance.AnimalHolder;
import com.javaetmoi.core.persistence.hibernate.deepInheritance.Dog;
import com.javaetmoi.core.persistence.hibernate.deepInheritance.Mammal;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the hydration of entities of a single table inheritance hierarchy with three levels.
 * The entity is reached by an association declared with the root type of the hierarchy.
 */
class TestDeepInheritance extends AbstractTest {
    @BeforeEach
    void setUpEntities() {
        var dog = new Dog(1, List.of("animal"), List.of("mammal"), List.of("dog"));
        persist(dog, new AnimalHolder(1, dog));
    }

    /**
     * Without hydration neither the association nor the attributes of any level are initialized.
     */
    @Test
    void notHydrated() {
        var dbHolder = findEntity(AnimalHolder.class, 1);
        var dbDog = findEntity(Dog.class, 1);

        assertThat(Hibernate.isInitialized(dbHolder.getAnimal())).isFalse();
        assertThat(Hibernate.isInitialized(dbDog.getAnimalNames())).isFalse();
        assertThat(Hibernate.isInitialized(dbDog.getMammalNames())).isFalse();
        assertThat(Hibernate.isInitialized(dbDog.getDogNames())).isFalse();
    }

    /**
     * The attributes of all levels are hydrated.
     */
    @Test
    void allLevels() {
        var dbDog = findDeepHydratedDog(hydrator);

        assertThat(dbDog.getAnimalNames()).containsExactly("animal");
        assertThat(dbDog.getMammalNames()).containsExactly("mammal");
        assertThat(dbDog.getDogNames()).containsExactly("dog");
    }

    /**
     * Excludes declared at the root class apply to instances of subclasses.
     */
    @Test
    void withExclude_ofRootClass() {
        var dbDog = findDeepHydratedDog(hydrator.withExclude(Animal.class, "animalNames"));

        assertThat(Hibernate.isInitialized(dbDog.getAnimalNames())).isFalse();
        assertThat(dbDog.getMammalNames()).containsExactly("mammal");
        assertThat(dbDog.getDogNames()).containsExactly("dog");
    }

    /**
     * Excludes declared at a superclass in between apply to instances of subclasses.
     */
    @Test
    void withExclude_ofSuperclass() {
        var dbDog = findDeepHydratedDog(hydrator.withExclude(Mammal.class, "mammalNames"));

        assertThat(dbDog.getAnimalNames()).containsExactly("animal");
        assertThat(Hibernate.isInitialized(dbDog.getMammalNames())).isFalse();
        assertThat(dbDog.getDogNames()).containsExactly("dog");
    }

    /**
     * Excludes of inherited attributes declared at a subclass apply to the attributes of the superclass.
     */
    @Test
    void withExclude_ofInheritedAttribute() {
        var dbDog = findDeepHydratedDog(hydrator.withExclude(Dog.class, "animalNames"));

        assertThat(Hibernate.isInitialized(dbDog.getAnimalNames())).isFalse();
        assertThat(dbDog.getMammalNames()).containsExactly("mammal");
        assertThat(dbDog.getDogNames()).containsExactly("dog");
    }

    /**
     * An instance of a subclass of an entity that is not mapped itself is hydrated like the entity.
     */
    @Test
    void unmappedSubclass() {
        var holder = doInJPA(entityManager -> {
            var unmappedHolder = new AnimalHolder() {};
            unmappedHolder.setAnimal(entityManager.getReference(Animal.class, 1));
            return hydrator.deepHydrate(unmappedHolder);
        });

        var dbDog = (Dog) Hibernate.unproxy(holder.getAnimal());
        assertThat(dbDog.getAnimalNames()).containsExactly("animal");
        assertThat(dbDog.getMammalNames()).containsExactly("mammal");
        assertThat(dbDog.getDogNames()).containsExactly("dog");
    }

    /**
     * An instance of a subclass that is not mapped itself is hydrated like its nearest entity superclass,
     * even if the association is declared with the root type of the hierarchy.
     */
    @Test
    void unmappedSubclass_association() {
        var holder = doInJPA(entityManager -> {
            var unmappedDog = new Dog() {};
            // Lazy collection of a subclass in between.
            unmappedDog.setDogNames(entityManager.find(Dog.class, 1).getDogNames());
            var newHolder = new AnimalHolder();
            newHolder.setAnimal(unmappedDog);
            return hydrator.deepHydrate(newHolder);
        });

        var dog = (Dog) holder.getAnimal();
        assertThat(dog.getDogNames()).containsExactly("dog");
    }

    private Dog findDeepHydratedDog(Hydrator hydrator) {
        var dbHolder = findDeepHydratedEntity(hydrator, AnimalHolder.class, 1);
        return (Dog) Hibernate.unproxy(dbHolder.getAnimal());
    }
}
