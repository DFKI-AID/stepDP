package de.dfki.tocalog.core;

import java.util.*;

/**
 */
public class Hypotheses {
    private Map<String, Hypothesis> hypotheses = new HashMap<>();

    public void add(Hypothesis hypothesis) {
        hypotheses.put(hypothesis.getId(), hypothesis);
    }

    public Collection<Hypothesis> getHypotheses() {
        return hypotheses.values();
    }

    public Optional<Hypothesis> getHypothesis(String id) {
        return Optional.ofNullable(hypotheses.get(id));
    }
}
