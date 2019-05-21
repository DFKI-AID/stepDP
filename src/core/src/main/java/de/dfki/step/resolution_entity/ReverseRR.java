package de.dfki.step.resolution_entity;


public class ReverseRR implements ReferenceResolver {

    private ReferenceResolver reverseResolver;

    public ReverseRR(ReferenceResolver reverseResolver) {
        this.reverseResolver = reverseResolver;
    }

    @Override
    public ReferenceDistribution getReferences() {
        ReferenceDistribution reverseDistribution = reverseResolver.getReferences();

        for(String id: reverseDistribution.getConfidences().keySet()) {
            reverseDistribution.getConfidences().put(id, 1.0 - reverseDistribution.getConfidences().get(id));
        }

        reverseDistribution.rescaleDistribution();
        return reverseDistribution;
    }
}
