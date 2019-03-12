package de.dfki.step.core.resolution;

import de.dfki.step.core.ReferenceDistribution;
import de.dfki.step.core.ReferenceResolver;
import de.dfki.step.kb.Entity;
import de.dfki.step.kb.KnowledgeBase;
import de.dfki.step.kb.Ontology;

import java.util.Collection;
import java.util.Optional;


public class LocationReferenceResolver implements ReferenceResolver {


    private String locationString;
    private Collection<Entity> referentMap;
    private Collection<Entity> baseMap;

    private final double DISTANCE = 2.0;


    public LocationReferenceResolver(KnowledgeBase knowledgeBase, String referentEntityType, String baseEntityType, String locationString) {
        this.locationString = locationString;
        referentMap = knowledgeBase.getKnowledgeMap(referentEntityType).getAll();
        baseMap = knowledgeBase.getKnowledgeMap(baseEntityType).getAll();
    }

    //TODO
  /*  @Override
    public ReferenceDistribution getReferences() {
        ReferenceDistribution locationDistribution = new ReferenceDistribution();
        PersonReferenceResolver personReferenceResolver = new PersonReferenceResolver(knowledgeBase, inputString);

        if(inputString.contains("here") || inputString.contains("to me")) {
           ReferenceDistribution personDistribution = personReferenceResolver.getReferences();
           for(String id: personDistribution.getConfidences().keySet()) {
               if(personMap.get(id).get().get(Ontology.position).isPresent()) {
                   Vector3 position = personMap.get(id).get().get(Ontology.position).get();
                   //id for position??
                   locationDistribution.getConfidences().put(position.toString(), personDistribution.getConfidences().get(id));
                }

           }
       }
       return locationDistribution;
    }*/

    @Override
    public ReferenceDistribution getReferences() {
        ReferenceDistribution locationDist = new ReferenceDistribution();

        for(Entity ref: referentMap) {
            if(ref.get(Ontology.position).isPresent()) {
                Optional<Entity> nextBase = Optional.empty();
                if(locationString.contains("next to")) {
                    nextBase = baseMap.stream()
                            .filter(baseE -> baseE.get(Ontology.position).isPresent())
                            .filter(baseE -> baseE.get(Ontology.position).get().getDistance(ref.get(Ontology.position).get()) <= DISTANCE)
                            .findAny();
                } else if(locationString.contains("above")|| locationString.contains("on top") ) {
                    nextBase = baseMap.stream()
                            .filter(baseE -> baseE.get(Ontology.position).isPresent())
                            .filter(baseE -> ref.get(Ontology.position).get().z - baseE.get(Ontology.position).get().z <= DISTANCE
                                    && ref.get(Ontology.position).get().z - baseE.get(Ontology.position).get().z > 0.0)
                            .findAny();
                } else if(locationString.contains("below") || locationString.contains("beneath")) {
                    nextBase = baseMap.stream()
                            .filter(baseE -> baseE.get(Ontology.position).isPresent())
                            .filter(baseE -> baseE.get(Ontology.position).get().z - ref.get(Ontology.position).get().z <= DISTANCE
                                    && baseE.get(Ontology.position).get().z - ref.get(Ontology.position).get().z > 0.0)
                            .findAny();

                } else if(locationString.contains("right")) {
                    nextBase = baseMap.stream()
                            .filter(baseE -> baseE.get(Ontology.position).isPresent())
                            .filter(baseE -> ref.get(Ontology.position).get().x - baseE.get(Ontology.position).get().x <= DISTANCE
                                    && ref.get(Ontology.position).get().x - baseE.get(Ontology.position).get().x > 0.0)
                            .findAny();

                } else if(locationString.contains("left")) {
                    nextBase = baseMap.stream()
                            .filter(baseE -> baseE.get(Ontology.position).isPresent())
                            .filter(baseE -> baseE.get(Ontology.position).get().x - ref.get(Ontology.position).get().x <= DISTANCE
                                    && baseE.get(Ontology.position).get().x - ref.get(Ontology.position).get().x > 0.0)
                            .findAny();

                } else if(locationString.contains("in front")) {
                    nextBase = baseMap.stream()
                            .filter(baseE -> baseE.get(Ontology.position).isPresent())
                            .filter(baseE -> baseE.get(Ontology.position).get().y - ref.get(Ontology.position).get().y <= DISTANCE
                                    && baseE.get(Ontology.position).get().y - ref.get(Ontology.position).get().y > 0.0)
                            .findAny();
                } else if(locationString.contains("behind")) {
                    nextBase = baseMap.stream()
                            .filter(baseE -> baseE.get(Ontology.position).isPresent())
                            .filter(baseE -> ref.get(Ontology.position).get().y - baseE.get(Ontology.position).get().y <= DISTANCE
                                    && ref.get(Ontology.position).get().y - baseE.get(Ontology.position).get().y > 0.0)
                            .findAny();
                }

                if(nextBase.isPresent()) {
                    locationDist.getConfidences().put(ref.get(Ontology.id).get(), 1.0);
                }else {
                    locationDist.getConfidences().put(ref.get(Ontology.id).get(), 0.0);
                }

            }
        }

        locationDist.rescaleDistribution();
        return locationDist;
    }



}
