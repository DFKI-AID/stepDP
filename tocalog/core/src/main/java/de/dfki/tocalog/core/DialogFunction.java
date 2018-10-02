package de.dfki.tocalog.core;

import de.dfki.tocalog.model.Confidence;

import java.util.Optional;

/**
 */
public interface DialogFunction extends Runnable {
    default Optional<Confidence> getConfidence() { //TODO maybe
        return Optional.empty();
    }

    /**
     * should return false, if the Hypothesis is not consumed. e.g. if a request by the system should be outputted
     * @return
     */
    default boolean consumesHypothesis() {
        return true;
    }
}
