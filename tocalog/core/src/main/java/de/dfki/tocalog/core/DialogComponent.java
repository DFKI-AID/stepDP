package de.dfki.tocalog.core;

import de.dfki.tocalog.core.ProjectManager;
import de.dfki.tocalog.core.EventEngine;

/**
 */
public interface DialogComponent extends EventEngine.Listener {
    void init(ProjectManager dialogCore);
}
