package com.javaetmoi.core.persistence.hibernate;

import jakarta.persistence.EntityManagerFactory;
import org.hibernate.Hibernate;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.internal.util.collections.IdentitySet;
import org.hibernate.metamodel.mapping.EmbeddableValuedModelPart;
import org.hibernate.metamodel.mapping.EntityValuedModelPart;
import org.hibernate.metamodel.mapping.ModelPart;
import org.hibernate.metamodel.mapping.PluralAttributeMapping;
import org.hibernate.metamodel.model.domain.NavigableRole;
import org.hibernate.metamodel.spi.MappingMetamodelImplementor;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Default implementation of {@link Hydrator}.
 */
class HydratorImpl implements Hydrator {
    /**
     * Mapping metamodel.
     */
    private final MappingMetamodelImplementor mappingMetamodel;

    /**
     * Excludes from hydration.
     */
    private final Set<NavigableRole> excludes;

    /**
     * Convenience constructor.
     */
    HydratorImpl(EntityManagerFactory entityManagerFactory) {
        this(entityManagerFactory.unwrap(SessionFactoryImplementor.class).getMappingMetamodel(), Set.of());
    }

    /**
     * Base constructor.
     */
    HydratorImpl(MappingMetamodelImplementor mappingMetamodel, Set<NavigableRole> excludes) {
        this.mappingMetamodel = mappingMetamodel;
        this.excludes = Set.copyOf(excludes);
    }

    @Override
    public Hydrator withExclude(Class<?> entityClass, String attribute) {
        var entityType = mappingMetamodel.getEntityDescriptor(entityClass);
        var entityName = entityType.getEntityName();
        var attributeMapping = entityType.findAttributeMapping(attribute);
        if (attributeMapping == null) {
            throw new IllegalArgumentException(String.format(
                    "The attribute %s does not exist at the entity %s.", attribute, entityName));
        }

        var newExcludes = new HashSet<>(this.excludes);
        newExcludes.add(attributeMapping.getNavigableRole());

        return new HydratorImpl(mappingMetamodel, newExcludes);
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
            Object propertyValue, ModelPart part, IdentitySet<Object> recursiveGuard) {
        if (propertyValue == null) {
            return;
        }

        if (part instanceof EntityValuedModelPart) {
            deepInflateEntity(propertyValue, (EntityValuedModelPart) part, recursiveGuard);
        } else if (part instanceof EmbeddableValuedModelPart) {
            deepInflateEmbedded(propertyValue, (EmbeddableValuedModelPart) part, recursiveGuard);
        } else if (part instanceof PluralAttributeMapping) {
            if (propertyValue instanceof Map) {
                deepInflateMap((Map<?, ?>) propertyValue, (PluralAttributeMapping) part, recursiveGuard);
            } else if (propertyValue instanceof Collection) {
                deepInflateCollection((Collection<?>) propertyValue, (PluralAttributeMapping) part, recursiveGuard);
            } else {
                throw new UnsupportedOperationException(String.format("Unsupported collection type %s for %s.",
                        propertyValue.getClass().getSimpleName(), part.getNavigableRole().getFullPath()));
            }
        }
    }

    /**
     * Deep inflate an entity.
     */
    private void deepInflateEntity(
            Object entity, EntityValuedModelPart part, IdentitySet<Object> recursiveGuard) {
        if (entity == null || !recursiveGuard.add(entity) || excludes.contains(part.getNavigableRole())) {
            return;
        }
        Hibernate.initialize(entity);

        var target = Hibernate.unproxy(entity);
        var descriptor = part.getEntityMappingType();
        descriptor.getAttributeMappings().forEach(attributeMapping -> {
            var propertyValue = attributeMapping.getValue(target);
            deepInflateProperty(propertyValue, attributeMapping, recursiveGuard);
        });
    }

    /**
     * Deep inflate an embedded entity.
     */
    private void deepInflateEmbedded(
            Object embeddable, EmbeddableValuedModelPart part, IdentitySet<Object> recursiveGuard) {
        if (embeddable == null || !recursiveGuard.add(embeddable) || excludes.contains(part.getNavigableRole())) {
            return;
        }

        var descriptor = part.getEmbeddableTypeDescriptor();
        descriptor.getAttributeMappings().forEach(attributeMapping -> {
            var propertyValue = attributeMapping.getValue(embeddable);
            deepInflateProperty(propertyValue, attributeMapping, recursiveGuard);
        });
    }

    /**
     * Deep inflate a map including its keys and values.
     */
    private void deepInflateMap(
            Map<?, ?> map, PluralAttributeMapping part, IdentitySet<Object> recursiveGuard) {
        if (map == null || !recursiveGuard.add(map) || excludes.contains(part.getNavigableRole())) {
            return;
        }
        Hibernate.initialize(map);

        var indexType = part.getIndexDescriptor();
        var elementType = part.getElementDescriptor();
        map.forEach((index, element) -> {
            deepInflateProperty(index, indexType, recursiveGuard);
            deepInflateProperty(element, elementType, recursiveGuard);
        });
    }

    /**
     * Deep inflate a collection including its elements.
     */
    private void deepInflateCollection(
            Collection<?> collection, PluralAttributeMapping part, IdentitySet<Object> recursiveGuard) {
        if (collection == null || !recursiveGuard.add(collection) || excludes.contains(part.getNavigableRole())) {
            return;
        }
        Hibernate.initialize(collection);

        var elementType = part.getElementDescriptor();
        collection.forEach(element ->
                deepInflateProperty(element, elementType, recursiveGuard));
    }
}
