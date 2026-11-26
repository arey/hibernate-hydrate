package com.javaetmoi.core.persistence.hibernate;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.Set;

import static java.util.Collections.newSetFromMap;

/**
 * State of a single hydration.
 */
final class Hydration {
    /**
     * Values that have already been visited.
     */
    private final Set<Object> visited;

    /**
     * Inflations that still have to be done.
     */
    private final Deque<Runnable> deferred = new ArrayDeque<>();

    /**
     * Constructor.
     *
     * @param capacity Expected number of entities to hydrate.
     */
    Hydration(int capacity) {
        this.visited = newSetFromMap(new IdentityHashMap<>(capacity));
    }

    /**
     * Mark the value as visited.
     *
     * @return {@code true} if the value has not been visited before.
     */
    boolean visit(Object value) {
        return visited.add(value);
    }

    /**
     * Do the inflation later on.
     */
    void defer(Runnable inflation) {
        deferred.add(inflation);
    }

    /**
     * Do all deferred inflations, including those that are deferred meanwhile.
     */
    void run() {
        for (var inflation = deferred.poll(); inflation != null; inflation = deferred.poll()) {
            inflation.run();
        }
    }
}
