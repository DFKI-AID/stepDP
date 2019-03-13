package de.dfki.step.dialog;

/**
 *
 */
public interface Behavior {
    /**
     * Initialize the behavior by e.g. creating rules.
     * @param dialog
     */
    void init(Dialog dialog);

    /**
     * Deinitialize the behavior by removing rules added through {@link #init(Dialog)}
     */
    void deinit();

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
