package de.dfki.tocalog.core.resolution;

import de.dfki.tocalog.core.ReferenceDistribution;
import de.dfki.tocalog.core.ReferenceResolver;
import de.dfki.tocalog.kb.*;

import java.util.ArrayList;
import java.util.List;

public class PointingReferenceResolver implements ReferenceResolver {


   //candidates: for example: entities in cone around pointing device
    private List<String> candidates = new ArrayList<>();
    private KnowledgeMap kMap;


    public PointingReferenceResolver(KnowledgeBase knowledgeBase, Type objectType) {
        kMap = knowledgeBase.getKnowledgeMap(objectType);
    }

    public void setCandidates(List<String> candidates) {
        this.candidates = candidates;
    }


    @Override
    public ReferenceDistribution getReferences() {
        ReferenceDistribution distribution = new ReferenceDistribution();

        //no pointing candidate
        if(candidates.isEmpty()) {
            for(Entity person: kMap.getAll()) {
                distribution.getConfidences().put(person.get(Ontology.id).get(),  1.0/ kMap.getAll().size());
            }
            return distribution;
        }


        for(String c: candidates) {
            distribution.getConfidences().put(c, 1.0/candidates.size());
        }

        return distribution;

    }
}
