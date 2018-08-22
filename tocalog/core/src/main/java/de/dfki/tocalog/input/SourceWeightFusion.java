package de.dfki.tocalog.input;

import de.dfki.tocalog.kb.EKnowledgeMap;
import de.dfki.tocalog.kb.KnowledgeBase;
import de.dfki.tocalog.model.Entity;

import java.util.*;

/**
 * Extracts an entity by assigning different weights to sources.
 * Entities are filtered if
 * [1] they are too old ({@link SourceWeightFusion#setTimeout}),
 * [2] confidence too low ({@link SourceWeightFusion#setMinConfidence}),
 * [3] no weight was assigned to the source ({@link SourceWeightFusion#setWeight})
 * [4] source of entity is unknown
 */
public class SourceWeightFusion<T extends Entity> {
    private final EKnowledgeMap<T> km;
    private KnowledgeBase knowledgeBase;
    private KnowledgeBase.Key<T> key;
    private Map<String, Integer> sourceWeight = new HashMap<>();
    private long timeout = 3000L;
    private double minConfidence = 0.5;

    public SourceWeightFusion(KnowledgeBase knowledgeBase, KnowledgeBase.Key<T> key) {
        this.knowledgeBase = knowledgeBase;
        this.key = key;
        this.km = knowledgeBase.getKnowledgeMap(key);
    }

    public Optional<T> get() {
        long now = System.currentTimeMillis();
        try {
            return km.lock().getStream()
                    .map(e -> e.getValue())
                    .filter(e ->
                            e.getTimestamp().orElse(0l) + timeout > now)
                    .filter(e ->
                            e.getConfidence().orElse(-1.0) > minConfidence)
                    .filter(e ->
                            e.getSource().isPresent())
                    .filter(e ->
                            sourceWeight.containsKey(e.getSource().get()))
                    .sorted(Comparator.comparingInt(
                            (Entity e) ->
                                    sourceWeight.get(e.getSource().get()))
                            .reversed())
                    .findFirst();
        } finally {
            km.unlock();
        }
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
