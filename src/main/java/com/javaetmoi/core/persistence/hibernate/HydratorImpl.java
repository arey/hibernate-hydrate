package com.javaetmoi.core.persistence.hibernate;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.hibernate.Hibernate;
import org.hibernate.engine.spi.SessionFactoryImplementor;
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
        var entityDescriptor = mappingMetamodel.findEntityDescriptor(entityClass);
        if (entityDescriptor == null) {
            throw new IllegalArgumentException("The class %s is no entity.".formatted(
                    entityClass.getName()));
        }
        var attributeMapping = findAttributeMapping(entityDescriptor, attribute);
        if (attributeMapping == null) {
            throw new IllegalArgumentException("The attribute %s does not exist at the entity %s.".formatted(
                    attribute, entityDescriptor.getEntityName()));
        }
        if (!isHydratable(attributeMapping)) {
            throw new IllegalArgumentException("The attribute %s of the entity %s is not hydrated, so it cannot be excluded.".formatted(
                    attribute, entityDescriptor.getEntityName()));
        }

        var newExcludes = new HashSet<>(this.excludes);
        newExcludes.add(attributeMapping.getNavigableRole());

        return new HydratorImpl(mappingMetamodel, newExcludes);
    }

    /**
     * Find the attribute of the entity.
     *
     * @param entityDescriptor
     *            Entity.
     * @param attributePath
     *            Attribute name or a path to an attribute inside of embeddables, e.g. {@code embedded.attribute}.
     * @return the attribute or {@code null} if it does not exist.
     */
    private ModelPart findAttributeMapping(EntityMappingType entityDescriptor, String attributePath) {
        var attributes = attributePath.split("\\.", -1);
        var part = findEntityAttributeMapping(entityDescriptor, attributes[0]);
        for (int i = 1; i < attributes.length; i++) {
            if (!(part instanceof EmbeddableValuedModelPart embeddableValuedModelPart)) {
                // Just embeddables have attributes that can be addressed by a path.
                return null;
            }
            part = embeddableValuedModelPart.getEmbeddableTypeDescriptor().findAttributeMapping(attributes[i]);
        }
        return part;
    }

    /**
     * Find the direct attribute of the entity.
     * Considers the identifier attributes of an entity with a composite identifier too,
     * because they are attributes of the entity, but are not part of its attribute mappings.
     */
    private ModelPart findEntityAttributeMapping(EntityMappingType entityDescriptor, String attribute) {
        var attributeMapping = entityDescriptor.findAttributeMapping(attribute);
        if (attributeMapping != null) {
            return attributeMapping;
        }

        if (!(entityDescriptor.getIdentifierMapping() instanceof CompositeIdentifierMapping identifierMapping)) {
            return null;
        }
        if (identifierMapping instanceof VirtualModelPart) {
            // @IdClass: The identifier attributes are attributes of the entity itself.
            return identifierMapping.getEmbeddableTypeDescriptor().findAttributeMapping(attribute);
        }
        // @EmbeddedId: The identifier is an attribute of the entity.
        return attribute.equals(identifierMapping.getAttributeName()) ? identifierMapping : null;
    }

    @Override
    public <C extends Collection<E>, E> C deepHydrateCollection(C entities) {
        if (entities == null) {
            return null;
        }

        // Reduce resizes for big collections.
        var capacity = max(entities.size(), 32);
        var hydration = new Hydration(capacity);
        entities.forEach(entity -> deepInflateInitialEntity(entity, hydration));
        hydration.run();
        return entities;
    }

    @Override
    public <E> E deepHydrate(E entity) {
        var hydration = new Hydration(32);
        deepInflateInitialEntity(entity, hydration);
        hydration.run();
        return entity;
    }

    /**
     * Populate a lazy-initialized object graph.
     * Entry point for entities whose entity type is not known from the mapping.
     *
     * @param entity
     *            The entity. May be {@code null}. Objects that are no entities are ignored.
     * @param hydration
     *            The state of the hydration.
     */
    private void deepInflateInitialEntity(Object entity, Hydration hydration) {
        if (entity == null) {
            return;
        }

        var entityType = mappingMetamodel.findEntityDescriptor(Hibernate.getClass(entity));
        if (entityType == null) {
            // No entity. Nothing to hydrate.
            return;
        }
        deepInflateEntity(entity, entityType, hydration);
    }

    /**
     * Deep inflate an entity.
     */
    private void deepInflateEntity(
            Object entity, EntityValuedModelPart part, Hydration hydration) {
        if (guard(entity, part, hydration)) {
            return;
        }
        Hibernate.initialize(entity);

        var target = Hibernate.unproxy(entity);
        var descriptor = part.getEntityMappingType();
        // Start with the identifier. It is not part of the attribute mappings.
        deepInflateIdentifier(target, descriptor, hydration);
        // Continue with the entity itself.
        descriptor.getAttributeMappings().forEach(attributeMapping -> {
            var propertyValue = attributeMapping.getValue(target);
            deepInflatePropertyQueued(propertyValue, attributeMapping, hydration);
        });
        // Continue with subclasses.
        if (descriptor.hasSubclasses()) {
            descriptor.getSubMappingTypes().stream()
                .filter(subclassDescriptor -> subclassDescriptor.getJavaType().getJavaTypeClass().isInstance(target))
                .forEach(subclassDescriptor -> subclassDescriptor.getAttributeMappings().forEach(attributeMapping -> {
                    var propertyValue = attributeMapping.getValue(target);
                    deepInflatePropertyQueued(propertyValue, attributeMapping, hydration);
                }));
        }
    }

    /**
     * Deep inflate the associations that are part of a composite identifier of an entity.
     */
    private void deepInflateIdentifier(
            Object entity, EntityMappingType descriptor, Hydration hydration) {
        if (!(descriptor.getIdentifierMapping() instanceof CompositeIdentifierMapping identifierMapping)) {
            // Simple identifiers contain no associations.
            return;
        }

        // @IdClass: The identifier attributes are attributes of the entity itself.
        // @EmbeddedId: The identifier attributes are attributes of the identifier.
        var container = identifierMapping instanceof VirtualModelPart ?
                entity : identifierMapping.getIdentifier(entity);
        if (guard(container, identifierMapping)) {
            return;
        }

        identifierMapping.getEmbeddableTypeDescriptor().getAttributeMappings().forEach(attributeMapping -> {
            var propertyValue = attributeMapping.getValue(container);
            deepInflatePropertyQueued(propertyValue, attributeMapping, hydration);
        });
    }

    /**
     * Deep inflate the target of an any association ({@code @Any}, element of {@code @ManyToAny}).
     * The mapping does not know the entity type of the target, so it is derived from the target itself.
     */
    private void deepInflateAny(
            Object entity, DiscriminatedAssociationModelPart part, Hydration hydration) {
        if (guard(entity, part)) {
            return;
        }

        deepInflateInitialEntity(entity, hydration);
    }

    /**
     * Deep inflate an embedded entity.
     */
    private void deepInflateEmbedded(Object embeddable, EmbeddableValuedModelPart part, Hydration hydration) {
        if (guard(embeddable, part, hydration)) {
            return;
        }

        var descriptor = part.getEmbeddableTypeDescriptor();
        descriptor.getAttributeMappings().forEach(attributeMapping -> {
            var propertyValue = attributeMapping.getValue(embeddable);
            deepInflatePropertyQueued(propertyValue, attributeMapping, hydration);
        });
    }

    /**
     * Deep inflate a map including its keys and values.
     */
    private void deepInflateMap(Map<?, ?> map, PluralAttributeMapping part, Hydration hydration) {
        if (guard(map, part, hydration)) {
            return;
        }
        Hibernate.initialize(map);

        var indexType = part.getIndexDescriptor();
        var elementType = part.getElementDescriptor();
        map.forEach((index, element) -> {
            deepInflatePropertyQueued(index, indexType, hydration);
            deepInflatePropertyQueued(element, elementType, hydration);
        });
    }

    /**
     * Deep inflate a collection including its elements.
     */
    private void deepInflateCollection(Collection<?> collection, PluralAttributeMapping part, Hydration hydration) {
        if (guard(collection, part, hydration)) {
            return;
        }
        Hibernate.initialize(collection);

        var elementType = part.getElementDescriptor();
        collection.forEach(element ->
                deepInflatePropertyQueued(element, elementType, hydration));
    }

    /**
     * Deep inflate the elements of an array.
     * Arrays themselves are never lazy.
     */
    private void deepInflateArray(Object[] array, PluralAttributeMapping part, Hydration hydration) {
        if (guard(array, part, hydration)) {
            return;
        }

        var elementType = part.getElementDescriptor();
        for (var element : array) {
            deepInflatePropertyQueued(element, elementType, hydration);
        }
    }

    /**
     * Deep inflate a property value later on.
     * The hydration is not done by recursion, because deep object graphs would overflow the stack.
     */
    private void deepInflatePropertyQueued(Object propertyValue, ModelPart part, Hydration hydration) {
        if (propertyValue == null || !isHydratable(part)) {
            return;
        }

        hydration.defer(() -> deepInflateProperty(propertyValue, part, hydration));
    }

    /**
     * Is the part hydrated at all? Basic values are not.
     * Has to match the parts handled by {@link #deepInflateProperty(Object, ModelPart, Hydration)}.
     */
    private boolean isHydratable(ModelPart part) {
        return part instanceof EntityValuedModelPart ||
                part instanceof DiscriminatedAssociationModelPart ||
                part instanceof EmbeddableValuedModelPart ||
                part instanceof PluralAttributeMapping;
    }

    /**
     * Deep inflate a property value.
     */
    private void deepInflateProperty(
            Object propertyValue, ModelPart part, Hydration hydration) {
        if (part instanceof EntityValuedModelPart entityValuedModelPart) {
            deepInflateEntity(propertyValue, entityValuedModelPart, hydration);
        } else if (part instanceof DiscriminatedAssociationModelPart discriminatedAssociationModelPart) {
            deepInflateAny(propertyValue, discriminatedAssociationModelPart, hydration);
        } else if (part instanceof EmbeddableValuedModelPart embeddableValuedModelPart) {
            deepInflateEmbedded(propertyValue, embeddableValuedModelPart, hydration);
        } else if (part instanceof PluralAttributeMapping pluralAttributeMapping) {
            if (propertyValue instanceof Map<?, ?> mapValue) {
                deepInflateMap(mapValue, pluralAttributeMapping, hydration);
            } else if (propertyValue instanceof Collection<?> collectionValue) {
                deepInflateCollection(collectionValue, pluralAttributeMapping, hydration);
            } else if (propertyValue instanceof Object[] arrayValue) {
                deepInflateArray(arrayValue, pluralAttributeMapping, hydration);
            } else if (propertyValue.getClass().isArray()) {
                // Arrays of primitives are never lazy and contain nothing to hydrate.
            } else {
                throw new UnsupportedOperationException("Unsupported collection type %s for %s.".formatted(
                        propertyValue.getClass().getSimpleName(), part.getNavigableRole().getFullPath()));
            }
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
     * Marks the value as visited, if it has to be inflated, to avoid endless loops.
     *
     * @param value
     *            The value. May be {@code null}.
     * @param part
     *            The model part of the value.
     * @param hydration
     *            The state of the hydration.
     * @return {@code true} if the value is {@code null}, excluded or has already been visited.
     */
    private boolean guard(Object value, ModelPart part, Hydration hydration) {
        // Check the excludes first: An excluded value must not be marked as visited,
        // because it may be reachable via another path that is not excluded.
        return guard(value, part) || !hydration.visit(value);
    }

}
