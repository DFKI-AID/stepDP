package de.dfki.step.resolution;

public class ReverseReferenceResolver implements ReferenceResolver {

    private ReferenceResolver reverseResolver;

    public ReverseReferenceResolver(ReferenceResolver reverseResolver) {
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
