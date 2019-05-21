package de.dfki.step.resolution_entity;

import de.dfki.step.kb.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;


/* candidate entities get higher confidence the more requested attributes they have */
public class AttributeRR implements ReferenceResolver {


    private Map<Attribute, AttributeValue> attributes = new HashMap<>();
    private Collection<Entity> candidates;


    public AttributeRR(Supplier<Collection<Entity>> candidateSupplier) {
        this.candidates = candidateSupplier.get();
    }

    public void setAttributes(Map<Attribute, AttributeValue> attributes) {
        this.attributes = attributes;
    }



    @Override
    public ReferenceDistribution getReferences() {

        ReferenceDistribution objectDistribution = new ReferenceDistribution();

        if(attributes.isEmpty()) {
            for(Entity e: candidates) {
                objectDistribution.getConfidences().put(e.get(Ontology.id).get(), 1.0/candidates.size());
            }
            return objectDistribution;
        }

        double matchedCount;
        for(Entity object: candidates) {
            matchedCount = 0.0;
            for(Attribute attribute: attributes.keySet()) {
                if(object.get(attribute).isPresent()) {
                    if(attributes.get(attribute).value.equals(object.get(attribute).get())) {
                        matchedCount += 1.0;
                    }
                }
            }
            objectDistribution.getConfidences().put(object.get(Ontology.id).get(), matchedCount);
        }

        if(objectDistribution.getConfidences().isEmpty() || objectDistribution.getConfidences().values().stream().allMatch(d -> d.equals(0.0))) {
            for(Entity e: candidates) {
                objectDistribution.getConfidences().put(e.get(Ontology.id).get(), 1.0/candidates.size());
            }
        }


        return objectDistribution.rescaleDistribution();

    }
}
