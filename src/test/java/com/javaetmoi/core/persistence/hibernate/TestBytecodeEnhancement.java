package com.javaetmoi.core.persistence.hibernate;

import com.javaetmoi.core.persistence.hibernate.enhanced.EnhancedInverse;
import com.javaetmoi.core.persistence.hibernate.enhanced.EnhancedOwner;
import org.hibernate.Hibernate;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.BootstrapServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.engine.spi.PersistentAttributeInterceptable;
import org.hibernate.testing.bytecode.enhancement.extension.BytecodeEnhanced;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.javaetmoi.core.persistence.hibernate.Hydrator.hydrator;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the hydration of bytecode enhanced entities.
 * Their lazy basic attributes and the inverse sides of their lazy one-to-one associations are loaded on access,
 * instead of being loaded together with the entity.
 */
@BytecodeEnhanced
class TestBytecodeEnhancement {
    private SessionFactory sessionFactory;

    private Hydrator hydrator;

    @BeforeEach
    void setUpDatabase() {
        // The entities have to be loaded by the class loader of the test, because that one enhances them.
        var bootstrapRegistry = new BootstrapServiceRegistryBuilder()
                .applyClassLoader(getClass().getClassLoader())
                .build();
        sessionFactory = new Configuration(bootstrapRegistry)
                .addAnnotatedClass(EnhancedOwner.class)
                .addAnnotatedClass(EnhancedInverse.class)
                .setProperty("hibernate.connection.url", "jdbc:h2:mem:hibernate-hydrate-enhanced;DB_CLOSE_DELAY=-1")
                .setProperty("hibernate.hbm2ddl.auto", "create-drop")
                .buildSessionFactory();
        hydrator = hydrator(sessionFactory);

        sessionFactory.inTransaction(session -> {
            var owner = new EnhancedOwner(1, "content");
            session.persist(owner);
            session.persist(new EnhancedInverse(10, owner, List.of("inverse")));
        });
    }

    @AfterEach
    void tearDownDatabase() {
        sessionFactory.close();
    }

    /**
     * Ensure that the entities are enhanced at all. Otherwise, the other tests would test nothing.
     */
    @Test
    void enhanced() {
        assertThat(PersistentAttributeInterceptable.class).isAssignableFrom(EnhancedOwner.class);

        var dbOwner = sessionFactory.fromTransaction(session -> session.find(EnhancedOwner.class, 1));

        assertThat(Hibernate.isPropertyInitialized(dbOwner, "content")).isFalse();
        assertThat(Hibernate.isPropertyInitialized(dbOwner, "inverse")).isFalse();
    }

    @Test
    void lazyBasic() {
        var dbOwner = findDeepHydratedOwner(hydrator);

        assertThat(dbOwner.getContent()).isEqualTo("content");
    }

    @Test
    void lazyBasic_withExclude() {
        var dbOwner = findDeepHydratedOwner(
                hydrator.withExclude(EnhancedOwner.class, "content"));

        assertThat(Hibernate.isPropertyInitialized(dbOwner, "content")).isFalse();
        assertThat(dbOwner.getInverse().getTags()).containsExactly("inverse");
    }

    @Test
    void inverseOneToOne() {
        var dbOwner = findDeepHydratedOwner(hydrator);

        assertThat(dbOwner.getInverse().getId()).isEqualTo(10);
        assertThat(dbOwner.getInverse().getOwner()).isSameAs(dbOwner);
        assertThat(dbOwner.getInverse().getTags()).containsExactly("inverse");
    }

    @Test
    void inverseOneToOne_withExclude() {
        var dbOwner = findDeepHydratedOwner(
                hydrator.withExclude(EnhancedOwner.class, "inverse"));

        assertThat(Hibernate.isPropertyInitialized(dbOwner, "inverse")).isFalse();
        assertThat(dbOwner.getContent()).isEqualTo("content");
    }

    /**
     * Hibernate loads all attributes of a lazy group at once,
     * so an excluded attribute is loaded together with the other attributes of its lazy group.
     */
    @Test
    void lazyGroup_withPartialExclude() {
        var dbOwner = findDeepHydratedOwner(
                hydrator.withExclude(EnhancedOwner.class, "summary"));

        assertThat(Hibernate.isPropertyInitialized(dbOwner, "summary")).isTrue();
        assertThat(Hibernate.isPropertyInitialized(dbOwner, "details")).isTrue();
    }

    /**
     * A lazy group stays unloaded, if all of its attributes are excluded.
     */
    @Test
    void lazyGroup_withExclude() {
        var dbOwner = findDeepHydratedOwner(
                hydrator.withExclude(EnhancedOwner.class, "summary")
                        .withExclude(EnhancedOwner.class, "details"));

        assertThat(Hibernate.isPropertyInitialized(dbOwner, "summary")).isFalse();
        assertThat(Hibernate.isPropertyInitialized(dbOwner, "details")).isFalse();
    }

    private EnhancedOwner findDeepHydratedOwner(Hydrator hydrator) {
        return sessionFactory.fromTransaction(session ->
                hydrator.deepHydrate(session.find(EnhancedOwner.class, 1)));
    }
}
