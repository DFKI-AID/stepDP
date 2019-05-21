package de.dfki.step.resolution_entity;

import de.dfki.step.kb.Entity;
import de.dfki.step.kb.Ontology;


import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Supplier;

/* confidence is given to sessions not including the speaker */
public class ReverseSessionRR implements ReferenceResolver {


    private String speakerId = "";
    private Collection<Entity> sessions;

    public ReverseSessionRR(Supplier<Collection<Entity>> sessionSupplier) {
        sessions = sessionSupplier.get();
    }

    public void setSpeakerId(String speakerId) {
        this.speakerId = speakerId;
    }

    @Override
    public ReferenceDistribution getReferences() {
        ReferenceDistribution distribution = new ReferenceDistribution();

        Collection<Entity> othersessions = new ArrayList<Entity>();
        for(Entity s: sessions) {
            if(s.get(Ontology.agents).isPresent()) {
                if (!s.get(Ontology.agents).get().contains(speakerId)) {
                    othersessions.add(s);
                }
            }
        }

        for(Entity s: othersessions) {
            if(s.get(Ontology.agents).isPresent()) {
                for (String a : s.get(Ontology.agents).get()) {
                    distribution.getConfidences().put(a, 1.0 / s.get(Ontology.agents).get().size());
                }
            }
        }


        return distribution;

    }
}
