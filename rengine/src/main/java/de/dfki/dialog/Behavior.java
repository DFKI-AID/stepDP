package de.dfki.dialog;

/**
 *
 */
public interface Behavior {
    void init(Dialog dialog);
    void deinit();
    Object createSnapshot();
    void loadSnapshot(Object snapshot);
}
