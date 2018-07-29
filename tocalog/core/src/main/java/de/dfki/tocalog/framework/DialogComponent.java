package de.dfki.tocalog.framework;

/**
 */
public interface DialogComponent extends EventEngine.Listener {
    void init(ProjectManager dialogCore);
}
