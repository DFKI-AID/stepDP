package de.dfki.tocalog.core;

import java.util.List;

/**
 */
public interface HypothesisProducer {
    List<Hypothesis> process(Inputs inputs);

    //SensorSetting
}
