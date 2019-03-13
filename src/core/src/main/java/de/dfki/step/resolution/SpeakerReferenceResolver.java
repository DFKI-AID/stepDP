package de.dfki.step.resolution;

import de.dfki.step.kb.*;

import java.util.Collection;
import java.util.stream.Collectors;

public class SpeakerReferenceResolver implements ReferenceResolver {


    private String speakerId = "";
    private KnowledgeMap personMap;


    public SpeakerReferenceResolver(KnowledgeBase knowledgeBase) {
        personMap = knowledgeBase.getKnowledgeMap(Ontology.Person);
    }

    public void setSpeakerId(String speakerId) {
        this.speakerId = speakerId;
    }

    @Override
    public ReferenceDistribution getReferences() {
        ReferenceDistribution distribution = new ReferenceDistribution();

        Collection<Entity> speakers = personMap.getAll().stream()
                .filter(e -> e.get(Ontology.id).get().equals(speakerId))
                .collect(Collectors.toList());

        Collection<Entity> nonspeakers = personMap.getAll().stream()
                .filter(e -> !e.get(Ontology.id).get().equals(speakerId))
                .collect(Collectors.toList());

        // 1.0/0.0 could be replaced with speaker confidence and rest
        for(Entity s: speakers) {
            if(s.get(Ontology.id).isPresent()) {
                distribution.getConfidences().put(s.get(Ontology.id).get(), 1.0);
            }
        }
        for(Entity s: nonspeakers) {
            if(s.get(Ontology.id).isPresent()) {
                distribution.getConfidences().put(s.get(Ontology.id).get(), 0.0);
            }
        }
        return distribution;

    }
}
