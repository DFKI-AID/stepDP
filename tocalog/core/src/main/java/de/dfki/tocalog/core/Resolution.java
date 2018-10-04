package de.dfki.tocalog.core;

/**
 * TODO: maybe: individual functions, maybe lower level e.g. working on speech input?
 */
public interface Resolution {
    Hypothesis process(Hypothesis hypothesis);
}
