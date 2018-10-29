package de.dfki.tocalog.core;

import de.dfki.tocalog.input.Input;

import java.util.Collection;

/**
 * TODO more meta-info, uses IMP;
 */
public interface DialogFunction extends Runnable {
    default Confidence getConfidence() {
        return new Confidence(0.5); //TODO value
    }

//    default Optional<Confidence> getPriorty() { return Optional.empty(); }

    /**
     * @return All inputs that will be / were consumed by this dialog function
     */
    Collection<Input> consumedInputs();

    /**
     * @return The object which created the DialogFunction e.g. DialogComponent
     */
    Object getOrigin();
}
