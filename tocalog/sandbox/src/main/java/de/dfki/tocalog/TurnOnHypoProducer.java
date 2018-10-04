package de.dfki.tocalog;

import de.dfki.tocalog.core.Hypothesis;
import de.dfki.tocalog.core.HypothesisProducer;
import de.dfki.tocalog.core.Inputs;
import de.dfki.tocalog.model.Entity;

import java.util.Optional;

/**
 */
public class TurnOnHypoProducer<T extends Entity> implements HypothesisProducer {
    public static final String INTENT = "TurnOn";

    @Override
    public Optional<Hypothesis> process(Inputs inputs) {
        return Optional.empty();
    }

    public String getIntent() {
        return INTENT;
    }
}
