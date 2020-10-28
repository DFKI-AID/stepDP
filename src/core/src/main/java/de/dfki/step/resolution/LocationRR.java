package de.dfki.step.resolution;

import de.dfki.step.deprecated.kb.DataEntry;
import de.dfki.step.util.Vector3;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Supplier;

/* if candidate entities (given by refSupplier) have certain positional relation (e.g. next to/above) to another object of a specific type (given by baseSupplier)
 * they receive a higher confidence */
public class LocationRR implements ReferenceResolver {


    private String locationRelation;
    private Collection<DataEntry> refCandidates;
    private Collection<DataEntry> baseObjects;
    private final double DISTANCE = 2.0;

    public LocationRR(Supplier<Collection<DataEntry>> refSupplier, Supplier<Collection<DataEntry>> baseSupplier) {
        refCandidates = refSupplier.get();
        baseObjects = baseSupplier.get();
    }


    public void setLocationRelation(String locationRelation) {
        this.locationRelation = locationRelation;
    }


    @Override
    public ReferenceDistribution getReferences() {
        ReferenceDistribution locationDist = new ReferenceDistribution();

        for(DataEntry candidate: refCandidates) {
            if(candidate.get("position", Vector3.class).isPresent()) {
                Optional<DataEntry> nextBase = Optional.empty();
                if(locationRelation.equals("next_to")) {
                    nextBase = baseObjects.stream()
                            .filter(baseE -> baseE.get("position", Vector3.class).isPresent())
                            .filter(baseE -> baseE.get("position", Vector3.class).get().getDistance(candidate.get("position", Vector3.class).get()) <= DISTANCE)
                            .findAny();
                } else if(locationRelation.equals("above") ) {
                    nextBase = baseObjects.stream()
                            .filter(baseE -> baseE.get("position", Vector3.class).isPresent())
                            .filter(baseE -> candidate.get("position", Vector3.class).get().z - baseE.get("position", Vector3.class).get().z <= DISTANCE
                                    && candidate.get("position", Vector3.class).get().z - baseE.get("position", Vector3.class).get().z > 0.0)
                            .findAny();
                } else if(locationRelation.equals("below")) {
                    nextBase = baseObjects.stream()
                            .filter(baseE -> baseE.get("position", Vector3.class).isPresent())
                            .filter(baseE -> baseE.get("position", Vector3.class).get().z - candidate.get("position", Vector3.class).get().z <= DISTANCE
                                    && baseE.get("position", Vector3.class).get().z - candidate.get("position", Vector3.class).get().z > 0.0)
                            .findAny();

                } else if(locationRelation.equals("right")) {
                    nextBase = baseObjects.stream()
                            .filter(baseE -> baseE.get("position", Vector3.class).isPresent())
                            .filter(baseE -> candidate.get("position", Vector3.class).get().x - baseE.get("position", Vector3.class).get().x <= DISTANCE
                                    && candidate.get("position", Vector3.class).get().x - baseE.get("position", Vector3.class).get().x > 0.0)
                            .findAny();

                } else if(locationRelation.equals("left")) {
                    nextBase = baseObjects.stream()
                            .filter(baseE -> baseE.get("position", Vector3.class).isPresent())
                            .filter(baseE -> baseE.get("position", Vector3.class).get().x - candidate.get("position", Vector3.class).get().x <= DISTANCE
                                    && baseE.get("position", Vector3.class).get().x - candidate.get("position", Vector3.class).get().x > 0.0)
                            .findAny();

                } else if(locationRelation.equals("in_front")) {
                    nextBase = baseObjects.stream()
                            .filter(baseE -> baseE.get("position", Vector3.class).isPresent())
                            .filter(baseE -> baseE.get("position", Vector3.class).get().y - candidate.get("position", Vector3.class).get().y <= DISTANCE
                                    && baseE.get("position", Vector3.class).get().y - candidate.get("position", Vector3.class).get().y > 0.0)
                            .findAny();
                } else if(locationRelation.equals("behind")) {
                    nextBase = baseObjects.stream()
                            .filter(baseE -> baseE.get("position", Vector3.class).isPresent())
                            .filter(baseE -> candidate.get("position", Vector3.class).get().y - baseE.get("position", Vector3.class).get().y <= DISTANCE
                                    && candidate.get("position", Vector3.class).get().y - baseE.get("position", Vector3.class).get().y > 0.0)
                            .findAny();
                }

                if(nextBase.isPresent()) {
                    locationDist.getConfidences().put(candidate.getId(), 1.0);
                }else {
                    locationDist.getConfidences().put(candidate.getId(), 0.0);
                }

            }
        }

        locationDist.rescaleDistribution();
        return locationDist;
    }


}
