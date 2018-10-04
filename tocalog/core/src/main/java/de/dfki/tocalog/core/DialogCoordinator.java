package de.dfki.tocalog.core;

import java.util.List;

/**
 */
public interface DialogCoordinator {
    List<DialogFunction> coordinate(List<DialogFunction> dialogFunctions);
}
