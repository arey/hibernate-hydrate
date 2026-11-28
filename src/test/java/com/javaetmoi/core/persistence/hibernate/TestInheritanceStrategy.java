package com.javaetmoi.core.persistence.hibernate;

import com.javaetmoi.core.persistence.hibernate.inheritance.InheritanceHolder;
import com.javaetmoi.core.persistence.hibernate.inheritance.JoinedSub;
import com.javaetmoi.core.persistence.hibernate.inheritance.MappedSub;
import com.javaetmoi.core.persistence.hibernate.inheritance.TablePerClassSub;
import com.javaetmoi.core.persistence.hibernate.mapping.Target;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the hydration of the attributes of subclasses and superclasses for the inheritance strategies.
 * The single table strategy is tested by {@link TestSingleTableInheritance}.
 */
class TestInheritanceStrategy extends AbstractTest {
    @BeforeEach
    void setUpEntities() {
        var joined = new Target(10);
        var tablePerClass = new Target(20);
        var mappedSuperclass = new Target(30);
        var joinedSub = new JoinedSub(1, joined);
        var tablePerClassSub = new TablePerClassSub(1, tablePerClass);
        persist(joined, tablePerClass, mappedSuperclass, joinedSub, tablePerClassSub,
                new InheritanceHolder(1, joinedSub, tablePerClassSub),
                new MappedSub(1, mappedSuperclass));
    }

    /**
     * Without hydration the associations are not initialized.
     */
    @Test
    void notHydrated() {
        var dbHolder = findEntity(InheritanceHolder.class, 1);
        var dbMappedSub = findEntity(MappedSub.class, 1);

        assertThat(Hibernate.isInitialized(dbHolder.getJoined())).isFalse();
        assertThat(Hibernate.isInitialized(dbHolder.getTablePerClass())).isFalse();
        assertThat(Hibernate.isInitialized(dbMappedSub.getTarget())).isFalse();
    }

    @Test
    void joined() {
        var dbSub = findDeepHydratedJoinedSub(hydrator);

        assertThat(dbSub.getTarget().getTags()).containsExactly("tag10");
    }

    /**
     * Attributes of a subclass are excluded via the subclass.
     */
    @Test
    void joined_withExclude() {
        var dbSub = findDeepHydratedJoinedSub(hydrator.withExclude(JoinedSub.class, "target"));

        assertThat(Hibernate.isInitialized(dbSub.getTarget())).isFalse();
    }

    @Test
    void tablePerClass() {
        var dbSub = findDeepHydratedTablePerClassSub(hydrator);

        assertThat(dbSub.getTarget().getTags()).containsExactly("tag20");
    }

    /**
     * Attributes of a subclass are excluded via the subclass.
     */
    @Test
    void tablePerClass_withExclude() {
        var dbSub = findDeepHydratedTablePerClassSub(hydrator.withExclude(TablePerClassSub.class, "target"));

        assertThat(Hibernate.isInitialized(dbSub.getTarget())).isFalse();
    }

    @Test
    void mappedSuperclass() {
        var dbSub = findDeepHydratedEntity(MappedSub.class, 1);

        assertThat(dbSub.getTarget().getTags()).containsExactly("tag30");
    }

    /**
     * Attributes of a mapped superclass are excluded via the entity.
     */
    @Test
    void mappedSuperclass_withExclude() {
        var dbSub = findDeepHydratedEntity(
                hydrator.withExclude(MappedSub.class, "target"), MappedSub.class, 1);

        assertThat(Hibernate.isInitialized(dbSub.getTarget())).isFalse();
    }

    private JoinedSub findDeepHydratedJoinedSub(Hydrator hydrator) {
        var dbHolder = findDeepHydratedEntity(hydrator, InheritanceHolder.class, 1);
        return (JoinedSub) Hibernate.unproxy(dbHolder.getJoined());
    }

    private TablePerClassSub findDeepHydratedTablePerClassSub(Hydrator hydrator) {
        var dbHolder = findDeepHydratedEntity(hydrator, InheritanceHolder.class, 1);
        return (TablePerClassSub) Hibernate.unproxy(dbHolder.getTablePerClass());
    }
}
