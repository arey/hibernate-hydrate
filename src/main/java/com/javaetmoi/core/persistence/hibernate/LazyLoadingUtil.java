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

import org.hibernate.Hibernate;
import org.hibernate.LazyInitializationException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.internal.util.collections.IdentitySet;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;
import org.hibernate.type.*;

import java.util.Collection;
import java.util.Map;

/**
 * Set of helper methods that fetch a complete entity graph.
 * 
 * <p>
 * Provides a lazy way to resolve all Hibernate proxies.
 * </p>
 * 
 * @author Antoine Rey
 */
public class LazyLoadingUtil {
    /**
     * No-arg constructor
     */
    private LazyLoadingUtil() {
        // Private visibility because utility class.
    }

    /**
     * Populate a lazy-initialized object graph by recursivity.
     * 
     * <p>
     * This method deeply navigates into a graph of entities in order to resolve uninitialized Hibernate proxies.<br>
     * The goal is to avoid any {@link LazyInitializationException} once entities are detached from the Hibernate session.<br>
     * May attention: this method has to be called from an open persistent context / Hibernate session.
     * </p>
     * 
     * @param currentSession
     *            Hibernate session still open
     * @param entities
     *            A {@link Collection} of attached Hibernate entities to load
     * @return the {@link Collection} of Hibernate entities fully loaded. Similar to the entities
     *         input parameter. Useful when calling this method in a return statement.
     */
    public static <C extends Collection<E>, E> C deepHydrate(Session currentSession, C entities) {
        return deepHydrate(currentSession.getSessionFactory(), entities);
    }

    /**
     * Populate a lazy-initialized object graph by recursivity.
     *
     * <p>
     * This method deeply navigates into a graph of entities in order to resolve uninitialized Hibernate proxies.<br>
     * The goal is to avoid any {@link LazyInitializationException} once entities are detached from the Hibernate session.<br>
     * </p>
     *
     * @param sessionFactory
     *            Hibernate session factory
     * @param entities
     *            A {@link Collection} of attached Hibernate entities to load
     * @return the {@link Collection} of Hibernate entities fully loaded. Similar to the entities
     *         input parameter. Useful when calling this method in a return statement.
     */
    public static <C extends Collection<E>, E> C deepHydrate(SessionFactory sessionFactory, C entities) {
        SessionFactoryImplementor sessionFactoryImplementor = sessionFactory.unwrap(SessionFactoryImplementor.class);
        // Reduce resizes for big collections.
        // *2 to compensate for the load factor.
        int capacity = Math.max(entities.size() * 2, 32);
        IdentitySet recursiveGuard = new IdentitySet(capacity);
        for (Object entity : entities) {
            // TODO markus 2016-06-19: How to determine entity type?
            deepInflateEntity(sessionFactoryImplementor, entity, null, recursiveGuard);
        }
        return entities;
    }

    /**
     * Populate a lazy-initialized object graph by recursivity.
     * 
     * <p>
     * This method deeply navigates into a graph of entities in order to resolve uninitialized Hibernate proxies.<br>
     * The goal is to avoid any {@link LazyInitializationException} once entities are detached from the Hibernate session.<br>
     * May attention: this method has to be called from an open persistent context / Hibernate session.
     * </p>
     * 
     * @param currentSession
     *            Hibernate session still open
     * @param entity
     *            A single attached Hibernate entity or a simple java class referencing entities
     * @return the Hibernate entity fully loaded. Similar to the entity input parameter. Useful
     *         when calling this method in a return statement.
     */
    public static <E> E deepHydrate(Session currentSession, E entity) {
        return deepHydrate(currentSession.getSessionFactory(), entity);
    }

    /**
     * Populate a lazy-initialized object graph by recursivity.
     *
     * <p>
     * This method deeply navigates into a graph of entities in order to resolve uninitialized Hibernate proxies.<br>
     * The goal is to avoid any {@link LazyInitializationException} once entities are detached from the Hibernate session.<br>
     * </p>
     *
     * @param sessionFactory
     *            Hibernate session factory
     * @param entity
     *            A single attached Hibernate entity or a simple java class referencing entities
     * @return the Hibernate entity fully loaded. Similar to the entity input parameter. Useful
     *         when calling this method in a return statement.
     */
    public static <E> E deepHydrate(SessionFactory sessionFactory, E entity) {
        SessionFactoryImplementor sessionFactoryImplementor = sessionFactory.unwrap(SessionFactoryImplementor.class);
        IdentitySet recursiveGuard = new IdentitySet();
        // TODO markus 2016-06-19: How to determine entity type?
        deepInflateEntity(sessionFactoryImplementor, entity, null, recursiveGuard);
        return entity;
    }

    private static void deepInflateProperty(
            SessionFactoryImplementor sessionFactory, Object propertyValue, Type propertyType, IdentitySet recursiveGuard) {
        if (propertyValue == null) {
            return; // null guard
        }

        if (propertyType instanceof EntityType) {
            deepInflateEntity(sessionFactory, propertyValue, (EntityType) propertyType, recursiveGuard);
        } else if (propertyType instanceof ComponentType) {
            // i.e. @Embeddable annotation (see https://github.com/arey/hibernate-hydrate/issues/1)
            deepInflateComponent(sessionFactory, propertyValue, (ComponentType) propertyType, recursiveGuard);
        } else if (propertyType instanceof MapType) {
            deepInflateMap(sessionFactory, (Map<?, ?>) propertyValue, (MapType) propertyType, recursiveGuard);
        } else if (propertyType instanceof CollectionType) {
            if (propertyValue instanceof Collection) {
                deepInflateCollection(sessionFactory, (Collection<?>) propertyValue, (CollectionType) propertyType, recursiveGuard);
            } else {
                throw new UnsupportedOperationException(String.format("Unsupported collection type %s for %s.",
                      propertyType.getClass().getSimpleName(), propertyValue.getClass().getSimpleName()));
            }
        }
    }

    private static void deepInflateEntity(
            SessionFactoryImplementor sessionFactory, Object entity, EntityType entityType, IdentitySet recursiveGuard) {
        if (entity == null || !recursiveGuard.add(entity)) {
            return;
        }
        Hibernate.initialize(entity);

        String name = entityType != null? entityType.getName() : entity.getClass().getName();
        Object target = entity;
        if (entity instanceof HibernateProxy) {
            LazyInitializer initializer = ((HibernateProxy) entity).getHibernateLazyInitializer();
            name = initializer.getEntityName();
            target = initializer.getImplementation();
        }

        EntityPersister persister = sessionFactory.getMetamodel().entityPersisters().get(name);
        if (persister == null) {
            return;
        }

        String[] propertyNames = persister.getPropertyNames();
        Type[] propertyTypes = persister.getPropertyTypes();
        for (int i = 0, n = propertyNames.length; i < n; i++) {
            Object propertyValue = persister.getPropertyValue(target, propertyNames[i]);
            deepInflateProperty(sessionFactory, propertyValue, propertyTypes[i], recursiveGuard);
        }
    }

    private static void deepInflateComponent(
            SessionFactoryImplementor sessionFactory, Object component, ComponentType componentType, IdentitySet recursiveGuard) {
        if (component == null || !recursiveGuard.add(component)) {
            return;
        }

        Type[] propertyTypes = componentType.getSubtypes();
        for (int i = 0; i < propertyTypes.length; i++) {
            Object propertyValue = componentType.getPropertyValue(component, i);
            deepInflateProperty(sessionFactory, propertyValue, propertyTypes[i], recursiveGuard);
        }
    }

    private static void deepInflateMap(
            SessionFactoryImplementor sessionFactory, Map<?, ?> map, MapType mapType, IdentitySet recursiveGuard) {
        if (map == null || !recursiveGuard.add(map)) {
            return;
        }
        Hibernate.initialize(map);

        if (map.isEmpty()) {
            return;
        }

        CollectionPersister persister = sessionFactory.getMetamodel().collectionPersister(mapType.getRole());

        // First map keys
        Type indexType = persister.getIndexType();
        for (Object index : map.keySet()) {
            deepInflateProperty(sessionFactory, index, indexType, recursiveGuard);
        }
        // Then map values
        Type elementType = mapType.getElementType(sessionFactory);
        for (Object element : map.values()) {
            deepInflateProperty(sessionFactory, element, elementType, recursiveGuard);
        }
    }

    private static void deepInflateCollection(
            SessionFactoryImplementor sessionFactory, Collection<?> collection, CollectionType collectionType, IdentitySet recursiveGuard) {
        if (collection == null || !recursiveGuard.add(collection)) {
            return;
        }
        Hibernate.initialize(collection);

        if (collection.isEmpty()) {
            return;
        }

        Type elementType = collectionType.getElementType(sessionFactory);
        for (Object element : collection) {
            deepInflateProperty(sessionFactory, element, elementType, recursiveGuard);
        }
    }
}
