package de.dfki.tocalog.core;

import java.util.Optional;

/**
 */
public interface HypothesisProducer {
    Optional<Hypothesis> process(Inputs inputs);

    //SensorSetting
}
