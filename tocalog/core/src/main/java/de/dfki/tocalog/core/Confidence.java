package de.dfki.tocalog.core;

import java.util.Comparator;

//TODO naming (Confidence / Priority / value in [0:1] / ..
public class Confidence implements Comparable<Confidence>{
    private double confidence;

    public Confidence(double confidence) {
        this.confidence = confidence;
        if (confidence < 0 || confidence > 1) {
            throw new IllegalArgumentException("confidence has to be in the range [0:1]");
        }
    }

    public Confidence distance(Confidence other) {
        return new Confidence(Math.abs(confidence - other.confidence));
    }

    public double getConfidence() {
        return confidence;
    }

    public static final Confidence HIGH = new Confidence(0.9);
    public static final Confidence VERY_HIGH = new Confidence(1.0);
//    public static final Confidence UNKNOWN = new Confidence(0);


    @Override
    public int compareTo(Confidence o) {
        return Double.compare(this.getConfidence(), o.getConfidence());
    }

    static CComparator getComparator() {
        return comparator;
    }

    private static final CComparator comparator = new CComparator();
    static class CComparator implements Comparator<Confidence> {
        @Override
        public int compare(Confidence o1, Confidence o2) {
            return Double.compare(o1.getConfidence(), o2.getConfidence());
        }
    }
}
