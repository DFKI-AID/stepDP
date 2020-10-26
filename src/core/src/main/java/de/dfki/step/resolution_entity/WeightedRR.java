package de.dfki.step.resolution_entity;


import java.util.ArrayList;
import java.util.List;

/**
 */
public class WeightedRR implements de.dfki.step.resolution_entity.ReferenceResolver {
    private List<Pair> resolvers = new ArrayList<>();

    public void addResolver(de.dfki.step.resolution_entity.ReferenceResolver rr, double weight) {
        this.resolvers.add(new Pair(rr, weight));
    }

    @Override
    public de.dfki.step.resolution_entity.ReferenceDistribution getReferences() {
        de.dfki.step.resolution_entity.ReferenceDistribution result = de.dfki.step.resolution_entity.ReferenceDistribution.Empty;
        for (Pair resolver : resolvers) {
            double weight = resolver.weight;
            de.dfki.step.resolution_entity.ReferenceDistribution rd = resolver.rr.getReferences();
            System.out.println(resolver.rr.toString() + ": " + rd.toString());
            rd = rd.mul(weight);
            result = result.add(rd);
        }

        result.assertState();
        return result;
    }

    //TODO assert functions for weight

    private static class Pair {
        public final de.dfki.step.resolution_entity.ReferenceResolver rr;
        public final double weight;

        private Pair(de.dfki.step.resolution_entity.ReferenceResolver rr, double weight) {
            this.rr = rr;
            this.weight = weight;
        }
    }
}
