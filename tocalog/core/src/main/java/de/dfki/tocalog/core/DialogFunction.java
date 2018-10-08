package de.dfki.tocalog.core;

import de.dfki.tocalog.input.Input;
import de.dfki.tocalog.model.Confidence;

import java.util.Collection;
import java.util.Optional;

/**
 */
public interface DialogFunction extends Runnable {
    default Optional<Confidence> getConfidence() { //TODO maybe
        return Optional.empty();
    }

    default Optional<Confidence> getPriorty() { return Optional.empty(); }

    /**
     * @return All inputs that will be / were consumed by this dialog function
     */
    Collection<Input> consumedInputs();

    Object getOrigin();
}
