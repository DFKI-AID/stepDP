package de.dfki.tocalog.input;

import de.dfki.tocalog.core.Ontology;
import de.dfki.tocalog.kb.KnowledgeBase;
import de.dfki.tocalog.kb.KnowledgeMap;

import java.util.*;

/**
 * Extracts an entity by assigning different weights to sources.
 * Entities are filtered if
 * [1] they are too old ({@link SourceWeightFusion#setTimeout}),
 * [2] confidence too low ({@link SourceWeightFusion#setMinConfidence}),
 * [3] no weight was assigned to the source ({@link SourceWeightFusion#setWeight})
 * [4] source of entity is unknown
 */
public class SourceWeightFusion {
    private final KnowledgeMap km;
    private Map<String, Integer> sourceWeight = new HashMap<>();
    private long timeout = 3000L;
    private double minConfidence = 0.5;

    public SourceWeightFusion(KnowledgeMap km) {
        this.km = km;
    }

    public Optional<Ontology.Ent> get() {
        long now = System.currentTimeMillis();
        //TODO performance can be improved if the km offers a stream function
        Collection<Ontology.Ent> qr = km.query(e ->
                (e.get(Ontology.timestamp).orElse(0l) + timeout > now) &&
                        (e.get(Ontology.confidence).orElse(-1.0) > minConfidence) &&
                        (e.get(Ontology.source).isPresent()) &&
                        (sourceWeight.containsKey(e.get(Ontology.source))));
        return qr.stream().sorted(Comparator.comparingInt(
                (Ontology.Ent e) ->
                        sourceWeight.get(e.get(Ontology.source).get()))
                .reversed())
                .findFirst();
    }

    public void setWeight(String source, int priority) {
        sourceWeight.put(source, priority);
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public void setMinConfidence(double minConfidence) {
        if (minConfidence < 0) {
            throw new IllegalArgumentException("min confidence has to be >= 0");
        }
        this.minConfidence = minConfidence;
    }
}
