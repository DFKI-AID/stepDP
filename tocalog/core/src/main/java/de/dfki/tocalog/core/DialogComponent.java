package de.dfki.tocalog.core;

import java.util.Optional;

/**
 */
public interface DialogComponent {
    Optional<DialogFunction> process(Inputs inputs);
}
