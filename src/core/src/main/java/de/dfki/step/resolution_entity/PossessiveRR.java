package de.dfki.step.resolution_entity;

import de.dfki.step.deprecated.kb.Entity;
import de.dfki.step.deprecated.kb.Ontology;
import de.dfki.step.deprecated.kb.Reference;
import de.dfki.step.kb.*;


import java.util.Collection;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/* personal pronouns are resolved and tested if candidate persons have object of correct type (given by objectSupplier) */
public class PossessiveRR implements de.dfki.step.resolution_entity.ReferenceResolver {


    private de.dfki.step.resolution_entity.ReferenceResolver possessivePronounResolver = new de.dfki.step.resolution_entity.WeightedRR();
    private Collection<Entity> objects;


    public PossessiveRR(Supplier<Collection<Entity>> objectSupplier) {
        this.objects = objectSupplier.get();
    }

    public void setPossessivePronounResolver(de.dfki.step.resolution_entity.ReferenceResolver possessivePronounResolver) {
        this.possessivePronounResolver = possessivePronounResolver;
    }




    @Override
    public de.dfki.step.resolution_entity.ReferenceDistribution getReferences() {

        de.dfki.step.resolution_entity.ReferenceDistribution objectDistribution = new de.dfki.step.resolution_entity.ReferenceDistribution();

        de.dfki.step.resolution_entity.ReferenceDistribution personDistribution = possessivePronounResolver.getReferences();

        for(String personId: personDistribution.getConfidences().keySet()) {
            Collection<Entity> ownedObjects = objects.stream().filter(o -> o.get(Ontology.owner).orElse(new Reference("", "")).id.equals(personId)).collect(Collectors.toList());
            for(Entity obj: ownedObjects) {
                objectDistribution.getConfidences().put(obj.get(Ontology.id).get(), personDistribution.getConfidences().get(personId));
            }
        }

        //no person with device
        if(objectDistribution.getConfidences().isEmpty()) {
            for(Entity obj: objects) {
                objectDistribution.getConfidences().put(obj.get(Ontology.id).get(), 1.0/objects.size());
            }
        }

        return objectDistribution.rescaleDistribution();

    }
}
