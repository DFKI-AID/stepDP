package de.dfki.step.core;

import java.util.List;

/**
 */
public interface DialogCoordinator {
    /**
     *
     * @param dialogFunctions
     * @return
     */
    List<DialogFunction> coordinate(List<DialogFunction> dialogFunctions);
}
