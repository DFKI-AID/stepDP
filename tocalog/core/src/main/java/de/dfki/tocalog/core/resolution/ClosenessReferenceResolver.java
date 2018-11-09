package de.dfki.tocalog.core.resolution;

import de.dfki.tocalog.core.ReferenceDistribution;
import de.dfki.tocalog.core.ReferenceResolver;
import de.dfki.tocalog.kb.*;
import de.dfki.tocalog.util.Vector3;

import java.util.*;
import java.util.stream.Collectors;

public class ClosenessReferenceResolver implements ReferenceResolver {

    private KnowledgeMap kMap;
    private String speakerId;
    private KnowledgeMap personMap;
    public static final Attribute<Vector3> position = new Attribute<>("tocalog/position");


    ClosenessReferenceResolver(KnowledgeBase knowledgeBase, Type type) {
        kMap = knowledgeBase.getKnowledgeMap(type);
        personMap = knowledgeBase.getKnowledgeMap(Ontology.Person);
    }

    public void setSpeakerId(String speakerId) {
        this.speakerId = speakerId;
    }

    public static void main(String[] args) {
        KnowledgeMap kMap = new KnowledgeMap();
        Vector3 speakerPosition = new Vector3(0.0, 0.0, 0.0);


        Entity pers1 = new Entity();
        position.set(pers1, new Vector3(1.0, 1.0, 1.0));
        pers1.set(position, new Vector3(1.0, 1.0, 1.0));
        kMap.add(pers1);
        Entity pers2 = new Entity();
        pers2.set(position, new Vector3(4.0, 4.0, 4.0));
        kMap.add(pers2);
        Entity pers3 = new Entity();
        pers3.set(position, new Vector3(-2.0, -2.0, -2.0));
        kMap.add(pers3);
        getReferences(speakerPosition, kMap);
    }


    //@Override
    public static ReferenceDistribution getReferences(Vector3 speakerPosition, KnowledgeMap kMap) {
        ReferenceDistribution distribution = new ReferenceDistribution();

       /* if(!personMap.get(speakerId).isPresent()) {
            return distribution;
        }
        Entity person = personMap.get(speakerId).get();

        if(!person.get(Ontology.position).isPresent()) {
            return distribution;
        }
        Vector3 speakerPosition = person.get(Ontology.position).get();

        */
        for(Entity e: kMap.getAll()) {
            System.out.println("entity: " + e.get(position).get());
        }


       /* Collection<Entity> objectwithPositions = kMap.getAll().stream()
                .filter(e -> e.get(Ontology.position).isPresent())
                .collect(Collectors.toList());

        Collection<Entity> sortedObjects = objectwithPositions.stream()
                .sorted((e1, e2) -> ((Double) e1.get(Ontology.position).get().getDistance(speakerPosition))
                        .compareTo(((Double) e2.get(Ontology.position).get().getDistance(speakerPosition))))
                .collect(Collectors.toList());

        */

        Collection<Entity> objectwithPositions = kMap.getAll().stream()
                .filter(e -> e.get(position).isPresent())
                .collect(Collectors.toList());

        Collection<Entity> sortedObjects = objectwithPositions.stream()
                .sorted((e1, e2) -> ((Double) e1.get(position).get().getDistance(speakerPosition))
                        .compareTo(((Double) e2.get(position).get().getDistance(speakerPosition))))
                .collect(Collectors.toList());
        System.out.println("sorted: " + sortedObjects.toString());
        double counter = 0.0;
        for(int i = sortedObjects.size()-1; i>0; i--) {
            distribution.getConfidences().put(((List<Entity>) sortedObjects).get(i).get(Ontology.id).get(), counter);
            counter += 1.0;
        }

        Optional<Double> totalCount = distribution.getConfidences().values().stream().reduce((d1, d2) -> d1 + d2);
        if (!totalCount.isPresent()) {
            return distribution;
        }
        for (String id: distribution.getConfidences().keySet()) {
            distribution.getConfidences().put(id, distribution.getConfidences().get(id) / totalCount.get());
        }

        System.out.println("distribution: " + distribution.getConfidences().toString());
        return distribution;

    }

    @Override
    public ReferenceDistribution getReferences() {
        return null;
    }
}
