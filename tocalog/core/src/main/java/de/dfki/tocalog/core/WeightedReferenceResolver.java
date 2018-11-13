package de.dfki.tocalog.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 */
public class WeightedReferenceResolver implements ReferenceResolver {
    private List<Pair> resolvers = new ArrayList<>();

    public void addResolver(ReferenceResolver rr, double weight) {
        this.resolvers.add(new Pair(rr, weight));
    }

    @Override
    public ReferenceDistribution getReferences() {
        ReferenceDistribution result = ReferenceDistribution.Empty;
        for (Pair resolver : resolvers) {
            double weight = resolver.weight;
            ReferenceDistribution rd = resolver.rr.getReferences();
            rd = rd.mul(weight);
            result = result.add(rd);
        }

        result.assertState();
        return result;
    }

    //TODO assert functions for weight

    private static class Pair {
        public final ReferenceResolver rr;
        public final double weight;

        private Pair(ReferenceResolver rr, double weight) {
            this.rr = rr;
            this.weight = weight;
        }
    }
}
