package de.dfki.step.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 */
public class LHypothesisProducer implements HypothesisProducer {
    private final List<HypothesisProducer> producers = new ArrayList<>();

    public LHypothesisProducer(HypothesisProducer... producers) {
        this(Set.of(producers));
    }

    public LHypothesisProducer(Collection<HypothesisProducer> producers) {
        this.producers.addAll(producers);
    }


    @Override
    public List<Hypothesis> process(Inputs inputs) {
        List<Hypothesis> hypotheses = new ArrayList<>();
        this.producers.forEach(p -> hypotheses.addAll(p.process(inputs)));
        return hypotheses;
    }
}
