package de.dfki.tocalog.output;

import de.dfki.tocalog.core.Mode;

/**
 * unimodal output e.g. Speech("hello world").
 * mode is implicit through the inheritance tree
 */
public interface Output {
    Mode getMode();
}
