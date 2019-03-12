package de.dfki.step.output;

import de.dfki.step.core.Mode;

/**
 * unimodal output e.g. Speech("hello world").
 * mode is implicit through the inheritance tree
 */
public interface Output {
    Mode getMode();
}
