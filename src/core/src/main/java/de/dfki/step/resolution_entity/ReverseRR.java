package de.dfki.step.resolution_entity;


public class ReverseRR implements de.dfki.step.resolution_entity.ReferenceResolver {

    private de.dfki.step.resolution_entity.ReferenceResolver reverseResolver;

    public ReverseRR(de.dfki.step.resolution_entity.ReferenceResolver reverseResolver) {
        this.reverseResolver = reverseResolver;
    }

    @Override
    public de.dfki.step.resolution_entity.ReferenceDistribution getReferences() {
        de.dfki.step.resolution_entity.ReferenceDistribution reverseDistribution = reverseResolver.getReferences();

        for(String id: reverseDistribution.getConfidences().keySet()) {
            reverseDistribution.getConfidences().put(id, 1.0 - reverseDistribution.getConfidences().get(id));
        }

        reverseDistribution.rescaleDistribution();
        return reverseDistribution;
    }
}
