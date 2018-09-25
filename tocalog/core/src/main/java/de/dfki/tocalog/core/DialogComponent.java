package de.dfki.tocalog.core;

import java.util.Collection;

/**
 */
public interface DialogComponent extends EventEngine.Listener {
    /**
     * Mark parts of the hypothesis tree that the component want to consume
     * @param ht
     */
    void examine(Hypothesis ht);

    void process(Hypothesis ht);

    Collection<Class<HypothesisProducer>> getRelevantHypoProducers();
}
