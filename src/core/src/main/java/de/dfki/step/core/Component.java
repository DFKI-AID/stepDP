package de.dfki.step.core;

import de.dfki.step.dialog.Dialog;

/**
 * As part of the dialog application, a component can be anything that has to initialize,
 * regularly update or manages data that depends on the dialog state.
 */
public interface Component {
    /**
     * Initialize the behavior by e.g. creating rules.
     * @param cm
     */
    void init(ComponentManager cm);

    /**
     * Deinitialize the behavior by removing rules added through {@link #init(ComponentManager)}
     */
    void deinit();

    default void beforeUpdate() {
    }

    /**
     * Called once each iteration
     */
    void update();

    default void afterUpdate() {
    }

    /**
     * Creates a snapshot of the current state which is sufficient reload the same state
     * through (@link {@link #loadSnapshot(Object)}.
     * @return The snapshot of the current state.
     */
    Object createSnapshot();

    /**
     * Reloads the state of the behavior defined by the given snapshot. See {@link #createSnapshot()}.
     * @param snapshot
     * @throws IllegalArgumentException on invalid snapshot.
     */
    void loadSnapshot(Object snapshot);
}
