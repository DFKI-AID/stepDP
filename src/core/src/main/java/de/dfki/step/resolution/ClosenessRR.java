package de.dfki.step.resolution;

import de.dfki.step.kb.Entity;
import de.dfki.step.kb.Ontology;

import de.dfki.step.util.Vector3;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/* entities closer to the speaker receive higher confidence*/
public class ClosenessRR implements ReferenceResolver {


    private Entity speaker;
    private Collection<Entity> entities;



    public ClosenessRR(Supplier<Collection<Entity>> entitiesSupplier, Entity speaker) {
        this.entities = entitiesSupplier.get();
        this.speaker = speaker;
    }




    @Override
    public ReferenceDistribution getReferences() {

        ReferenceDistribution distribution = new ReferenceDistribution();


        Vector3 speakerPosition = speaker.get(Ontology.position).get();

        Collection<Entity> objectwithPositions = entities.stream()
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
            for(Entity e: entities) {
                distribution.getConfidences().put(e.get(Ontology.id).get(), 1.0/entities.size());
            }
        }

        return distribution.rescaleDistribution();

    }

}
