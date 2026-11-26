package com.javaetmoi.core.persistence.hibernate;

import com.javaetmoi.core.persistence.hibernate.chain.ChainNode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the hydration of deep object graphs.
 */
class TestDeepGraph extends AbstractTest {
    /**
     * Length of the chain. Long enough to overflow the stack, if the hydration is done by recursion.
     */
    private static final int LENGTH = 10_000;

    /**
     * A long chain of entities must not overflow the stack.
     */
    @Test
    void longChain() {
        doInJPAVoid(entityManager -> {
            ChainNode next = null;
            for (int id = LENGTH; id >= 1; id--) {
                next = new ChainNode(id, next);
                entityManager.persist(next);
            }
        });

        var dbFirst = findDeepHydratedEntity(ChainNode.class, 1);

        // At this step, transaction and session are closed.
        var length = 0;
        for (var dbNode = dbFirst; dbNode != null; dbNode = dbNode.getNext()) {
            length++;
            assertThat(dbNode.getId()).isEqualTo(length);
        }
        assertThat(length).isEqualTo(LENGTH);
    }

    /**
     * A cycle of entities must not end in an endless loop.
     */
    @Test
    void cycle() {
        doInJPAVoid(entityManager -> {
            var first = new ChainNode(1, null);
            var second = new ChainNode(2, first);
            first.setNext(second);
            entityManager.persist(first);
            entityManager.persist(second);
        });

        var dbFirst = findDeepHydratedEntity(ChainNode.class, 1);

        // At this step, transaction and session are closed.
        assertThat(dbFirst.getNext().getId()).isEqualTo(2);
        assertThat(dbFirst.getNext().getNext()).isSameAs(dbFirst);
    }
}
