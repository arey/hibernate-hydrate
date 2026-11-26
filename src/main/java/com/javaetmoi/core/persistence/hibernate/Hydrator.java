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
     * Supports attributes that are associations to entities, collections or embedded
     * and lazy basic attributes ({@code @Basic(fetch = LAZY)}).
     * Attributes inside of embedded attributes and embedded identifiers are addressed by a path,
     * e.g. {@code "address.country"} or {@code "id.owner"}.
     * Attributes inside of collections of {@link jakarta.persistence.Embeddable}s are not supported.
     *
     * @param entityClass
     *            Entity class.
     * @param attribute
     *            Attribute name or path to an attribute inside of an embedded attribute.
     * @throws IllegalArgumentException
     *            if the entity class is not a JPA entity, the attribute does not exist
     *            or the attribute is a basic value that is not lazy, which is never hydrated.
     * @return new instance with the exclusion.
     */
    public Hydrator withExclude(Class<?> entityClass, String attribute);

    /**
     * Populate a lazy-initialized object graph.
     * <p>
     * This method deeply navigates into a graph of entities in order to resolve uninitialized Hibernate proxies.<br>
     * The goal is to avoid any {@link LazyInitializationException} once entities are detached.<br>
     * Attention: This method has to be called from an open persistent context / Hibernate session.
     * </p>
     *
     * @param entities
     *            A {@link Collection} of attached Hibernate entities to load.
     *            Elements that are no entities are ignored.
     *            If it is {@code null}, {@code null} is returned.
     * @return the {@link Collection} of Hibernate entities fully loaded.
     *         Similar to the entities input parameter.
     *         Useful when calling this method in a return statement.
     */
    public <C extends Collection<E>, E> C deepHydrateCollection(C entities);

    /**
     * Populate a lazy-initialized object graph.
     * <p>
     * This method deeply navigates into a graph of entities in order to resolve uninitialized Hibernate proxies.<br>
     * The goal is to avoid any {@link LazyInitializationException} once entities are detached.<br>
     * Attention: This method has to be called from an open persistent context / Hibernate session.
     * </p>
     *
     * @param entity
     *            An attached Hibernate entity to load.
     *            If it is no entity or {@code null}, it is returned unchanged.
     * @return the Hibernate entity fully loaded.
     *         Similar to the entity input parameter.
     *         Useful when calling this method in a return statement.
     */
    public <E> E deepHydrate(E entity);
}
