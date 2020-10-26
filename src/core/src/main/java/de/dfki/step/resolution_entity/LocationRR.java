package de.dfki.step.resolution_entity;

import de.dfki.step.kb.Entity;
import de.dfki.step.kb.Ontology;


import java.util.Collection;
import java.util.Optional;
import java.util.function.Supplier;

/* if candidate entities (given by refSupplier) have certain positional relation (e.g. next to/above) to another object of a specific type (given by baseSupplier)
 * they receive a higher confidence */
public class LocationRR implements de.dfki.step.resolution_entity.ReferenceResolver {


    private String locationRelation;
    private Collection<Entity> refCandidates;
    private Collection<Entity> baseObjects;
    private final double DISTANCE = 2.0;

    public LocationRR(Supplier<Collection<Entity>> refSupplier, Supplier<Collection<Entity>> baseSupplier) {
        refCandidates = refSupplier.get();
        baseObjects = baseSupplier.get();
    }


    public void setLocationRelation(String locationRelation) {
        this.locationRelation = locationRelation;
    }


    @Override
    public de.dfki.step.resolution_entity.ReferenceDistribution getReferences() {
        de.dfki.step.resolution_entity.ReferenceDistribution locationDist = new de.dfki.step.resolution_entity.ReferenceDistribution();

        for(Entity candidate: refCandidates) {
            if(candidate.get(Ontology.position).isPresent()) {
                Optional<Entity> nextBase = Optional.empty();
                if(locationRelation.equals("next_to")) {
                    nextBase = baseObjects.stream()
                            .filter(baseE -> baseE.get(Ontology.position).isPresent())
                            .filter(baseE -> baseE.get(Ontology.position).get().getDistance(candidate.get(Ontology.position).get()) <= DISTANCE)
                            .findAny();
                } else if(locationRelation.equals("above") ) {
                    nextBase = baseObjects.stream()
                            .filter(baseE -> baseE.get(Ontology.position).isPresent())
                            .filter(baseE -> candidate.get(Ontology.position).get().z - baseE.get(Ontology.position).get().z <= DISTANCE
                                    && candidate.get(Ontology.position).get().z - baseE.get(Ontology.position).get().z > 0.0)
                            .findAny();
                } else if(locationRelation.equals("below")) {
                    nextBase = baseObjects.stream()
                            .filter(baseE -> baseE.get(Ontology.position).isPresent())
                            .filter(baseE -> baseE.get(Ontology.position).get().z - candidate.get(Ontology.position).get().z <= DISTANCE
                                    && baseE.get(Ontology.position).get().z - candidate.get(Ontology.position).get().z > 0.0)
                            .findAny();

                } else if(locationRelation.equals("right")) {
                    nextBase = baseObjects.stream()
                            .filter(baseE -> baseE.get(Ontology.position).isPresent())
                            .filter(baseE -> candidate.get(Ontology.position).get().x - baseE.get(Ontology.position).get().x <= DISTANCE
                                    && candidate.get(Ontology.position).get().x - baseE.get(Ontology.position).get().x > 0.0)
                            .findAny();

                } else if(locationRelation.equals("left")) {
                    nextBase = baseObjects.stream()
                            .filter(baseE -> baseE.get(Ontology.position).isPresent())
                            .filter(baseE -> baseE.get(Ontology.position).get().x - candidate.get(Ontology.position).get().x <= DISTANCE
                                    && baseE.get(Ontology.position).get().x - candidate.get(Ontology.position).get().x > 0.0)
                            .findAny();

                } else if(locationRelation.equals("in_front")) {
                    nextBase = baseObjects.stream()
                            .filter(baseE -> baseE.get(Ontology.position).isPresent())
                            .filter(baseE -> baseE.get(Ontology.position).get().y - candidate.get(Ontology.position).get().y <= DISTANCE
                                    && baseE.get(Ontology.position).get().y - candidate.get(Ontology.position).get().y > 0.0)
                            .findAny();
                } else if(locationRelation.equals("behind")) {
                    nextBase = baseObjects.stream()
                            .filter(baseE -> baseE.get(Ontology.position).isPresent())
                            .filter(baseE -> candidate.get(Ontology.position).get().y - baseE.get(Ontology.position).get().y <= DISTANCE
                                    && candidate.get(Ontology.position).get().y - baseE.get(Ontology.position).get().y > 0.0)
                            .findAny();
                }

                if(nextBase.isPresent()) {
                    locationDist.getConfidences().put(candidate.get(Ontology.id).get(), 1.0);
                }else {
                    locationDist.getConfidences().put(candidate.get(Ontology.id).get(), 0.0);
                }

            }
        }

        locationDist.rescaleDistribution();
        return locationDist;
    }


}
