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
     * Factory for {@link EntityManager} or {@link Session}.
     *
     * @param entityManager
     *            {@link EntityManager} of an open {@link EntityManagerFactory}.
     * @return new instance.
     */
    public static Hydrator hydrator(EntityManager entityManager) {
        return hydrator(entityManager.getEntityManagerFactory());
    }

    /**
     * Factory for {@link EntityManagerFactory} or {@link SessionFactory}.
     *
     * @param entityManagerFactory
     *            Open {@link EntityManagerFactory}.
     * @return new instance.
     */
    public static Hydrator hydrator(EntityManagerFactory entityManagerFactory) {
        return new HydratorImpl(entityManagerFactory);
    }

    /**
     * Exclude the attribute of the entity from hydration.
     * Just supports attributes that are JPA entities or collections.
     * {@link jakarta.persistence.Embeddable}s are not supported.
     *
     * @param entityClass
     *            Entity class.
     * @param attribute
     *            Attribute name.
     * @throws IllegalArgumentException if then entity class is not a JPA entity or the attribute does not exist.
     * @return new instance with the exclusion.
     */
    public Hydrator withExclude(Class<?> entityClass, String attribute);

    /**
     * Populate a lazy-initialized object graph by recursion.
     * <p>
     * This method deeply navigates into a graph of entities in order to resolve uninitialized Hibernate proxies.<br>
     * The goal is to avoid any {@link LazyInitializationException} once entities are detached.<br>
     * Attention: This method has to be called from an open persistent context / Hibernate session.
     * </p>
     *
     * @param entities
     *            A {@link Collection} of attached Hibernate entities to load.
     * @return the {@link Collection} of Hibernate entities fully loaded.
     *         Similar to the entities input parameter.
     *         Useful when calling this method in a return statement.
     */
    public <C extends Collection<E>, E> C deepHydrateCollection(C entities);

    /**
     * Populate a lazy-initialized object graph by recursion.
     * <p>
     * This method deeply navigates into a graph of entities in order to resolve uninitialized Hibernate proxies.<br>
     * The goal is to avoid any {@link LazyInitializationException} once entities are detached.<br>
     * Attention: This method has to be called from an open persistent context / Hibernate session.
     * </p>
     *
     * @param entity
     *            An attached Hibernate entity to load.
     * @return the Hibernate entity fully loaded.
     *         Similar to the entity input parameter.
     *         Useful when calling this method in a return statement.
     */
    public <E> E deepHydrate(E entity);
}
