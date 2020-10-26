package de.dfki.step.resolution_entity;

import de.dfki.step.kb.Entity;
import de.dfki.step.kb.Ontology;


import java.util.Collection;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/* the speaker is resolved */
public class SpeakerRR implements de.dfki.step.resolution_entity.ReferenceResolver {


    private String speakerId = "";
    private Collection<Entity> persons;


    public SpeakerRR(Supplier<Collection<Entity>> personSupplier) {
        persons = personSupplier.get();
    }

    public void setSpeakerId(String speakerId) {
        this.speakerId = speakerId;
    }

    @Override
    public de.dfki.step.resolution_entity.ReferenceDistribution getReferences() {
        de.dfki.step.resolution_entity.ReferenceDistribution distribution = new de.dfki.step.resolution_entity.ReferenceDistribution();

        Collection<Entity> speakers = persons.stream()
                .filter(e -> e.get(Ontology.id).get().equals(speakerId))
                .collect(Collectors.toList());

        Collection<Entity> nonspeakers = persons.stream()
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
