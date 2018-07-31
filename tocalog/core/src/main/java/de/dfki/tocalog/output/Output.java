package de.dfki.tocalog.output;

import de.dfki.tocalog.framework.Mode;

/**
 * unimodal output e.g. Speech("hello world").
 * modality is implicit through the inheritance tree
 */
public interface Output {
    Mode getMode();
}
