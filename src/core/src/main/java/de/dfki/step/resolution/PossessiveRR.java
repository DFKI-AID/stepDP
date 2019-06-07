package de.dfki.step.resolution;

import de.dfki.step.kb.DataEntry;
import de.dfki.step.kb.Entity;
import de.dfki.step.kb.Ontology;
import de.dfki.step.kb.Reference;

import java.util.Collection;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/* personal pronouns are resolved and tested if candidate persons have object of correct type (given by objectSupplier) */
public class PossessiveRR implements ReferenceResolver {


    private ReferenceResolver possessivePronounResolver = new WeightedRR();
    private Collection<DataEntry> objects;


    public PossessiveRR(Supplier<Collection<DataEntry>> objectSupplier) {
        this.objects = objectSupplier.get();
    }

    public void setPossessivePronounResolver(ReferenceResolver possessivePronounResolver) {
        this.possessivePronounResolver = possessivePronounResolver;
    }




    @Override
    public ReferenceDistribution getReferences() {

        ReferenceDistribution objectDistribution = new ReferenceDistribution();

        ReferenceDistribution personDistribution = possessivePronounResolver.getReferences();

        for(String personId: personDistribution.getConfidences().keySet()) {
            Collection<DataEntry> ownedObjects = objects.stream().filter(o -> o.get("owner").orElse("").equals(personId)).collect(Collectors.toList());
            for(DataEntry obj: ownedObjects) {
                objectDistribution.getConfidences().put(obj.getId(), personDistribution.getConfidences().get(personId));
            }
        }

        //no person with device
        if(objectDistribution.getConfidences().isEmpty()) {
            for(DataEntry obj: objects) {
                objectDistribution.getConfidences().put(obj.getId(), 1.0/objects.size());
            }
        }

        return objectDistribution.rescaleDistribution();

    }
}
