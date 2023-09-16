/*
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

import java.util.Collection;

import jakarta.persistence.EntityManager;

import jakarta.persistence.EntityManagerFactory;
import org.hibernate.LazyInitializationException;

import static com.javaetmoi.core.persistence.hibernate.Hydrator.hydrator;

/**
 * Set of helper methods that fetch a complete entity graph.
 * <p>
 * Provides a lazy way to resolve all Hibernate proxies.
 * </p>
 *
 * @author Antoine Rey
 * @deprecated Use {@link Hydrator#hydrator(EntityManagerFactory)} or {@link LazyLoadingUtil} instead.
 */
@Deprecated(forRemoval = true)
public final class JpaLazyLoadingUtil {
    /**
     * No-arg constructor.
     */
    private JpaLazyLoadingUtil() {
        // Private visibility because utility class
    }

    /**
     * Populate a lazy-initialized object graph by recursion.
     * <p>
     * This method deeply navigates into a graph of entities in order to resolve uninitialized Hibernate proxies.<br>
     * The goal is to avoid any {@link LazyInitializationException} once entities are detached.<br>
     * Attention: This method has to be called from an open persistent context / Hibernate session.
     * </p>
     *
     * @param entityManager
     *            Open {@link EntityManager}.
     * @param entities
     *            A {@link Collection} of attached Hibernate entities to load.
     * @return the {@link Collection} of Hibernate entities fully loaded.
     *         Similar to the entities input parameter.
     *         Useful when calling this method in a return statement.
     */
    public static <C extends Collection<E>, E> C deepHydrate(EntityManager entityManager, C entities) {
        return hydrator(entityManager).deepHydrateCollection(entities);
    }

    /**
     * Populate a lazy-initialized object graph by recursion.
     * <p>
     * This method deeply navigates into a graph of entities in order to resolve uninitialized Hibernate proxies.<br>
     * The goal is to avoid any {@link LazyInitializationException} once entities are detached.<br>
     * Attention: This method has to be called from an open persistent context / Hibernate session.
     * </p>
     *
     * @param entityManager
     *            Open {@link EntityManager}.
     * @param entity
     *            An attached Hibernate entity to load.
     * @return the Hibernate entity fully loaded.
     *         Similar to the entity input parameter.
     *         Useful when calling this method in a return statement.
     */
    public static <E> E deepHydrate(EntityManager entityManager, E entity) {
        return hydrator(entityManager).deepHydrate(entity);
    }
}
