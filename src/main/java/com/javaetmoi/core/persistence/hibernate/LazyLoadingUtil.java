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
import org.hibernate.metamodel.mapping.AttributeMapping;
import org.hibernate.metamodel.spi.MappingMetamodelImplementor;
import org.hibernate.proxy.HibernateProxy;
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
     * No-arg constructor,
     */
    private LazyLoadingUtil() {
        // Private visibility because utility class.
    }

    /**
     * Populate a lazy-initialized object graph by recursion.
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
     * Populate a lazy-initialized object graph by recursion.
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
        var sessionFactoryImplementor = sessionFactory.unwrap(SessionFactoryImplementor.class);
        // Reduce resizes for big collections.
        // *2 to compensate for the load factor.
        int capacity = Math.max(entities.size() * 2, 32);
        var recursiveGuard = new IdentitySet<>(capacity);
        for (var entity : entities) {
            // TODO markus 2016-06-19: How to determine entity type?
            deepInflateEntity(sessionFactoryImplementor, entity, null, recursiveGuard);
        }
        return entities;
    }

    /**
     * Populate a lazy-initialized object graph by recursion.
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
     * Populate a lazy-initialized object graph by recursion.
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
        var sessionFactoryImplementor = sessionFactory.unwrap(SessionFactoryImplementor.class);
        var recursiveGuard = new IdentitySet<>();
        // TODO markus 2016-06-19: How to determine entity type?
        deepInflateEntity(sessionFactoryImplementor, entity, null, recursiveGuard);
        return entity;
    }

    private static void deepInflateProperty(
            SessionFactoryImplementor sessionFactory,
            Object propertyValue, Type propertyType,
            IdentitySet<Object> recursiveGuard) {
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
            SessionFactoryImplementor sessionFactory,
            Object entity, EntityType entityType,
            IdentitySet<Object> recursiveGuard) {
        if (entity == null || !recursiveGuard.add(entity)) {
            return;
        }
        Hibernate.initialize(entity);

        var name = entityType != null ? entityType.getName() : null;
        var target = entity;
        if (entity instanceof HibernateProxy) {
            var initializer = ((HibernateProxy) entity).getHibernateLazyInitializer();
            name = initializer.getEntityName();
            target = initializer.getImplementation();
        }

        var mappingMetamodel = sessionFactory.getMappingMetamodel();
        var descriptor = name != null ?
                mappingMetamodel.getEntityDescriptor(name) :
                mappingMetamodel.getEntityDescriptor(entity.getClass());
        if (descriptor == null) {
            return;
        }

        var propertyTypes = descriptor.getPropertyTypes();
        for (var attributeMapping : descriptor.getAttributeMappings()) {
            var propertyValue = attributeMapping.getValue(target);
            var propertyType = propertyTypes[attributeMapping.getStateArrayPosition()];
            deepInflateProperty(sessionFactory, propertyValue, propertyType, recursiveGuard);
        }
    }

    private static void deepInflateComponent(
            SessionFactoryImplementor sessionFactory,
            Object component, ComponentType componentType,
            IdentitySet<Object> recursiveGuard) {
        if (component == null || !recursiveGuard.add(component)) {
            return;
        }

        var propertyTypes = componentType.getSubtypes();
        for (int i = 0; i < propertyTypes.length; i++) {
            var propertyValue = componentType.getPropertyValue(component, i);
            deepInflateProperty(sessionFactory, propertyValue, propertyTypes[i], recursiveGuard);
        }
    }

    private static void deepInflateMap(
            SessionFactoryImplementor sessionFactory,
            Map<?, ?> map, MapType mapType,
            IdentitySet<Object> recursiveGuard) {
        if (map == null || !recursiveGuard.add(map)) {
            return;
        }
        Hibernate.initialize(map);

        if (map.isEmpty()) {
            return;
        }

        var mappingMetamodel = sessionFactory.getMappingMetamodel();
        var descriptor = mappingMetamodel.getCollectionDescriptor(mapType.getRole());
        var indexType = descriptor.getIndexType();
        var elementType = descriptor.getElementType();
        map.forEach((index, element) -> {
            deepInflateProperty(sessionFactory, index, indexType, recursiveGuard);
            deepInflateProperty(sessionFactory, element, elementType, recursiveGuard);
        });
    }

    private static void deepInflateCollection(
            SessionFactoryImplementor sessionFactory,
            Collection<?> collection, CollectionType collectionType,
            IdentitySet<Object> recursiveGuard) {
        if (collection == null || !recursiveGuard.add(collection)) {
            return;
        }
        Hibernate.initialize(collection);

        if (collection.isEmpty()) {
            return;
        }

        var mappingMetamodel = sessionFactory.getMappingMetamodel();
        var descriptor = mappingMetamodel.getCollectionDescriptor(collectionType.getRole());
        var elementType = descriptor.getElementType();
        collection.forEach(element ->
                deepInflateProperty(sessionFactory, element, elementType, recursiveGuard));
    }
}
