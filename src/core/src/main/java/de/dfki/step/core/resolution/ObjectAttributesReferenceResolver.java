package de.dfki.step.core.resolution;

import de.dfki.step.core.ReferenceDistribution;
import de.dfki.step.core.ReferenceResolver;
import de.dfki.step.kb.*;

import java.util.HashMap;
import java.util.Map;

public class ObjectAttributesReferenceResolver implements ReferenceResolver {


    private Map<Attribute, AttributeValue> attributes = new HashMap<>();
    private KnowledgeMap objectMap = new KnowledgeMap();
    private String type = "";
    private KnowledgeBase kb;


    public ObjectAttributesReferenceResolver(KnowledgeBase knowledgeBase) {
        this.kb = knowledgeBase;
    }

    public void setAttributes(Map<Attribute, AttributeValue> attributes) {
        this.attributes = attributes;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public ReferenceDistribution getReferences() {
        objectMap = kb.getKnowledgeMap(type);
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
