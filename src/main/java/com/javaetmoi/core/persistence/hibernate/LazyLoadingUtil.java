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

import org.hibernate.EntityMode;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.collection.PersistentCollection;
import org.hibernate.collection.PersistentMap;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.type.ComponentType;
import org.hibernate.type.Type;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

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
     * The goal is to avoid any {@link lazyinitializationexception} once entities are detached from
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
     *         input parameter. Usefull when calling this method in a return statement.
     * 
     */
    public static <E> Collection<E> deepHydrate(final Session currentSession, Collection<E> entities) {
        for (Object entity : entities) {
            deepInflateEntity(currentSession, entity, new HashSet<String>());
        }
        return entities;
    }

    /**
     * Populate a lazy-initialized object graph by recursivity.
     * 
     * <p>
     * This method deeply navigates into a graph of entities in order to resolve uninitialized
     * Hibernate proxies.<br>
     * The goal is to avoid any {@link lazyinitializationexception} once entities are detached from
     * the Hibernate session.<br>
     * May attention: this method has to be called from an open persistent context / Hibernate
     * session.
     * </p>
     * 
     * @param currentSession
     *            Hibernate session still open
     * @param entity
     *            a single Hibernate entity or a simple java class referencing entities
     * @return the Hibernate entity fully loaded. Similar to the entity input parameter. Usefull
     *         when calling this method in a return statement.
     * 
     */
    public static <E> E deepHydrate(final Session currentSession, E entity) {
        deepInflateEntity(currentSession, entity, new HashSet<String>());
        return entity;
    }

    @SuppressWarnings("unchecked")
    private static void deepInflateEntity(final Session currentSession, Object entity,
            Set<String> recursiveGuard) throws HibernateException {
        if (entity == null) {
            return;
        }

        Class<? extends Object> persistentClass = entity.getClass();
        if (entity instanceof HibernateProxy) {
            persistentClass = ((HibernateProxy) entity).getHibernateLazyInitializer().getPersistentClass();
        }
        ClassMetadata classMetadata = currentSession.getSessionFactory().getClassMetadata(
                persistentClass);
        if (classMetadata == null) {
            return;
        }
        Serializable identifier = classMetadata.getIdentifier(entity,
                (SessionImplementor) currentSession);
        String key = persistentClass.getName() + "|" + identifier;

        if (recursiveGuard.contains(key)) {
            return;
        }
        recursiveGuard.add(key);

        if (!Hibernate.isInitialized(entity)) {
            Hibernate.initialize(entity);
        }

        for (int i = 0, n = classMetadata.getPropertyNames().length; i < n; i++) {
            String propertyName = classMetadata.getPropertyNames()[i];
            Type propertyType = classMetadata.getPropertyType(propertyName);
            Object propertyValue = null;
            if (entity instanceof javassist.util.proxy.ProxyObject) {
                // For javassist proxy, the classMetadata.getPropertyValue(..) method return en
                // emppty collection. So we have to call the property's getter in order to call the
                // JavassistLazyInitializer.invoke(..) method that will initialize the collection by
                // loading it from the database.
                propertyValue = callCollectionGetter(entity, propertyName);
            } else {
                propertyValue = classMetadata.getPropertyValue(entity, propertyName,
                        EntityMode.POJO);
            }
            deepInflateProperty(propertyValue, propertyType, currentSession, recursiveGuard);
        }
    }

    @SuppressWarnings({ "rawtypes" })
    private static void deepInflateProperty(Object propertyValue, Type propertyType,
            Session currentSession, Set<String> recursiveGuard) {
        if (propertyValue == null) {
            return; // null guard
        }

        if (propertyType.isEntityType()) {
            deepInflateEntity(currentSession, propertyValue, recursiveGuard);
        } else if (propertyType.isCollectionType()) {
            // Handle PersistentBag, PersistentList and PersistentIndentifierBag
            if (propertyValue instanceof List) {
                deepInflateCollection(currentSession, recursiveGuard, (List) propertyValue);
            } else if (propertyValue instanceof Map) {
                deepInflateMap(currentSession, recursiveGuard, (Map) propertyValue);
            } else if (propertyValue instanceof Set) {
                deepInflateCollection(currentSession, recursiveGuard, (Set) propertyValue);
            } else {
                throw new UnsupportedOperationException("Unsupported collection type: "
                        + propertyValue.getClass().getSimpleName());
            }
        } else if (propertyType.isComponentType()) {
            if (propertyType instanceof ComponentType) {
                // i.e. @Embeddable annotation (see
                // https://github.com/arey/hibernate-hydrate/issues/1)
                deepInflateComponent(currentSession, propertyValue, (ComponentType) propertyType,
                        recursiveGuard);
            }
        }
    }

    private static void deepInflateComponent(Session currentSession, Object componentValue,
            ComponentType componentType, Set<String> recursiveGuard) {
        // No public API to access to the component Hibernate metamodel => force to use
        // introspection instead
        String[] propertyNames = ReflectionUtil.getValue("propertyNames", componentType);
        Type[] propertyTypes = ReflectionUtil.getValue("propertyTypes", componentType);

        for (int i = 0; i < propertyNames.length; i++) {
            String propertyName = propertyNames[i];
            Type propertyType = propertyTypes[i];
            Object propertyValue = ReflectionUtil.getValue(propertyName, componentValue);
            deepInflateProperty(propertyValue, propertyType, currentSession, recursiveGuard);
        }

    }

    private static void deepInflateMap(Session currentSession, Set<String> recursiveGuard,
            @SuppressWarnings("rawtypes") Map map) {

        if (map instanceof PersistentMap) {
            if (!((PersistentMap) map).wasInitialized()) {
                Hibernate.initialize(map);
            }
        }

        if (map.size() > 0) {
            // First map keys
            @SuppressWarnings("rawtypes")
            Set keySet = map.keySet();
            for (Object key : keySet) {
                deepInflateEntity(currentSession, key, recursiveGuard);
            }
            // Then map values
            deepInflateCollection(currentSession, recursiveGuard, map.values());
        }
    }

    /**
     * 
     * 
     * @param sessionFactory
     * @param recursiveGuard
     * @param collection
     */
    private static void deepInflateCollection(Session currentSession, Set<String> recursiveGuard,
            @SuppressWarnings("rawtypes") Collection collection) {
        if (collection != null && collection.size() > 0) {
            ComponentType collectionType = null;
            if (collection instanceof PersistentCollection
                    && !((PersistentCollection) collection).isUnreferenced()) {
                // The isUnreferenced() test is useful for some persistent bags that does not have
                // any role
                String role = ((PersistentCollection) collection).getRole();
                Type type = currentSession.getSessionFactory().getCollectionMetadata(role).getElementType();
                if (type instanceof ComponentType) {
                    // ManyToMany relationship with @Embeddable annotation (see
                    // https://github.com/arey/hibernate-hydrate/issues/3)
                    collectionType = (ComponentType) type;
                }
            }
            for (Object item : collection) {
                if (item == null) {
                    continue;
                } else if (collectionType != null) {
                    deepInflateComponent(currentSession, item, collectionType, recursiveGuard);
                } else {
                    deepInflateEntity(currentSession, item, recursiveGuard);
                }
            }
        }
    }

    /**
     * Calls the getter of a collection property in order to resolve the javassist lazy proxy
     * object.
     * 
     * @param entity
     *            target object
     * @param propertyName
     *            name of the collection property (ie. clients)
     * @return the collection
     */
    protected static Object callCollectionGetter(Object entity, String propertyName) {
        try {
            Method getter = entity.getClass().getMethod(getterFromCollection(propertyName));
            getter.setAccessible(true);
            return getter.invoke(entity);
        } catch (NoSuchMethodException e) {
            // Wrap checked exception to the runtime unchecked exception
            return new RuntimeException(e);
        } catch (IllegalAccessException e) {
            return new RuntimeException(e);
        } catch (InvocationTargetException e) {
            return new RuntimeException(e);
        }
    }

    /**
     * Generate the getter name of a collection property.
     * 
     * @param propertyName
     *            name of the collection property (ie. clients)
     * @return name of the corresponding getter (ie. getClients)
     */
    protected static String getterFromCollection(String propertyName) {
        return new StringBuilder(propertyName.length() + 3).append("get").append(
                Character.toTitleCase(propertyName.charAt(0))).append(propertyName.substring(1)).toString();
    }
}
