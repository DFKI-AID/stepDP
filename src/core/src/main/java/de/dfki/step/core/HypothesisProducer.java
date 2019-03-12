package de.dfki.step.core;

import java.util.List;

/**
 */
public interface HypothesisProducer {
    List<Hypothesis> process(Inputs inputs);

    //SensorSetting
}
