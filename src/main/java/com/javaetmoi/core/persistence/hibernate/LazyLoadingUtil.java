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

import org.hibernate.Hibernate;
import org.hibernate.LazyInitializationException;
import org.hibernate.Session;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.internal.util.collections.IdentitySet;
import org.hibernate.metamodel.spi.MetamodelImplementor;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;
import org.hibernate.type.ComponentType;
import org.hibernate.type.Type;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

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
        // Private visibility because utility class
    }

    /**
     * Populate a lazy-initialized object graph by recursivity.
     * 
     * <p>
     * This method deeply navigates into a graph of entities in order to resolve uninitialized
     * Hibernate proxies.<br>
     * The goal is to avoid any {@link LazyInitializationException} once entities are detached from
     * the Hibernate session.<br>
     * May attention: this method has to be called from an open persistent context / Hibernate
     * session.
     * </p>
     * 
     * @param currentSession
     *            Hibernate session still open
     * @param entities
     *            a {@link Collection} of Hibernate entities to load
     * @return the {@link Collection} of Hibernate entities fully loaded. Similar to the entities
     *         input parameter. Useful when calling this method in a return statement.
     * 
     */
    public static <E> Collection<E> deepHydrate(final Session currentSession, Collection<E> entities) {
        IdentitySet recursiveGuard = new IdentitySet();
        for (Object entity : entities) {
            deepInflateEntity(currentSession, entity, recursiveGuard);
        }
        return entities;
    }

    /**
     * Populate a lazy-initialized object graph by recursivity.
     * 
     * <p>
     * This method deeply navigates into a graph of entities in order to resolve uninitialized
     * Hibernate proxies.<br>
     * The goal is to avoid any {@link LazyInitializationException} once entities are detached from
     * the Hibernate session.<br>
     * May attention: this method has to be called from an open persistent context / Hibernate
     * session.
     * </p>
     * 
     * @param currentSession
     *            Hibernate session still open
     * @param entity
     *            a single Hibernate entity or a simple java class referencing entities
     * @return the Hibernate entity fully loaded. Similar to the entity input parameter. Useful
     *         when calling this method in a return statement.
     * 
     */
    public static <E> E deepHydrate(final Session currentSession, E entity) {
        IdentitySet recursiveGuard = new IdentitySet();
        deepInflateEntity(currentSession, entity, recursiveGuard);
        return entity;
    }

    private static void deepInflateEntity(
            final Session currentSession, Object entity, IdentitySet recursiveGuard) {
        if (entity == null || !recursiveGuard.add(entity)) {
            return;
        }
        Hibernate.initialize(entity);

        String name = entity.getClass().getName();
        Object target = entity;
        if (entity instanceof HibernateProxy) {
            LazyInitializer initializer = ((HibernateProxy) entity).getHibernateLazyInitializer();
            name = initializer.getEntityName();
            target = initializer.getImplementation();
        }

        EntityPersister persister = ((MetamodelImplementor) currentSession.getMetamodel()).entityPersisters().get(name);
        if (persister == null) {
            return;
        }

        String[] propertyNames = persister.getPropertyNames();
        Type[] propertyTypes = persister.getPropertyTypes();
        for (int i = 0, n = propertyNames.length; i < n; i++) {
            Object propertyValue = persister.getPropertyValue(target, propertyNames[i]);
            deepInflateProperty(currentSession, propertyValue, propertyTypes[i], recursiveGuard);
        }
    }

    private static void deepInflateProperty(
            Session currentSession, Object propertyValue, Type propertyType, IdentitySet recursiveGuard) {
        if (propertyValue == null) {
            return; // null guard
        }

        if (propertyType.isEntityType()) {
            deepInflateEntity(currentSession, propertyValue, recursiveGuard);
        } else if (propertyType.isCollectionType()) {
            if (propertyValue instanceof Map) {
                deepInflateMap(currentSession, (Map<?, ?>) propertyValue, recursiveGuard);
            } else if (propertyValue instanceof Collection) {
                // Handle PersistentBag, PersistentList and PersistentIdentifierBag
                deepInflateCollection(currentSession, (Collection<?>) propertyValue, recursiveGuard);
            } else {
                throw new UnsupportedOperationException(
                        "Unsupported collection type: " + propertyValue.getClass().getSimpleName());
            }
        } else if (propertyType.isComponentType()) {
            if (propertyType instanceof ComponentType) {
                // i.e. @Embeddable annotation (see
                // https://github.com/arey/hibernate-hydrate/issues/1)
                deepInflateComponent(currentSession, propertyValue, (ComponentType) propertyType, recursiveGuard);
            }
        }
    }

    private static void deepInflateComponent(
            Session currentSession, Object componentValue, ComponentType componentType, IdentitySet recursiveGuard) {
        if (componentValue == null || !recursiveGuard.add(componentValue)) {
            return;
        }

        Type[] propertyTypes = componentType.getSubtypes();
        for (int i = 0; i < propertyTypes.length; i++) {
            Object propertyValue = componentType.getPropertyValue(componentValue, i);
            deepInflateProperty(currentSession, propertyValue, propertyTypes[i], recursiveGuard);
        }

    }

    private static void deepInflateMap(
            Session currentSession, Map<?, ?> map, IdentitySet recursiveGuard) {
        if (map == null || !recursiveGuard.add(map)) {
            return;
        }
        Hibernate.initialize(map);

        if (!map.isEmpty()) {
            // First map keys
            Set<?> keySet = map.keySet();
            for (Object key : keySet) {
                deepInflateEntity(currentSession, key, recursiveGuard);
            }
            // Then map values
            deepInflateCollection(currentSession, map.values(), recursiveGuard);
        }
    }

    private static void deepInflateCollection(
            Session currentSession, Collection<?> collection, IdentitySet recursiveGuard) {
        if (collection == null || !recursiveGuard.add(collection)) {
            return;
        }
        Hibernate.initialize(collection);

        if (!collection.isEmpty()) {
            ComponentType componentType = null;
            if (collection instanceof PersistentCollection && !((PersistentCollection) collection).isUnreferenced()) {
                // The isUnreferenced() test is useful for some persistent bags that does not have any role
                String role = ((PersistentCollection) collection).getRole();
                CollectionPersister persister = ((MetamodelImplementor) currentSession.getMetamodel()).collectionPersister(role);
                Type type = persister.getElementType();
                if (type instanceof ComponentType) {
                    // ManyToMany relationship with @Embeddable annotation (see
                    // https://github.com/arey/hibernate-hydrate/issues/3)
                    componentType = (ComponentType) type;
                }
            }
            for (Object item : collection) {
                if (item == null) {
                    continue;
                } else if (componentType != null) {
                    deepInflateComponent(currentSession, item, componentType, recursiveGuard);
                } else {
                    deepInflateEntity(currentSession, item, recursiveGuard);
                }
            }
        }
    }
}
