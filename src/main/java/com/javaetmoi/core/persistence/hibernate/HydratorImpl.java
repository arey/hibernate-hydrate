package com.javaetmoi.core.persistence.hibernate;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.hibernate.Hibernate;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.internal.util.collections.IdentitySet;
import org.hibernate.metamodel.mapping.AttributeMapping;
import org.hibernate.metamodel.mapping.CompositeIdentifierMapping;
import org.hibernate.metamodel.mapping.DiscriminatedAssociationModelPart;
import org.hibernate.metamodel.mapping.EmbeddableValuedModelPart;
import org.hibernate.metamodel.mapping.EntityMappingType;
import org.hibernate.metamodel.mapping.EntityValuedModelPart;
import org.hibernate.metamodel.mapping.ModelPart;
import org.hibernate.metamodel.mapping.PluralAttributeMapping;
import org.hibernate.metamodel.mapping.VirtualModelPart;
import org.hibernate.metamodel.model.domain.NavigableRole;
import org.hibernate.metamodel.spi.MappingMetamodelImplementor;

import jakarta.persistence.EntityManagerFactory;

import static java.lang.Math.max;

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
        var entityDescriptor = mappingMetamodel.getEntityDescriptor(entityClass);
        var attributeMapping = findAttributeMapping(entityDescriptor, attribute);
        if (attributeMapping == null) {
            throw new IllegalArgumentException("The attribute %s does not exist at the entity %s.".formatted(
                    attribute, entityDescriptor.getEntityName()));
        }

        var newExcludes = new HashSet<>(this.excludes);
        newExcludes.add(attributeMapping.getNavigableRole());

        return new HydratorImpl(mappingMetamodel, newExcludes);
    }

    /**
     * Find the attribute of the entity.
     * Considers the identifier attributes of an entity with an {@code @IdClass} too,
     * because they are attributes of the entity, but are not part of its attribute mappings.
     */
    private static AttributeMapping findAttributeMapping(EntityMappingType entityDescriptor, String attribute) {
        var attributeMapping = entityDescriptor.findAttributeMapping(attribute);
        if (attributeMapping == null &&
                entityDescriptor.getIdentifierMapping() instanceof CompositeIdentifierMapping identifierMapping &&
                identifierMapping instanceof VirtualModelPart) {
            return identifierMapping.getEmbeddableTypeDescriptor().findAttributeMapping(attribute);
        }
        return attributeMapping;
    }

    @Override
    public <C extends Collection<E>, E> C deepHydrateCollection(C entities) {
        // Reduce resizes for big collections.
        var capacity = max(entities.size(), 32);
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
     * Populate a lazy-initialized object graph by recursion.
     * Entry point for entities whose entity type is not known from the mapping.
     *
     * @param entity
     *            The entity. May be {@code null}. Objects that are no entities are ignored.
     * @param recursiveGuard
     *            A guard to avoid endless recursion.
     */
    private void deepInflateInitialEntity(Object entity, IdentitySet<Object> recursiveGuard) {
        if (entity == null) {
            return;
        }

        var entityType = mappingMetamodel.findEntityDescriptor(Hibernate.getClass(entity));
        if (entityType == null) {
            // No entity. Nothing to hydrate.
            return;
        }
        deepInflateEntity(entity, entityType, recursiveGuard);
    }

    private void deepInflateProperty(
            Object propertyValue, ModelPart part, IdentitySet<Object> recursiveGuard) {
        if (propertyValue == null) {
            return;
        }

        if (part instanceof EntityValuedModelPart entityValuedModelPart) {
            deepInflateEntity(propertyValue, entityValuedModelPart, recursiveGuard);
        } else if (part instanceof DiscriminatedAssociationModelPart discriminatedAssociationModelPart) {
            deepInflateAny(propertyValue, discriminatedAssociationModelPart, recursiveGuard);
        } else if (part instanceof EmbeddableValuedModelPart embeddableValuedModelPart) {
            deepInflateEmbedded(propertyValue, embeddableValuedModelPart, recursiveGuard);
        } else if (part instanceof PluralAttributeMapping pluralAttributeMapping) {
            if (propertyValue instanceof Map) {
                deepInflateMap((Map<?, ?>) propertyValue, pluralAttributeMapping, recursiveGuard);
            } else if (propertyValue instanceof Collection) {
                deepInflateCollection((Collection<?>) propertyValue, pluralAttributeMapping, recursiveGuard);
            } else if (propertyValue instanceof Object[]) {
                deepInflateArray((Object[]) propertyValue, pluralAttributeMapping, recursiveGuard);
            } else if (propertyValue.getClass().isArray()) {
                // Arrays of primitives are never lazy and contain nothing to hydrate.
            } else {
                throw new UnsupportedOperationException("Unsupported collection type %s for %s.".formatted(
                        propertyValue.getClass().getSimpleName(), part.getNavigableRole().getFullPath()));
            }
        }
    }

    /**
     * Deep inflate an entity.
     */
    private void deepInflateEntity(
            Object entity, EntityValuedModelPart part, IdentitySet<Object> recursiveGuard) {
        if (guard(entity, part, recursiveGuard)) {
            return;
        }
        Hibernate.initialize(entity);

        var target = Hibernate.unproxy(entity);
        var descriptor = part.getEntityMappingType();
        // Start with the identifier. It is not part of the attribute mappings.
        deepInflateIdentifier(target, descriptor, recursiveGuard);
        // Continue with the entity itself.
        descriptor.getAttributeMappings().forEach(attributeMapping -> {
            var propertyValue = attributeMapping.getValue(target);
            deepInflateProperty(propertyValue, attributeMapping, recursiveGuard);
        });
        // Continue with subclasses.
        if (descriptor.hasSubclasses()) {
            descriptor.getSubMappingTypes().stream()
                .filter(subclassDescriptor -> subclassDescriptor.getJavaType().getJavaTypeClass().isInstance(target))
                .forEach(subclassDescriptor -> subclassDescriptor.getAttributeMappings().forEach(attributeMapping -> {
                    var propertyValue = attributeMapping.getValue(target);
                    deepInflateProperty(propertyValue, attributeMapping, recursiveGuard);
                }));
        }
    }

    /**
     * Deep inflate the associations that are part of a composite identifier of an entity.
     */
    private void deepInflateIdentifier(
            Object entity, EntityMappingType descriptor, IdentitySet<Object> recursiveGuard) {
        if (!(descriptor.getIdentifierMapping() instanceof CompositeIdentifierMapping identifierMapping)) {
            // Simple identifiers contain no associations.
            return;
        }

        // @IdClass: The identifier attributes are attributes of the entity itself.
        // @EmbeddedId: The identifier attributes are attributes of the identifier.
        var container = identifierMapping instanceof VirtualModelPart ?
                entity : identifierMapping.getIdentifier(entity);
        if (container == null) {
            return;
        }

        identifierMapping.getEmbeddableTypeDescriptor().getAttributeMappings().forEach(attributeMapping -> {
            var propertyValue = attributeMapping.getValue(container);
            deepInflateProperty(propertyValue, attributeMapping, recursiveGuard);
        });
    }

    /**
     * Deep inflate the target of an any association ({@code @Any}, element of {@code @ManyToAny}).
     * The mapping does not know the entity type of the target, so it is derived from the target itself.
     */
    private void deepInflateAny(
            Object entity, DiscriminatedAssociationModelPart part, IdentitySet<Object> recursiveGuard) {
        if (guard(entity, part)) {
            return;
        }

        deepInflateInitialEntity(entity, recursiveGuard);
    }

    /**
     * Deep inflate an embedded entity.
     */
    private void deepInflateEmbedded(
            Object embeddable, EmbeddableValuedModelPart part, IdentitySet<Object> recursiveGuard) {
        if (guard(embeddable, part, recursiveGuard)) {
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
        if (guard(map, part, recursiveGuard)) {
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
        if (guard(collection, part, recursiveGuard)) {
            return;
        }
        Hibernate.initialize(collection);

        var elementType = part.getElementDescriptor();
        collection.forEach(element ->
                deepInflateProperty(element, elementType, recursiveGuard));
    }

    /**
     * Deep inflate the elements of an array.
     * Arrays themselves are never lazy.
     */
    private void deepInflateArray(
            Object[] array, PluralAttributeMapping part, IdentitySet<Object> recursiveGuard) {
        if (guard(array, part, recursiveGuard)) {
            return;
        }

        var elementType = part.getElementDescriptor();
        for (var element : array) {
            deepInflateProperty(element, elementType, recursiveGuard);
        }
    }

    /**
     * Guard against values that must not be inflated.
     *
     * @param value
     *            The value. May be {@code null}.
     * @param part
     *            The model part of the value.
     * @return {@code true} if the value is {@code null} or excluded.
     */
    private boolean guard(Object value, ModelPart part) {
        return value == null || excludes.contains(part.getNavigableRole());
    }

    /**
     * Guard against values that must not be inflated.
     * Marks the value as visited, if it has to be inflated.
     *
     * @param value
     *            The value. May be {@code null}.
     * @param part
     *            The model part of the value.
     * @param recursiveGuard
     *            A guard to avoid endless recursion.
     * @return {@code true} if the value is {@code null}, excluded or has already been visited.
     */
    private boolean guard(Object value, ModelPart part, IdentitySet<Object> recursiveGuard) {
        // Check the excludes first: An excluded value must not be marked as visited,
        // because it may be reachable via another path that is not excluded.
        return guard(value, part) || !recursiveGuard.add(value);
    }
}
