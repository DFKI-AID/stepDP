package de.dfki.tocalog.core;

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
