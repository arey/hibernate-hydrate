package com.javaetmoi.core.persistence.hibernate;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.LazyInitializationException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.util.Collection;

/**
 * Hydrate Hibernate/JPA entities.
 */
public interface Hydrator {
    /**
     * Factory for {@link EntityManagerFactory} and {@link SessionFactory}.
     */
    public static Hydrator hydrator(EntityManagerFactory entityManagerFactory) {
        return new HydratorImpl(entityManagerFactory);
    }

    /**
     * Factory for {@link EntityManager} and {@link Session}.
     */
    public static Hydrator hydrator(EntityManager entityManager) {
        return new HydratorImpl(entityManager);
    }

    /**
     * Populate a lazy-initialized object graph by recursion.
     *
     * <p>
     * This method deeply navigates into a graph of entities in order to resolve uninitialized Hibernate proxies.<br>
     * The goal is to avoid any {@link LazyInitializationException} once entities are detached from the Hibernate session.<br>
     * </p>
     *
     * @param entities
     *            A {@link Collection} of attached Hibernate entities to load
     * @return the {@link Collection} of Hibernate entities fully loaded. Similar to the entities
     *         input parameter. Useful when calling this method in a return statement.
     */
    public <C extends Collection<E>, E> C deepHydrateCollection(C entities);

    /**
     * Populate a lazy-initialized object graph by recursion.
     *
     * <p>
     * This method deeply navigates into a graph of entities in order to resolve uninitialized Hibernate proxies.<br>
     * The goal is to avoid any {@link LazyInitializationException} once entities are detached from the Hibernate session.<br>
     * </p>
     *
     * @param entity
     *            A single attached Hibernate entity or a simple java class referencing entities
     * @return the Hibernate entity fully loaded. Similar to the entity input parameter. Useful
     *         when calling this method in a return statement.
     */
    public <E> E deepHydrate(E entity);
}
