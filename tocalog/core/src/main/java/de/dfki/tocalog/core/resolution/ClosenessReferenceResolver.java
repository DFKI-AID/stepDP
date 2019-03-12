package de.dfki.tocalog.core.resolution;

import de.dfki.tocalog.core.ReferenceDistribution;
import de.dfki.tocalog.core.ReferenceResolver;
import de.dfki.tocalog.kb.*;
import de.dfki.tocalog.util.Vector3;

import java.util.*;
import java.util.stream.Collectors;

public class ClosenessReferenceResolver implements ReferenceResolver {


    private String speakerId = "";
    private KnowledgeMap personMap;
    private String type = "";
    private KnowledgeBase kb;


    public ClosenessReferenceResolver(KnowledgeBase knowledgeBase) {
        this.kb = knowledgeBase;
        personMap = knowledgeBase.getKnowledgeMap(Ontology.Person);
    }

    public void setSpeakerId(String speakerId) {
        this.speakerId = speakerId;
    }


    public void setEntityType(String type) {
        this.type = type;
    }

  /*  public static void main(String[] args) {
        KnowledgeMap kMap = new KnowledgeMap();
        Vector3 speakerPosition = new Vector3(0.0, 0.0, 0.0);


        Entity pers1 = new Entity();
        pers1 = pers1.set(position, new Vector3(1.0, 1.0, 1.0));
        kMap.add(pers1);
        Entity pers2 = new Entity();
        pers2 = pers2.set(position, new Vector3(4.0, 4.0, 4.0));
        kMap.add(pers2);
        Entity pers3 = new Entity();
        pers3 = pers3.set(position, new Vector3(-2.0, -2.0, -2.0));
        kMap.add(pers3);
        getReferences(speakerPosition, kMap);
    }*/


    @Override
    public ReferenceDistribution getReferences() {

        KnowledgeMap kMap = kb.getKnowledgeMap(type);
        ReferenceDistribution distribution = new ReferenceDistribution();

        if(!personMap.get(speakerId).isPresent() || !personMap.get(speakerId).get().get(Ontology.position).isPresent()) {
            for(Entity e: kMap.getAll()) {
                distribution.getConfidences().put(e.get(Ontology.id).get(), 1.0/kMap.getAll().size());
            }
            return distribution;
        }


        Vector3 speakerPosition = personMap.get(speakerId).get().get(Ontology.position).get();

        Collection<Entity> objectwithPositions = kMap.getAll().stream()
                .filter(e -> e.get(Ontology.position).isPresent())
                .collect(Collectors.toList());

        Collection<Entity> sortedObjects = objectwithPositions.stream()
                .sorted((e1, e2) -> ((Double) e1.get(Ontology.position).get().getDistance(speakerPosition))
                        .compareTo(((Double) e2.get(Ontology.position).get().getDistance(speakerPosition))))
                .collect(Collectors.toList());


        double counter = 0.0;
        for(int i = sortedObjects.size()-1; i>=0; i--) {
            distribution.getConfidences().put(((List<Entity>) sortedObjects).get(i).get(Ontology.id).get(), counter);
            counter += 1.0;
        }

        if(distribution.getConfidences().isEmpty()) {
            for(Entity e: kMap.getAll()) {
                distribution.getConfidences().put(e.get(Ontology.id).get(), 1.0/kMap.getAll().size());
            }
        }

        return distribution.rescaleDistribution();

    }

}
