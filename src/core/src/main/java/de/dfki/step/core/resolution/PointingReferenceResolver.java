package de.dfki.step.core.resolution;

import de.dfki.step.core.ReferenceDistribution;
import de.dfki.step.core.ReferenceResolver;
import de.dfki.step.kb.*;

import java.util.ArrayList;
import java.util.List;

public class PointingReferenceResolver implements ReferenceResolver {


   //candidates: for example: entities in cone around pointing device
    private List<String> candidates = new ArrayList<>();
    private KnowledgeMap kMap = new KnowledgeMap();
    private KnowledgeBase kb;
    private String objectType = "";


    public PointingReferenceResolver(KnowledgeBase knowledgeBase) {
      this.kb = knowledgeBase;
    }

    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }

    public void setCandidates(List<String> candidates) {
        this.candidates = candidates;
    }


    @Override
    public ReferenceDistribution getReferences() {
        kMap = kb.getKnowledgeMap(objectType);
        ReferenceDistribution distribution = new ReferenceDistribution();

        //no pointing candidate
        if(candidates.isEmpty()) {
            for(Entity entity: kMap.getAll()) {
                distribution.getConfidences().put(entity.get(Ontology.id).get(),  1.0/ kMap.getAll().size());
            }
            return distribution;
        }


        for(String c: candidates) {
            distribution.getConfidences().put(c, 1.0/candidates.size());
        }

        return distribution;

    }
}
