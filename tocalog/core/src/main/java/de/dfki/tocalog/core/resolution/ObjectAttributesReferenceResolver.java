package de.dfki.tocalog.core.resolution;

import de.dfki.tocalog.core.ReferenceDistribution;
import de.dfki.tocalog.core.ReferenceResolver;
import de.dfki.tocalog.core.WeightedReferenceResolver;
import de.dfki.tocalog.kb.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ObjectAttributesReferenceResolver implements ReferenceResolver {


    private Map<Attribute, AttributeValue> attributes = new HashMap<>();
    private KnowledgeMap objectMap;


    public ObjectAttributesReferenceResolver(KnowledgeBase knowledgeBase, Type objectType) {
        objectMap = knowledgeBase.getKnowledgeMap(objectType);
    }

    public void setAttributes(Map<Attribute, AttributeValue> attributes) {
        this.attributes = attributes;
    }



    @Override
    public ReferenceDistribution getReferences() {
        ReferenceDistribution objectDistribution = new ReferenceDistribution();

        if(attributes.isEmpty()) {
            for(Entity e: objectMap.getAll()) {
                objectDistribution.getConfidences().put(e.get(Ontology.id).get(), 1.0/objectMap.getAll().size());
            }
            return objectDistribution;
        }

        double matchedCount;
        for(Entity object: objectMap.getAll()) {
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
            for(Entity e: objectMap.getAll()) {
                objectDistribution.getConfidences().put(e.get(Ontology.id).get(), 1.0/objectMap.getAll().size());
            }
        }


        return objectDistribution.rescaleDistribution();

    }
}
