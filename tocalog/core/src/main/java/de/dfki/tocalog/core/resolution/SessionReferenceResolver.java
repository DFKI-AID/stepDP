package de.dfki.tocalog.core.resolution;

import de.dfki.tocalog.core.ReferenceDistribution;
import de.dfki.tocalog.core.ReferenceResolver;
import de.dfki.tocalog.kb.Entity;
import de.dfki.tocalog.kb.KnowledgeBase;
import de.dfki.tocalog.kb.KnowledgeMap;
import de.dfki.tocalog.kb.Ontology;

import java.util.Collection;
import java.util.stream.Collectors;
import org.pcollections.*;

import static de.dfki.tocalog.kb.Ontology.Agent;

public class SessionReferenceResolver implements ReferenceResolver {

   // public static final String Speaker = "tocalog/kinect/Speaker";



    private String speakerId;
    private KnowledgeMap personMap;
    private KnowledgeMap sessionMap;


    SessionReferenceResolver(KnowledgeBase knowledgeBase) {
        personMap = knowledgeBase.getKnowledgeMap(Ontology.Person);
        sessionMap = knowledgeBase.getKnowledgeMap(Ontology.Session);
    }

    public void setSpeakerId(String speakerId) {
        this.speakerId = speakerId;
    }

    @Override
    public ReferenceDistribution getReferences() {
        ReferenceDistribution distribution = new ReferenceDistribution();

       /* Collection<Entity> persons = sessionMap.getAll().stream()
                .map(s -> s.get(Ontology.agents).get())
                .filter(a -> ((PSet<String>) a).contains(speakerId))
                .map(s -> )
                .collect(Collectors.toList());
                */
        Entity speakerSession = null;
        Collection<Entity> othersessions = null;
        for(Entity s: sessionMap.getAll()) {
            if(s.get(Ontology.agents).get().contains(speakerId)) {
                speakerSession = s;
            }else {
                othersessions.add(s);
            }
        }
        // better: take confidence that agent is in session
        for(String a: speakerSession.get(Ontology.agents).get()) {
            distribution.getConfidences().put(a, 1.0);
        }

        for(Entity s: othersessions) {
            for(String a: s.get(Ontology.agents).get()) {
                distribution.getConfidences().put(a, 0.0);
            }
        }




        return distribution;

    }
}
