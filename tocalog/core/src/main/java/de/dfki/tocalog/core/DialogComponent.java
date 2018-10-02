package de.dfki.tocalog.core;

import java.util.Collection;
import java.util.Optional;

/**
 */
public interface DialogComponent {
    Optional<DialogFunction> process(Hypothesis h);

    Collection<Class<HypothesisProducer>> getRelevantHypoProducers();
}
