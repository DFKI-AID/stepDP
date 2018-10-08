package de.dfki.tocalog.core;

import de.dfki.tocalog.input.Input;
import de.dfki.tocalog.model.Confidence;
import de.dfki.tocalog.model.Entity;

import java.util.List;

/**
 */
public interface Slot {
    Entity consumes(Input input);
    void consume(Input input);
    boolean isFilled();
    //TODO optional?

    interface Entity {
        Confidence getConfidence();
        Object getObject();
        Slot getSlot();
    }
}
