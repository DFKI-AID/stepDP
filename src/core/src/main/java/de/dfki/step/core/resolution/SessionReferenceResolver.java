package de.dfki.step.core.resolution;

import de.dfki.step.core.ReferenceDistribution;
import de.dfki.step.core.ReferenceResolver;
import de.dfki.step.kb.Entity;
import de.dfki.step.kb.KnowledgeBase;
import de.dfki.step.kb.KnowledgeMap;
import de.dfki.step.kb.Ontology;

import java.util.ArrayList;
import java.util.Collection;


public class SessionReferenceResolver implements ReferenceResolver {


    private String speakerId = "";
    private KnowledgeMap sessionMap;
    private KnowledgeMap personMap;


    public SessionReferenceResolver(KnowledgeBase knowledgeBase) {
        sessionMap = knowledgeBase.getKnowledgeMap(Ontology.Session);
        personMap = knowledgeBase.getKnowledgeMap(Ontology.Person);
    }

    public void setSpeakerId(String speakerId) {
        this.speakerId = speakerId;
    }

    @Override
    public ReferenceDistribution getReferences() {
        ReferenceDistribution distribution = new ReferenceDistribution();
        if(speakerId.equals("")) {
            for(Entity e: personMap.getAll()) {
                distribution.getConfidences().put(e.get(Ontology.id).get(), 1.0 / personMap.getAll().size());
            }
            return  distribution;
        }

        Entity speakerSession = new Entity();
        Collection<Entity> othersessions = new ArrayList<Entity>();
        for(Entity s: sessionMap.getAll()) {
            if(s.get(Ontology.agents).isPresent()) {
                if (s.get(Ontology.agents).get().contains(speakerId)) {
                    speakerSession = s;
                } else {
                    othersessions.add(s);
                }
            }
        }
        if(speakerSession.get(Ontology.agents).isPresent()) {
            // better: take confidence that agent is in session
            for (String a : speakerSession.get(Ontology.agents).get()) {
                distribution.getConfidences().put(a, 1.0 / speakerSession.get(Ontology.agents).get().size());
            }
        }

        for(Entity s: othersessions) {
            if(s.get(Ontology.agents).isPresent()) {
                for (String a : s.get(Ontology.agents).get()) {
                    distribution.getConfidences().put(a, 0.0);
                }
            }
        }




        return distribution;

    }
}
