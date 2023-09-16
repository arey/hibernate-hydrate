package com.javaetmoi.core.persistence.hibernate;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.Hibernate;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.internal.util.collections.IdentitySet;
import org.hibernate.metamodel.mapping.EmbeddableValuedModelPart;
import org.hibernate.metamodel.mapping.EntityValuedModelPart;
import org.hibernate.metamodel.mapping.ModelPart;
import org.hibernate.metamodel.mapping.PluralAttributeMapping;
import org.hibernate.metamodel.spi.MappingMetamodelImplementor;

import java.util.Collection;
import java.util.Map;

/**
 * Default implementation of {@link Hydrator}.
 */
class HydratorImpl implements Hydrator {
    /**
     * Mapping metamodel.
     */
    private final MappingMetamodelImplementor mappingMetamodel;

    /**
     * Base constructor.
     */
    HydratorImpl(MappingMetamodelImplementor mappingMetamodel) {
        this.mappingMetamodel = mappingMetamodel;
    }

    @Override
    public <C extends Collection<E>, E> C deepHydrateCollection(C entities) {
        // Reduce resizes for big collections.
        int capacity = Math.max(entities.size(), 32);
        var recursiveGuard = new IdentitySet<>(capacity);
        entities.forEach(entity ->
                deepInflateInitialEntity(entity, recursiveGuard));
        return entities;
    }

    @Override
    public <E> E deepHydrate(E entity) {
        var recursiveGuard = new IdentitySet<>();
        deepInflateInitialEntity(entity, recursiveGuard);
        return entity;
    }

    /**
     * Populate a lazy-initialized object graph by recursion. Recursion entry point.
     *
     * @param entity
     *            The entity. May be {@code null}.
     * @param recursiveGuard
     *            A guard to avoid endless recursion.
     */
    private void deepInflateInitialEntity(Object entity, IdentitySet<Object> recursiveGuard) {
        if (entity == null) {
            return;
        }

        var entityType = mappingMetamodel.getEntityDescriptor(Hibernate.getClass(entity));
        deepInflateEntity(entity, entityType, recursiveGuard);
    }

    private void deepInflateProperty(
            Object propertyValue, ModelPart propertyType, IdentitySet<Object> recursiveGuard) {
        if (propertyValue == null) {
            return;
        }

        if (propertyType instanceof EntityValuedModelPart) {
            deepInflateEntity(propertyValue, (EntityValuedModelPart) propertyType, recursiveGuard);
        } else if (propertyType instanceof EmbeddableValuedModelPart) {
            deepInflateEmbedded(propertyValue, (EmbeddableValuedModelPart) propertyType, recursiveGuard);
        } else if (propertyType instanceof PluralAttributeMapping) {
            if (propertyValue instanceof Map) {
                deepInflateMap((Map<?, ?>) propertyValue, (PluralAttributeMapping) propertyType, recursiveGuard);
            } else if (propertyValue instanceof Collection) {
                deepInflateCollection((Collection<?>) propertyValue, (PluralAttributeMapping) propertyType, recursiveGuard);
            } else {
                throw new UnsupportedOperationException(String.format("Unsupported collection type %s for %s.",
                        propertyValue.getClass().getSimpleName(), propertyType.getNavigableRole().getFullPath()));
            }
        }
    }

    /**
     * Deep inflate an entity.
     */
    private void deepInflateEntity(
            Object entity, EntityValuedModelPart entityType, IdentitySet<Object> recursiveGuard) {
        if (entity == null || !recursiveGuard.add(entity)) {
            return;
        }
        Hibernate.initialize(entity);

        var target = Hibernate.unproxy(entity);
        var descriptor = entityType.getEntityMappingType();
        descriptor.getAttributeMappings().forEach(attributeMapping -> {
            var propertyValue = attributeMapping.getValue(target);
            deepInflateProperty(propertyValue, attributeMapping, recursiveGuard);
        });
    }

    /**
     * Deep inflate an embedded entity.
     */
    private void deepInflateEmbedded(
            Object embeddable, EmbeddableValuedModelPart componentType, IdentitySet<Object> recursiveGuard) {
        if (embeddable == null || !recursiveGuard.add(embeddable)) {
            return;
        }

        var descriptor = componentType.getEmbeddableTypeDescriptor();
        descriptor.getAttributeMappings().forEach(attributeMapping -> {
            var propertyValue = attributeMapping.getValue(embeddable);
            deepInflateProperty(propertyValue, attributeMapping, recursiveGuard);
        });
    }

    /**
     * Deep inflate a map including its keys and values.
     */
    private void deepInflateMap(
            Map<?, ?> map, PluralAttributeMapping mapType, IdentitySet<Object> recursiveGuard) {
        if (map == null || !recursiveGuard.add(map)) {
            return;
        }
        Hibernate.initialize(map);

        var indexType = mapType.getIndexDescriptor();
        var elementType = mapType.getElementDescriptor();
        map.forEach((index, element) -> {
            deepInflateProperty(index, indexType, recursiveGuard);
            deepInflateProperty(element, elementType, recursiveGuard);
        });
    }

    /**
     * Deep inflate a collection including its elements.
     */
    private void deepInflateCollection(
            Collection<?> collection, PluralAttributeMapping collectionType, IdentitySet<Object> recursiveGuard) {
        if (collection == null || !recursiveGuard.add(collection)) {
            return;
        }
        Hibernate.initialize(collection);

        var elementType = collectionType.getElementDescriptor();
        collection.forEach(element ->
                deepInflateProperty(element, elementType, recursiveGuard));
    }
}
