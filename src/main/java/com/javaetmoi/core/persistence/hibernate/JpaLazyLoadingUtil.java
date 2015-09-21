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

import java.util.Collection;

import javax.persistence.EntityManager;

import org.hibernate.ejb.HibernateEntityManager;

/**
 * Set of helper methods that fetch a complete entity graph.
 * 
 * <p>
 * Provides a lazy way to resolve all Hibernate proxies.
 * </p>
 * 
 * @author Antoine Rey
 */

public class JpaLazyLoadingUtil {

    /**
     * No-arg constructor
     */
    private JpaLazyLoadingUtil() {
        // Private visibility because utility class
    }

    /**
     * Populate a lazy-initialized object graph by recursivity.
     * 
     * <p>
     * This method deeply navigates into a graph of entities in order to resolve uninitialized
     * Hibernate proxies.<br>
     * The goal is to avoid any {@link org.hibernate.LazyInitializationException} once entities are detached from
     * the Hibernate session.<br>
     * May attention: this method has to be called from an open persistent context / Hibernate
     * session.
     * </p>
     * 
     * @param currentEntityManager
     *            current JPA entity manager still open
     * @param entities
     *            a {@link Collection} of Hibernate entities to load
     * @return the {@link Collection} of Hibernate entities fully loaded. Similar to the entities
     *         input parameter. Usefull when calling this method in a return statement.
     * 
     */
    public static <E> Collection<E> deepHydrate(final EntityManager currentEntityManager,
            Collection<E> entities) {
        if (currentEntityManager instanceof HibernateEntityManager) {
            HibernateEntityManager entityManager = (HibernateEntityManager) currentEntityManager;
            return LazyLoadingUtil.deepHydrate(entityManager.getSession(), entities);
        } else
            if (currentEntityManager instanceof org.hibernate.jpa.HibernateEntityManager) {
                org.hibernate.jpa.HibernateEntityManager entityManager = (HibernateEntityManager) currentEntityManager;
                return LazyLoadingUtil.deepHydrate(entityManager.getSession(), entities);
            }            
        throw new RuntimeException(
                "Only the Hibername implementation of JPA is currently supported");
    }

    /**
     * Populate a lazy-initialized object graph by recursivity.
     * 
     * <p>
     * This method deeply navigates into a graph of entities in order to resolve uninitialized
     * Hibernate proxies.<br>
     * The goal is to avoid any {@link org.hibernate.LazyInitializationException} once entities are detached from
     * the Hibernate session.<br>
     * May attention: this method has to be called from an open persistent context / Hibernate
     * session.
     * </p>
     * 
     * @param currentEntityManager
     *            current JPA entity manager still open
     * @param entity
     *            a single Hibernate entity or a simple java class referencing entities
     * @return the Hibernate entity fully loaded. Similar to the entity input parameter. Usefull
     *         when calling this method in a return statement.
     * 
     */
    public static <E> E deepHydrate(final EntityManager currentEntityManager, E entity) {
        if (currentEntityManager instanceof HibernateEntityManager) {
            HibernateEntityManager entityManager = (HibernateEntityManager) currentEntityManager;
            return LazyLoadingUtil.deepHydrate(entityManager.getSession(), entity);
        } else
        if (currentEntityManager instanceof org.hibernate.jpa.HibernateEntityManager) {
            org.hibernate.jpa.HibernateEntityManager entityManager = (org.hibernate.jpa.HibernateEntityManager) currentEntityManager;
            return LazyLoadingUtil.deepHydrate(entityManager.getSession(), entity);
        }
        throw new RuntimeException(
                "Only the Hibername implementation of JPA is currently supported");
    }

}
