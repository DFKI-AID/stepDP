package de.dfki.tocalog.core;

import de.dfki.tocalog.core.Hypothesis;
import de.dfki.tocalog.core.Inputs;
import de.dfki.tocalog.input.Input;

import java.util.List;

public interface HypothesisProcessor {
    Hypothesis process(Input input, Hypothesis hypothesis);

}
