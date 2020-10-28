package de.dfki.step.resolution;

import de.dfki.step.deprecated.kb.DataEntry;
import de.dfki.step.kb.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;


/* candidate entities get higher confidence the more requested attributes they have */
public class AttributeRR implements ReferenceResolver {


    private Map<String, Object> attributes = new HashMap<>();
    private Collection<DataEntry> candidates;


    public AttributeRR(Supplier<Collection<DataEntry>> candidateSupplier) {
        this.candidates = candidateSupplier.get();
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }



    @Override
    public ReferenceDistribution getReferences() {

        ReferenceDistribution objectDistribution = new ReferenceDistribution();

        if(attributes.isEmpty()) {
            for(DataEntry e: candidates) {
                objectDistribution.getConfidences().put(e.getId(), 1.0/candidates.size());
            }
            return objectDistribution;
        }

        double matchedCount;
        for(DataEntry object: candidates) {
            matchedCount = 0.0;
            for(String attribute: attributes.keySet()) {
                if(object.get(attribute).isPresent()) {
                    if(attributes.get(attribute).equals(object.get(attribute).get())) {
                        matchedCount += 1.0;
                    }else if(object.get(attribute).get() instanceof Collection && ((Collection) object.get(attribute).get()).contains(attributes.get(attribute))) {
                        matchedCount += 1.0;
                    }
                }
            }
            objectDistribution.getConfidences().put(object.getId(), matchedCount);
        }

        if(objectDistribution.getConfidences().isEmpty() || objectDistribution.getConfidences().values().stream().allMatch(d -> d.equals(0.0))) {
            for(DataEntry e: candidates) {
                objectDistribution.getConfidences().put(e.getId(), 1.0/candidates.size());
            }
        }


        return objectDistribution.rescaleDistribution();

    }
}
