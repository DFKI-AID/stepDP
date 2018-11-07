package de.dfki.tocalog.core;

import de.dfki.tocalog.kb.Type;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * one entry should always be none / unknown / ... to model low confidence for other values (sum has to be 1.0)//
 */
public class ReferenceDistribution {
    private Map<String, Double> confidences = new HashMap<>();

    protected void assertState() {
        for (Map.Entry<String, Double> c : confidences.entrySet()) {
            if (c.getValue() < 0) {
                throw new IllegalStateException("confidence of FocusDistribution can't be < 0. key was " + c.getKey());
            }
            if (c.getValue() > 1) {
                throw new IllegalStateException("confidence of FocusDistribution can't be > 1. key was " + c.getKey());
            }
        }


        double sum = confidences.values().stream().reduce(1.0, (d1, d2) -> d1 + d2);
        if (sum != 1.0) {
            throw new IllegalStateException("the accumulated sum of the confidences of a FocusDistribution has to be 1.0");
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
}
