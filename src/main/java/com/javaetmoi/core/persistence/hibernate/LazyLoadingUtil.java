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
import org.hibernate.metamodel.MappingMetamodel;
import org.hibernate.metamodel.mapping.EmbeddableValuedModelPart;
import org.hibernate.metamodel.mapping.EntityValuedModelPart;
import org.hibernate.metamodel.mapping.ModelPart;
import org.hibernate.metamodel.mapping.PluralAttributeMapping;
import org.hibernate.proxy.HibernateProxy;

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
     * No-arg constructor.
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
        var mappingMetamodel = sessionFactory.unwrap(SessionFactoryImplementor.class).getMappingMetamodel();
        // Reduce resizes for big collections.
        int capacity = Math.max(entities.size(), 32);
        var recursiveGuard = new IdentitySet<>(capacity);
        for (var entity : entities) {
            deepInflateEntity(mappingMetamodel, entity, null, recursiveGuard);
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
        var mappingMetamodel = sessionFactory.unwrap(SessionFactoryImplementor.class).getMappingMetamodel();
        var recursiveGuard = new IdentitySet<>();
        // TODO markus 2016-06-19: How to determine entity type?
        deepInflateEntity(mappingMetamodel, entity, null, recursiveGuard);
        return entity;
    }

    private static void deepInflateProperty(
            MappingMetamodel mappingMetamodel,
            Object propertyValue, ModelPart propertyType,
            IdentitySet<Object> recursiveGuard) {
        if (propertyValue == null) {
            return;
        }

        if (propertyType instanceof EntityValuedModelPart) {
            deepInflateEntity(mappingMetamodel, propertyValue, (EntityValuedModelPart) propertyType, recursiveGuard);
        } else if (propertyType instanceof EmbeddableValuedModelPart) {
            deepInflateComponent(mappingMetamodel, propertyValue, (EmbeddableValuedModelPart) propertyType, recursiveGuard);
        } else if (propertyType instanceof PluralAttributeMapping) {
            if (propertyValue instanceof Map) {
                deepInflateMap(mappingMetamodel, (Map<?, ?>) propertyValue, (PluralAttributeMapping) propertyType, recursiveGuard);
            } else if (propertyValue instanceof Collection) {
                deepInflateCollection(mappingMetamodel, (Collection<?>) propertyValue, (PluralAttributeMapping) propertyType, recursiveGuard);
            } else {
                throw new UnsupportedOperationException(String.format("Unsupported collection type %s for %s.",
                      propertyType.getClass().getSimpleName(), propertyValue.getClass().getSimpleName()));
            }
        }
    }

    /**
     * Populate a lazy-initialized object graph by recursion.
     *
     * @param mappingMetamodel
     *            The mapping metamodel.
     * @param entity
     *            The entity. May be {@code null}.
     * @param entityType
     *            The type of the entity. {@code null} if not known.
     * @param recursiveGuard
     *            A guard to avoid endless recursion.
     */
    private static void deepInflateEntity(
            MappingMetamodel mappingMetamodel,
            Object entity, EntityValuedModelPart entityType,
            IdentitySet<Object> recursiveGuard) {
        if (entity == null || !recursiveGuard.add(entity)) {
            return;
        }
        Hibernate.initialize(entity);

        String entityName;
        Object target;
        if (entity instanceof HibernateProxy) {
            var initializer = ((HibernateProxy) entity).getHibernateLazyInitializer();
            entityName = initializer.getEntityName();
            target = initializer.getImplementation();
        } else {
            entityName = entityType != null ? entityType.getEntityMappingType().getEntityName() : null;
            target = entity;
        }

        var descriptor = entityName != null ?
                mappingMetamodel.getEntityDescriptor(entityName) :
                mappingMetamodel.getEntityDescriptor(entity.getClass());
        if (descriptor == null) {
            return;
        }

        descriptor.getAttributeMappings().forEach(attributeMapping -> {
            var propertyValue = attributeMapping.getValue(target);
            deepInflateProperty(mappingMetamodel, propertyValue, attributeMapping, recursiveGuard);
        });
    }

    private static void deepInflateComponent(
            MappingMetamodel mappingMetamodel,
            Object component, EmbeddableValuedModelPart componentType,
            IdentitySet<Object> recursiveGuard) {
        if (component == null || !recursiveGuard.add(component)) {
            return;
        }

        var descriptor = componentType.getEmbeddableTypeDescriptor();
        descriptor.getAttributeMappings().forEach(attributeMapping -> {
            var propertyValue = attributeMapping.getValue(component);
            deepInflateProperty(mappingMetamodel, propertyValue, attributeMapping, recursiveGuard);
        });
    }

    private static void deepInflateMap(
            MappingMetamodel mappingMetamodel,
            Map<?, ?> map, PluralAttributeMapping mapType,
            IdentitySet<Object> recursiveGuard) {
        if (map == null || !recursiveGuard.add(map)) {
            return;
        }
        Hibernate.initialize(map);

        var attributeMapping = mapType.getCollectionDescriptor().getAttributeMapping();
        var indexType = attributeMapping.getIndexDescriptor();
        var elementType = attributeMapping.getElementDescriptor();
        map.forEach((index, element) -> {
            deepInflateProperty(mappingMetamodel, index, indexType, recursiveGuard);
            deepInflateProperty(mappingMetamodel, element, elementType, recursiveGuard);
        });
    }

    private static void deepInflateCollection(
            MappingMetamodel mappingMetamodel,
            Collection<?> collection, PluralAttributeMapping collectionType,
            IdentitySet<Object> recursiveGuard) {
        if (collection == null || !recursiveGuard.add(collection)) {
            return;
        }
        Hibernate.initialize(collection);

        var elementType = collectionType.getElementDescriptor();
        collection.forEach(element ->
                deepInflateProperty(mappingMetamodel, element, elementType, recursiveGuard));
    }
}
