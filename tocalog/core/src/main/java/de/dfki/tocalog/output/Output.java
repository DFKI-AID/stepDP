package de.dfki.tocalog.output;

import de.dfki.tocalog.model.Mode;

/**
 * unimodal output e.g. Speech("hello world").
 * modality is implicit through the inheritance tree
 */
public interface Output {
    Mode getMode();
}
