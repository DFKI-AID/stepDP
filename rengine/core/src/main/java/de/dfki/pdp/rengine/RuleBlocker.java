package de.dfki.pdp.rengine;

/**
 * Functional interface that is used to to determine if a rule is currently disabled.
 */
public interface RuleBlocker {
    boolean isBlocked();
}
