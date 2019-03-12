package de.dfki.step.core;

import java.util.Optional;

/**
 */
public interface DialogComponent {
    Optional<DialogFunction> process(Inputs inputs);
}
