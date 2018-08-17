package de.dfki.tocalog.model;

public class Confidence {
    private double confidence;

    public Confidence(double confidence) {
        this.confidence = confidence;
        if(confidence < 0 || confidence > 1) {
            throw new IllegalArgumentException("confidence has to be in the range [0:1]");
        }
    }

    public double getConfidence() {
        return confidence;
    }
}
