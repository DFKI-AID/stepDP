package de.dfki.step.resolution;

import de.dfki.step.deprecated.kb.DataEntry;
import de.dfki.step.util.Vector3;

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/* entities closer to the speaker receive higher confidence*/
public class ClosenessRR implements ReferenceResolver {


    private DataEntry speaker;
    private Collection<DataEntry> entities;



    public ClosenessRR(Supplier<Collection<DataEntry>> entitiesSupplier, DataEntry speaker) {
        this.entities = entitiesSupplier.get();
        this.speaker = speaker;
    }




    @Override
    public ReferenceDistribution getReferences() {

        ReferenceDistribution distribution = new ReferenceDistribution();


        Vector3 speakerPosition = speaker.get("position", Vector3.class).get();

        Collection<DataEntry> objectwithPositions = entities.stream()
                .filter(e -> e.get("position").isPresent())
                .collect(Collectors.toList());


        Collection<DataEntry> sortedObjects = objectwithPositions.stream()
                .sorted((e1, e2) -> ((Double) e1.get("position", Vector3.class).get().getDistance(speakerPosition))
                        .compareTo(((Double) e2.get("position", Vector3.class).get().getDistance(speakerPosition))))
                .collect(Collectors.toList());


        double counter = 0.0;
        for(int i = sortedObjects.size()-1; i>=0; i--) {
            distribution.getConfidences().put(((List<DataEntry>) sortedObjects).get(i).getId(), counter);
            counter += 1.0;
        }

        if(distribution.getConfidences().isEmpty()) {
            for(DataEntry e: entities) {
                distribution.getConfidences().put(e.getId(), 1.0/entities.size());
            }
        }

        return distribution.rescaleDistribution();

    }

}
