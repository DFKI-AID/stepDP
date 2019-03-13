package de.dfki.step.resolution;

import java.util.*;

/**
 * one entry should always be none / unknown / ... to model low confidence for other values (sum has to be 1.0)//
 * TODO maybe add a function to check whether the ReferenceDistribution is 'available' e.g. no sensor available
 */
public class ReferenceDistribution {
    private Map<String, Double> confidences = new HashMap<>();
    private final double THRESHOLD = .0001;

    protected void assertState() {
        for (Map.Entry<String, Double> c : confidences.entrySet()) {
            if (c.getValue() < 0) {
                throw new IllegalStateException("confidence of the ReferenceDistribution can't be < 0. key was " + c.getKey());
            }
            if (c.getValue() > 1) {
                throw new IllegalStateException("confidence of the ReferenceDistribution can't be > 1. key was " + c.getKey());
            }
        }

        if(!confidences.values().isEmpty()) {
            double sum = confidences.values().stream().reduce((d1, d2) -> d1 + d2).get();
            if (Math.abs(1.0 - sum) > THRESHOLD) {
                throw new IllegalStateException("the accumulated sum of the confidences of a Reference Distribution has to be 1.0");
            }
        }
    }

    public Map<String, Double> getConfidences() {
        return confidences;
    }

    public ReferenceDistribution add(ReferenceDistribution referenceDistribution) {
        Set<String> keys = new HashSet<>();
        keys.addAll(this.confidences.keySet());
        keys.addAll(referenceDistribution.confidences.keySet());

        ReferenceDistribution result = new ReferenceDistribution();
        for(String key : keys) {
            double confidence = 0.0;
            confidence += this.confidences.containsKey(key) ? this.confidences.get(key) : 0.0;
            confidence += referenceDistribution.confidences.containsKey(key) ? referenceDistribution.confidences.get(key) : 0.0;
            result.confidences.put(key, confidence);
        }

        return result;
    }

    public ReferenceDistribution mul(double weight) {
        ReferenceDistribution rd = new ReferenceDistribution();
        rd.confidences.putAll(this.confidences);
        for (Map.Entry<String, Double> entry : confidences.entrySet()) {
            rd.confidences.put(entry.getKey(), entry.getValue() * weight);
        }
        return rd;
    }

    public final static ReferenceDistribution Empty = new ReferenceDistribution(); //TODO assertState fails for this

    static {
//        Empty.confidences.put("none", 1.0);
    }

    public ReferenceDistribution rescaleDistribution() {
        Optional<Double> totalCount = this.getConfidences().values().stream().reduce((d1, d2) -> d1 + d2);
        if (!totalCount.isPresent()) {
            return this;
        }
        for (String id: this.getConfidences().keySet()) {
            this.getConfidences().put(id, this.getConfidences().get(id) / totalCount.get());
        }
        return this;
    }

    public ReferenceDistribution revertDistribution() {
        for(String id: this.getConfidences().keySet()) {
            this.getConfidences().put(id, 1.0- this.getConfidences().get(id));
        }
        this.rescaleDistribution();
        return this;
    }

    @Override
    public String toString() {
        return "ReferenceDistribution{" +
                "confidences=" + confidences +
                '}';
    }
}
