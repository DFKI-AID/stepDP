package de.dfki.step.resolution_entity;

import de.dfki.step.deprecated.kb.Entity;
import de.dfki.step.deprecated.kb.Ontology;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

/* confidence is given to the session including the speaker */
public class SessionRR implements de.dfki.step.resolution_entity.ReferenceResolver {


    private String speakerId = "";
    private Collection<Entity> sessions;

    public SessionRR(Supplier<Collection<Entity>> sessionSupplier) {
        sessions = sessionSupplier.get();
    }

    public void setSpeakerId(String speakerId) {
        this.speakerId = speakerId;
    }

    @Override
    public de.dfki.step.resolution_entity.ReferenceDistribution getReferences() {
        de.dfki.step.resolution_entity.ReferenceDistribution distribution = new de.dfki.step.resolution_entity.ReferenceDistribution();

        Entity speakerSession = new Entity();
        List<Entity> otherSessions = new ArrayList<>();

        for(Entity s: sessions) {
            if(s.get(Ontology.agents).isPresent()) {
                if (s.get(Ontology.agents).get().contains(speakerId)) {
                    speakerSession = s;
                }else {
                    otherSessions.add(s);
                }
            }
        }

        if(speakerSession.get(Ontology.agents).isPresent()) {
            // better: take confidence that agent is in session
            for (String a : speakerSession.get(Ontology.agents).get()) {
                distribution.getConfidences().put(a, 1.0 / speakerSession.get(Ontology.agents).get().size());
            }
        }

        for(Entity s: otherSessions) {
            if(s.get(Ontology.agents).isPresent()) {
                for (String a : s.get(Ontology.agents).get()) {
                    distribution.getConfidences().put(a, 0.0);
                }
            }
        }

        return distribution;

    }
}
