package a;

import de.dfki.tocalog.input.Input;
import de.dfki.tocalog.core.Confidence;

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
