package de.dfki.tocalog.core.resolution;

import de.dfki.tocalog.core.ReferenceDistribution;
import de.dfki.tocalog.core.ReferenceResolver;
import de.dfki.tocalog.kb.Entity;
import de.dfki.tocalog.kb.KnowledgeBase;
import de.dfki.tocalog.kb.KnowledgeMap;
import de.dfki.tocalog.kb.Ontology;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class PointingReferenceResolver implements ReferenceResolver {


   //candidates: for example: entities in cone around pointing device
    private List<String> candidates = new ArrayList<>();
    private KnowledgeMap personMap;


    public PointingReferenceResolver(KnowledgeBase knowledgeBase) {
        personMap = knowledgeBase.getKnowledgeMap(Ontology.Person);
    }

    public void setCandidates(List<String> candidates) {
        this.candidates = candidates;
    }


    @Override
    public ReferenceDistribution getReferences() {
        ReferenceDistribution distribution = new ReferenceDistribution();

        //no pointing candidate
        if(candidates.isEmpty()) {
            for(Entity person: personMap.getAll()) {
                distribution.getConfidences().put(person.get(Ontology.id).get(),  1.0/personMap.getAll().size());
            }
            return distribution;
        }


        for(String c: candidates) {
            distribution.getConfidences().put(c, 1.0/candidates.size());
        }

        return distribution;

    }
}
